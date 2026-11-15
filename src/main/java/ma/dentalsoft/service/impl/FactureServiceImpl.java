package ma.dentalsoft.service.impl;

import ma.dentalsoft.dto.ApercuFactureDTO;
import ma.dentalsoft.config.JdbcConnectionFactory;
import ma.dentalsoft.domain.Facture;
import ma.dentalsoft.domain.Permission;
import ma.dentalsoft.domain.Utilisateur;
import ma.dentalsoft.exceptions.DatabaseException;
import ma.dentalsoft.exceptions.DentalSoftException;
import ma.dentalsoft.exceptions.ValidationException;
import ma.dentalsoft.repository.api.FactureRepository;
import ma.dentalsoft.repository.impl.jdbc_mysql.JdbcFactureRepository;
import ma.dentalsoft.session.SessionManager;
import ma.dentalsoft.service.api.ConsultationService;
import ma.dentalsoft.service.api.FactureService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/** M6 : aperçu mémoire et génération transactionnelle d'un brouillon persisté. */
public class FactureServiceImpl implements FactureService {

    private static final BigDecimal MONTANT_MAX = new BigDecimal("9999999999.99");

    private final ConsultationService consultationService;
    private final FactureRepository factureRepository;

    /**
     * Fournir l'instance M5 qui contient les consultations : le service actuel
     * utilise des listes propres à chaque instance. Ne pas en créer une ici.
     */
    public FactureServiceImpl(ConsultationService consultationService) {
        this(consultationService, new JdbcFactureRepository());
    }

    public FactureServiceImpl(ConsultationService consultationService, FactureRepository factureRepository) {
        this.consultationService = Objects.requireNonNull(consultationService);
        this.factureRepository = Objects.requireNonNull(factureRepository);
    }

    @Override
    public ApercuFactureDTO previsualiserFacture(Long consultationId) {
        verifierAcces();
        if (consultationId == null || consultationId <= 0) {
            throw new IllegalArgumentException("L'identifiant de consultation doit être positif.");
        }

        BigDecimal total = consultationService.calculerTotalConsultation(consultationId);
        return new ApercuFactureDTO(consultationId, verifierMontant(total));
    }

    @Override
    public Facture genererBrouillon(Long consultationId, String numero) throws DentalSoftException {
        Utilisateur auteur = verifierAcces();
        if (consultationId == null || consultationId <= 0) {
            throw new ValidationException("L'identifiant de consultation doit être positif.");
        }
        if (numero == null || numero.isBlank() || numero.trim().length() > 50) {
            throw new ValidationException("Le numéro de facture est obligatoire (50 caractères maximum).");
        }

        try (Connection cn = ouvrirConnexion()) {
            cn.setAutoCommit(false);
            try {
                var consultation = factureRepository.verrouillerConsultation(cn, consultationId)
                        .orElseThrow(() -> new ValidationException("Consultation persistée introuvable."));
                if (!"TERMINEE".equals(consultation.statut())) {
                    throw new ValidationException("La consultation doit être validée avant facturation.");
                }
                if (consultation.interventions().isEmpty()) {
                    throw new ValidationException("La consultation ne contient aucune intervention.");
                }
                if (factureRepository.trouverParConsultation(cn, consultationId).isPresent()) {
                    throw new ValidationException("Une facture existe déjà pour cette consultation.");
                }

                BigDecimal total = BigDecimal.ZERO;
                for (var ligne : consultation.interventions()) {
                    if (ligne.quantite() <= 0) {
                        throw new ValidationException("La quantité d'intervention doit être positive.");
                    }
                    total = total.add(verifierMontant(ligne.prixUnitaire())
                            .multiply(BigDecimal.valueOf(ligne.quantite())));
                }
                total = verifierMontant(total);
                long sfId = factureRepository.obtenirSituationFinanciere(
                        cn, consultation.dossierId(), auteur.getIdUtilisateur());
                var facture = new Facture(consultationId, sfId, numero.trim(),
                        LocalDateTime.now(), total, "BROUILLON");
                facture.setCreePar(auteur.getIdUtilisateur());
                factureRepository.inserer(cn, facture);
                cn.commit();
                return facture;
            } catch (SQLException | DentalSoftException | RuntimeException e) {
                try {
                    cn.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
                if (e instanceof SQLException sql && sql.getErrorCode() == 1062) {
                    throw new ValidationException("Le numéro ou la consultation possède déjà une facture.", e);
                }
                if (e instanceof SQLException sql) {
                    throw new DatabaseException("Échec de la génération du brouillon de facture.", sql);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Connexion ou transaction de facturation impossible.", e);
        }
    }

    /** Réutilise la factory existante ; surcharge possible uniquement pour les tests isolés. */
    protected Connection ouvrirConnexion() throws SQLException {
        return JdbcConnectionFactory.getConnection();
    }

    private Utilisateur verifierAcces() {
        var session = SessionManager.getInstance();
        var utilisateur = session.getUtilisateur();
        if (utilisateur == null || !utilisateur.isActif()
                || utilisateur.getIdUtilisateur() == null || utilisateur.getIdUtilisateur() <= 0
                || !("SECRETAIRE".equals(utilisateur.getRole().getCode())
                || "MEDECIN".equals(utilisateur.getRole().getCode()))
                || !session.peut(Permission.GERER_CAISSE)) {
            throw new SecurityException("Accès refusé : session et droits de facturation requis.");
        }
        return utilisateur;
    }

    private BigDecimal verifierMontant(BigDecimal total) {
        if (total == null || total.signum() < 0 || total.compareTo(MONTANT_MAX) > 0) {
            throw new IllegalStateException("Le montant fourni par la consultation est invalide.");
        }
        try {
            // Aucun arrondi métier inventé : refuser une fraction de centime.
            total = total.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalStateException("Le montant doit être exprimable avec deux décimales.", e);
        }

        return total;
    }
}
