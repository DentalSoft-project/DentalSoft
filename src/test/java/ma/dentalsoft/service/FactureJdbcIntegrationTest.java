package ma.dentalsoft.service;

import ma.dentalsoft.domain.Facture;
import ma.dentalsoft.domain.Permission;
import ma.dentalsoft.domain.Role;
import ma.dentalsoft.domain.Utilisateur;
import ma.dentalsoft.exceptions.DatabaseException;
import ma.dentalsoft.exceptions.ValidationException;
import ma.dentalsoft.repository.impl.jdbc_mysql.JdbcFactureRepository;
import ma.dentalsoft.service.impl.ConsultationServiceImpl;
import ma.dentalsoft.service.impl.FactureServiceImpl;
import ma.dentalsoft.session.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Opt-in uniquement, après install.sql et seed/dev_seed.sql sur MySQL jetable.
 * -Ddentalsoft.test.jdbc.url=jdbc:mysql://127.0.0.1:PORT/dentalsoftbd
 * -Ddentalsoft.test.jdbc.disposable=true
 * Instance temporaire initialisée sans mot de passe ; aucune configuration applicative lue.
 * Les fixtures sont conservées jusqu'à l'abandon de cette instance, jamais nettoyées en base partagée.
 */
class FactureJdbcIntegrationTest {
    private static String url;
    private long dossierId;
    private long consultationId;
    private long acteId;
    private long medecinId;
    private long auteurId;

    @BeforeAll
    static void exigerBaseJetable() {
        url = System.getProperty("dentalsoft.test.jdbc.url");
        assumeTrue(url != null, "Test JDBC désactivé : URL jetable explicite requise.");
        assertEquals("true", System.getProperty("dentalsoft.test.jdbc.disposable"));
        assertTrue(url.matches("jdbc:mysql://127\\.0\\.0\\.1:[0-9]+/dentalsoftbd"),
                "Seule une instance locale jetable sur port explicite est autorisée.");
    }

    private static Connection connexion() throws SQLException {
        return DriverManager.getConnection(url, "root", "");
    }

    @BeforeEach
    void preparer() throws Exception {
        try (var cn = connexion()) {
            medecinId = scalaire(cn, "SELECT id_utilisateur FROM utilisateurs WHERE login = 'dr.alami'");
            auteurId = scalaire(cn, "SELECT id_utilisateur FROM utilisateurs WHERE login = 'secretaire'");
            long patientId = inserer(cn, """
                    INSERT INTO patient (nom, prenom, date_naissance, sexe, telephone)
                    VALUES ('Fictif KAN34', 'Test', '2000-01-01', 'F', '0600000000')
                    """);
            dossierId = inserer(cn, "INSERT INTO dossier_medical (patient_id) VALUES (?)", patientId);
            consultationId = inserer(cn, """
                    INSERT INTO consultation (dossier_id, medecin_id, statut) VALUES (?, ?, 'TERMINEE')
                    """, dossierId, medecinId);
            acteId = inserer(cn, """
                    INSERT INTO acte (libelle, categorie, prix_base) VALUES ('Acte fictif KAN34', 'TEST', 999.00)
                    """);
        }
        ouvrirSession("SECRETAIRE", Set.of(Permission.GERER_CAISSE));
    }

    private void ouvrirSession(String role, Set<Permission> permissions) {
        var utilisateur = new Utilisateur(auteurId, "Fictif", "Finance", "test", "inutilise", true,
                new Role(1L, role, role, permissions)) { };
        SessionManager.getInstance().ouvrir(utilisateur);
    }

    @AfterEach
    void fermerSession() {
        SessionManager.getInstance().fermer();
    }

    private FactureServiceImpl service(JdbcFactureRepository repository) {
        // Le service mémoire n'est jamais utilisé pour générer le brouillon SQL.
        return new FactureServiceImpl(new ConsultationServiceImpl(), repository) {
            @Override
            protected Connection ouvrirConnexion() throws SQLException {
                return connexion();
            }
        };
    }

