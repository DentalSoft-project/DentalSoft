package ma.dentalsoft.service.impl;

import ma.dentalsoft.domain.Consultation;
import ma.dentalsoft.domain.Intervention;
import ma.dentalsoft.security.SessionManager;
import ma.dentalsoft.service.api.ConsultationService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation du service de gestion des consultations (Module M5 - Sprint 2).
 * Gère les règles métier RG-06 (une consultation par RDV) et RG-07 (quantité > 0).
 */
public class ConsultationServiceImpl implements ConsultationService {

    // --- SIMULATION DE LA BASE DE DONNÉES (En attendant le DAO JDBC du Sprint 3) ---
    // Ces listes remplacent temporairement les appels SQL pour que l'on puisse tester l'interface.
    private final List<Consultation> listeConsultations = new ArrayList<>();
    private final List<Intervention> listeInterventions = new ArrayList<>();
    private Long compteurConsultationId = 1L;
    private Long compteurInterventionId = 1L;

    // =========================================================================
    // US-M5-01 : Démarrer une consultation
    // =========================================================================
    @Override
    public Consultation demarrerConsultation(Long dossierId, Long rdvId) {
        // 1. Vérification des droits (RG-12) : Seul un médecin peut démarrer
        if (!SessionManager.hasAccess("MEDECIN")) {
            throw new SecurityException("Accès refusé : Seul un médecin peut démarrer une consultation.");
        }

        // 2. Vérification RG-06 : Un RDV ne peut produire qu'une seule consultation
        if (rdvId != null) {
            for (Consultation c : listeConsultations) {
                if (c.getRdvId() != null && c.getRdvId().equals(rdvId)) {
                    throw new IllegalStateException("Erreur RG-06 : Ce rendez-vous a déjà une consultation associée.");
                }
            }
        }

        // 3. Création et sauvegarde
        Consultation nouvelleConsultation = new Consultation(dossierId, 99L, rdvId); // 99L = faux ID médecin pour le test
        nouvelleConsultation.setId(compteurConsultationId++);
        listeConsultations.add(nouvelleConsultation);

        System.out.println("[LOG] Consultation démarrée avec succès (ID: " + nouvelleConsultation.getId() + ") pour le dossier : " + dossierId);
        return nouvelleConsultation;
    }

    // =========================================================================
    // US-M5-03 : Ajouter une intervention (Acte effectué)
    // =========================================================================
    @Override
    public Intervention ajouterIntervention(Long consultationId, Long acteId, String libelle, String dent, int quantite, BigDecimal prix) {
        // 1. Vérification des droits (RG-12)
        if (!SessionManager.hasAccess("MEDECIN")) {
            throw new SecurityException("Accès refusé : Seul un médecin peut ajouter des interventions.");
        }

        // 2. Vérification RG-07 : La quantité doit être positive
        if (quantite <= 0) {
            throw new IllegalArgumentException("Erreur RG-07 : La quantité doit être strictement supérieure à 0.");
        }

        // 3. Vérification que la consultation existe et est toujours ouverte
        Consultation cons = trouverConsultationParId(consultationId);
        if (cons == null) {
            throw new IllegalArgumentException("Erreur : Consultation introuvable avec l'ID " + consultationId);
        }
        if (!cons.getStatut().equals("EN_COURS")) {
            throw new IllegalStateException("Erreur : Impossible d'ajouter un acte, la consultation est déjà " + cons.getStatut());
        }

        // 4. Création de l'intervention
        Intervention intervention = new Intervention(consultationId, acteId, libelle, dent, quantite, prix);
        intervention.setId(compteurInterventionId++);
        listeInterventions.add(intervention);

        return intervention;
    }

    // =========================================================================
    // US-M5-04 : Récapitulatif (Calcul du total)
    // =========================================================================
    @Override
    public BigDecimal calculerTotalConsultation(Long consultationId) {
        // Règle RG-08 : On utilise BigDecimal pour les montants
        BigDecimal total = BigDecimal.ZERO;

        for (Intervention i : listeInterventions) {
            if (i.getConsultationId().equals(consultationId)) {
                total = total.add(i.getSousTotal());
            }
        }

        return total;
    }

    // =========================================================================
    // MÉTHODES UTILITAIRES (Privées)
    // =========================================================================

    /**
     * Recherche une consultation par son ID dans notre liste temporaire.
     */
    private Consultation trouverConsultationParId(Long id) {
        for (Consultation c : listeConsultations) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }
}