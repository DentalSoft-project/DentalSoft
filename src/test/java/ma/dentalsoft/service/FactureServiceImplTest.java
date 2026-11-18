package ma.dentalsoft.service;

import ma.dentalsoft.domain.Consultation;
import ma.dentalsoft.domain.Intervention;
import ma.dentalsoft.domain.Permission;
import ma.dentalsoft.domain.Role;
import ma.dentalsoft.domain.Utilisateur;
import ma.dentalsoft.security.SessionManager;
import ma.dentalsoft.service.api.ConsultationService;
import ma.dentalsoft.service.impl.ConsultationServiceImpl;
import ma.dentalsoft.service.impl.FactureServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Tests en mémoire uniquement : aucune connexion JDBC ni donnée de base modifiée. */
class FactureServiceImplTest {

    @AfterEach
    void deconnecter() {
        SessionManager.logout();
        ma.dentalsoft.session.SessionManager.getInstance().fermer();
    }

    private void connecter(String role) {
        SessionManager.login("test", role); // Nécessaire uniquement au M5 mémoire existant.
        var utilisateur = new Utilisateur(1L, "Test", "Finance", "test", "inutilise", true,
                new Role(1L, role, role, Set.of(Permission.GERER_CAISSE))) { };
        ma.dentalsoft.session.SessionManager.getInstance().ouvrir(utilisateur);
    }

    @Test
    void derive850DepuisLesInterventionsM5PourLaSecretaire() {
        connecter("MEDECIN");
        ConsultationService m5 = new ConsultationServiceImpl();
        Consultation consultation = m5.demarrerConsultation(7L, null);
        m5.ajouterIntervention(consultation.getId(), 1L, "Acte fictif A", null,
                1, new BigDecimal("400.00"));
        m5.ajouterIntervention(consultation.getId(), 2L, "Acte fictif B", null,
                1, new BigDecimal("450.00"));

        connecter("SECRETAIRE");
        var apercu = new FactureServiceImpl(m5).previsualiserFacture(consultation.getId());

        assertEquals(consultation.getId(), apercu.consultationId());
        assertEquals(new BigDecimal("850.00"), apercu.total());
        assertEquals("EN_COURS", consultation.getStatut());
    }

    @Test
    void respecteLaQuantiteEtLaPrecisionDecimalePourLeMedecin() {
        connecter("MEDECIN");
        ConsultationService m5 = new ConsultationServiceImpl();
        Consultation consultation = m5.demarrerConsultation(7L, null);
        m5.ajouterIntervention(consultation.getId(), 1L, "Acte fictif", null,
                3, new BigDecimal("0.10"));

        var apercu = new FactureServiceImpl(m5).previsualiserFacture(consultation.getId());

        assertEquals(new BigDecimal("0.30"), apercu.total());
    }

    @Test
    void refuseLesAccesNonAutorisesAvantLaLectureM5() {
        ConsultationService m5 = new ConsultationServiceImpl() {
            @Override
            public BigDecimal calculerTotalConsultation(Long id) {
                fail("M5 ne doit pas être consulté sans autorisation.");
                return BigDecimal.ZERO;
            }
        };
        var service = new FactureServiceImpl(m5);
        SessionManager.logout();
        ma.dentalsoft.session.SessionManager.getInstance().fermer();
        assertThrows(SecurityException.class, () -> service.previsualiserFacture(1L));
        connecter("ADMIN");
        assertThrows(SecurityException.class, () -> service.previsualiserFacture(1L));
        ma.dentalsoft.session.SessionManager.getInstance().fermer();
        SessionManager.login("legacy-only", "SECRETAIRE");
        assertThrows(SecurityException.class, () -> service.previsualiserFacture(1L));
    }

    @Test
    void refuseLesIdentifiantsInvalides() {
        connecter("SECRETAIRE");
        var service = new FactureServiceImpl(new ConsultationServiceImpl());
        assertThrows(IllegalArgumentException.class, () -> service.previsualiserFacture(null));
        assertThrows(IllegalArgumentException.class, () -> service.previsualiserFacture(0L));
        assertThrows(IllegalArgumentException.class, () -> service.previsualiserFacture(-1L));
    }

    @Test
    void refuseLesMontantsInvalidesSansLesArrondirSilencieusement() {
        connecter("SECRETAIRE");
        for (BigDecimal total : new BigDecimal[]{null, new BigDecimal("-0.01"),
                new BigDecimal("0.001"), new BigDecimal("10000000000.00")}) {
            ConsultationService m5 = new ConsultationServiceImpl() {
                @Override
                public BigDecimal calculerTotalConsultation(Long id) {
                    return total;
                }
            };
            var service = new FactureServiceImpl(m5);
            assertThrows(IllegalStateException.class, () -> service.previsualiserFacture(1L));
        }
    }

    @Test
    void unMontantNulResteUnApercuSansPreuveDexistence() {
        connecter("SECRETAIRE");
        var service = new FactureServiceImpl(new ConsultationServiceImpl());

        // Limite actuelle M5 : une consultation inconnue retourne aussi zéro.
        assertEquals(new BigDecimal("0.00"), service.previsualiserFacture(999L).total());
    }
}