    private FactureServiceImpl service() { return service(new JdbcFactureRepository()); }

    private String numero() { return "TEST-" + UUID.randomUUID(); }

    private void intervention(int quantite, String prix) throws SQLException {
        try (var cn = connexion()) {
            inserer(cn, """
                    INSERT INTO intervention
                        (consultation_id, acte_id, libelle_acte_snapshot, quantite, prix_unitaire)
                    VALUES (?, ?, 'Acte fictif appliqué', ?, ?)
                    """, consultationId, acteId, quantite, new BigDecimal(prix));
        }
    }

    @Test
    void persiste850Depuis400Et450AvecLeBonDossierEtAuteur() throws Exception {
        intervention(1, "400.00");
        intervention(1, "450.00");
        Facture facture = service().genererBrouillon(consultationId, numero());
        assertNotNull(facture.getId());
        assertEquals(new BigDecimal("850.00"), facture.getTotal());
        assertEquals("BROUILLON", facture.getStatut());
        try (var cn = connexion()) {
            var relue = new JdbcFactureRepository().trouverParConsultation(cn, consultationId).orElseThrow();
            assertEquals(facture.getId(), relue.getId());
            assertEquals(facture.getNumero(), relue.getNumero());
            assertEquals(new BigDecimal("850.00"), relue.getTotal());
            assertEquals(auteurId, relue.getCreePar());
            assertEquals(dossierId, scalaire(cn,
                    "SELECT id_dossier FROM situation_financiere WHERE id = ?", relue.getSituationFinanciereId()));
            assertEquals(0, scalaire(cn,
                    "SELECT total_actes + total_paye + credit FROM situation_financiere WHERE id_dossier = ?", dossierId));
        }
    }

    @Test
    void tientCompteDesQuantitesSansUtiliserLePrixDuCatalogue() throws Exception {
        intervention(3, "0.10");
        ouvrirSession("MEDECIN", Set.of(Permission.GERER_CAISSE));
        assertEquals(new BigDecimal("0.30"), service().genererBrouillon(consultationId, numero()).getTotal());
    }

    @Test
    void refuseConsultationsAbsentesOuNonValidees() throws Exception {
        intervention(1, "400.00");
        assertThrows(ValidationException.class, () -> service().genererBrouillon(Long.MAX_VALUE, numero()));
        for (String statut : new String[]{"EN_COURS", "ANNULEE"}) {
            try (var cn = connexion(); var ps = cn.prepareStatement("UPDATE consultation SET statut = ? WHERE id = ?")) {
                ps.setString(1, statut);
                ps.setLong(2, consultationId);
                ps.executeUpdate();
            }
            assertThrows(ValidationException.class, () -> service().genererBrouillon(consultationId, numero()));
        }
        verifierAucuneEcriture();
    }

    @Test
    void refuseConsultationVideMaisAccepteUneInterventionGratuite() throws Exception {
        assertThrows(ValidationException.class, () -> service().genererBrouillon(consultationId, numero()));
        verifierAucuneEcriture();
        intervention(1, "0.00");
        assertEquals(new BigDecimal("0.00"), service().genererBrouillon(consultationId, numero()).getTotal());
    }

    @Test
    void refuseDoublonConsultationEtNumeroAvecRollbackDeLaNouvelleSf() throws Exception {
        intervention(1, "850.00");
        String numero = numero();
        service().genererBrouillon(consultationId, numero);
        assertThrows(ValidationException.class, () -> service().genererBrouillon(consultationId, numero()));
        // Autre dossier : sa SF ne doit pas survivre à un échec sur le numéro unique.
        preparer();
        intervention(1, "100.00");
        assertThrows(ValidationException.class, () -> service().genererBrouillon(consultationId, numero));
        verifierAucuneEcriture();
    }

    @Test
    void annuleFactureEtSfSiErreurApresInsertion() throws Exception {
        intervention(1, "850.00");
        var repository = new JdbcFactureRepository() {
            @Override
            public Facture inserer(Connection cn, Facture facture) throws SQLException {
                super.inserer(cn, facture);
                throw new SQLException("Erreur fictive après écriture");
            }
        };
        assertThrows(DatabaseException.class, () -> service(repository).genererBrouillon(consultationId, numero()));
        verifierAucuneEcriture();
    }

    @Test
    void refuseSansSessionOuSansPermissionEtValideLesParametres() throws Exception {
        SessionManager.getInstance().fermer();
        assertThrows(SecurityException.class, () -> service().genererBrouillon(consultationId, numero()));
        ouvrirSession("SECRETAIRE", Set.of());
        assertThrows(SecurityException.class, () -> service().genererBrouillon(consultationId, numero()));
        ouvrirSession("ADMIN", Set.of(Permission.GERER_CAISSE));
        assertThrows(SecurityException.class, () -> service().genererBrouillon(consultationId, numero()));
        ouvrirSession("SECRETAIRE", Set.of(Permission.GERER_CAISSE));
        assertThrows(ValidationException.class, () -> service().genererBrouillon(null, numero()));
        assertThrows(ValidationException.class, () -> service().genererBrouillon(0L, numero()));
        for (String numero : new String[]{null, " ", "x".repeat(51)}) {
            assertThrows(ValidationException.class, () -> service().genererBrouillon(consultationId, numero));
        }
        verifierAucuneEcriture();
    }

    @Test
    void conserveLaSfExistante() throws Exception {
        intervention(1, "400.00");
        long sfId;
        try (var cn = connexion()) {
            sfId = inserer(cn, """
                    INSERT INTO situation_financiere (id_dossier, total_actes, total_paye, credit)
                    VALUES (?, 100.00, 30.00, 70.00)
                    """, dossierId);
        }
        assertEquals(sfId, service().genererBrouillon(consultationId, numero()).getSituationFinanciereId());
        try (var cn = connexion()) {
            assertEquals(70, scalaire(cn, "SELECT credit FROM situation_financiere WHERE id = ?", sfId));
        }
    }

    @Test
    void deuxGenerationsConcurrentesNeCreentQuUneFacture() throws Exception {
        intervention(1, "850.00");
        var depart = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Boolean> tentative = () -> {
                assertTrue(depart.await(5, TimeUnit.SECONDS));
                try {
                    service().genererBrouillon(consultationId, numero());
                    return true;
                } catch (ValidationException attendu) {
                    return false;
                }
            };
            var a = pool.submit(tentative);
            var b = pool.submit(tentative);
            depart.countDown();
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            try (var cn = connexion()) {
                assertEquals(1, scalaire(cn, "SELECT COUNT(*) FROM facture WHERE consultation_id = ?", consultationId));
            }
        } finally {
            pool.shutdownNow();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private void verifierAucuneEcriture() throws SQLException {
        try (var cn = connexion()) {
            assertEquals(0, scalaire(cn, "SELECT COUNT(*) FROM facture WHERE consultation_id = ?", consultationId));
            assertEquals(0, scalaire(cn, "SELECT COUNT(*) FROM situation_financiere WHERE id_dossier = ?", dossierId));
        }
    }

    private static long inserer(Connection cn, String sql, Object... valeurs) throws SQLException {
        try (var ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valeurs.length; i++) ps.setObject(i + 1, valeurs[i]);
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                assertTrue(keys.next());
                return keys.getLong(1);
            }
        }
    }

    private static long scalaire(Connection cn, String sql, Object... valeurs) throws SQLException {
        try (var ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < valeurs.length; i++) ps.setObject(i + 1, valeurs[i]);
            try (var rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getLong(1);
            }
        }
    }
}
