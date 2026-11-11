package ma.dentalsoft.service;

import ma.dentalsoft.config.JdbcConnectionFactory;
import ma.dentalsoft.domain.DossierMedical;
import ma.dentalsoft.domain.Patient;
import ma.dentalsoft.domain.Sexe;
import ma.dentalsoft.exceptions.DatabaseException;
import ma.dentalsoft.exceptions.ValidationException;
import ma.dentalsoft.repository.api.DossierMedicalRepository;
import ma.dentalsoft.repository.impl.jdbc_mysql.JdbcDossierMedicalRepository;
import ma.dentalsoft.repository.impl.jdbc_mysql.JdbcPatientRepository;
import ma.dentalsoft.service.api.PatientService;
import ma.dentalsoft.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests d'intégration sur la base dentalsoftbd.
 * Les données de test utilisent des CIN en ZZ9... et sont nettoyées
 * avant et après chaque test.
 */
class PatientServiceImplTest {

    private PatientService service;

    @BeforeEach
    void setUp() {
        service = new PatientServiceImpl(
                new JdbcPatientRepository(),
                new JdbcDossierMedicalRepository());
        nettoyerDonneesDeTest();
    }

    @AfterEach
    void tearDown() {
        nettoyerDonneesDeTest();
    }

    @Test
    @DisplayName("Création : le patient et son dossier sont enregistrés ensemble")
    void creePatientEtDossier() throws Exception {
        Patient patient = patient("ZZ900001", "Test", "Nom",
                LocalDate.of(1990, 5, 12), Sexe.M, "+212699000001");

        Patient cree = service.creerPatient(patient, 1L);

        assertNotNull(cree.getId(), "L'identifiant du patient doit être généré");
        try (Connection cn = JdbcConnectionFactory.getConnection()) {
            assertEquals(1, compter(cn,
                    "SELECT COUNT(*) FROM patient WHERE cin = ?", "ZZ900001"));
            assertEquals(1, compter(cn,
                    "SELECT COUNT(*) FROM dossier_medical WHERE patient_id = ?", cree.getId()));
        }
    }

    @Test
    @DisplayName("Doublon : un CIN déjà présent est refusé")
    void refuseCinEnDouble() throws Exception {
        service.creerPatient(patient("ZZ900002", "Premier", "Patient",
                LocalDate.of(1980, 1, 1), Sexe.M, "+212699000002"), 1L);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                service.creerPatient(patient("ZZ900002", "Second", "Patient",
                        LocalDate.of(1985, 2, 2), Sexe.F, "+212699000012"), 1L));

        assertTrue(exception.getMessage().contains("CIN"));
    }

    @Test
    @DisplayName("Atomicité : si le dossier échoue, le patient est annulé")
    void annulePatientSiDossierEchoue() throws Exception {
        DossierMedicalRepository dossierEnEchec = new DossierMedicalRepository() {
            @Override
            public DossierMedical inserer(Connection cn, DossierMedical dossier)
                    throws SQLException {
                throw new SQLException("Échec simulé de l'insertion du dossier");
            }

            @Override
            public Optional<DossierMedical> trouverParPatientId(Connection cn, long patientId) {
                return Optional.empty();
            }
        };

        PatientService serviceEnEchec = new PatientServiceImpl(
                new JdbcPatientRepository(), dossierEnEchec);

        assertThrows(DatabaseException.class, () ->
                serviceEnEchec.creerPatient(patient("ZZ900003", "Rollback", "Test",
                        LocalDate.of(1991, 1, 1), Sexe.F, "+212699000003"), 1L));

        try (Connection cn = JdbcConnectionFactory.getConnection()) {
            assertEquals(0, compter(cn,
                            "SELECT COUNT(*) FROM patient WHERE cin = ?", "ZZ900003"),
                    "Le patient ne doit pas rester en base après le rollback");
        }
    }

    @Test
    @DisplayName("Recherche : par nom et par téléphone")
    void rechercheParNomEtTelephone() throws Exception {
        service.creerPatient(patient("ZZ900004", "Cherchable", "Unique",
                LocalDate.of(1985, 3, 3), Sexe.M, "+212699000004"), 1L);

        assertFalse(service.rechercher("Cherchable").isEmpty());
        assertFalse(service.rechercher("699000004").isEmpty());
        assertTrue(service.rechercher("IntrouvableXYZ").isEmpty());
    }

    private static Patient patient(String cin, String nom, String prenom,
                                   LocalDate naissance, Sexe sexe, String telephone) {
        Patient patient = new Patient();
        patient.setCin(cin);
        patient.setNom(nom);
        patient.setPrenom(prenom);
        patient.setDateNaissance(naissance);
        patient.setSexe(sexe);
        patient.setTelephone(telephone);
        patient.setEmail(nom.toLowerCase() + "." + prenom.toLowerCase() + "@example.ma");
        patient.setAdresse("Adresse de test");
        return patient;
    }

    private static int compter(Connection cn, String sql, Object parametre)
            throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (parametre instanceof Long valeur) {
                ps.setLong(1, valeur);
            } else {
                ps.setString(1, String.valueOf(parametre));
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void nettoyerDonneesDeTest() {
        try (Connection cn = JdbcConnectionFactory.getConnection()) {
            cn.setAutoCommit(false);
            try (PreparedStatement dossiers = cn.prepareStatement("""
                         DELETE d FROM dossier_medical d
                         JOIN patient p ON p.id = d.patient_id
                         WHERE p.cin LIKE 'ZZ9%'
                         """);
                 PreparedStatement patients = cn.prepareStatement("""
                         DELETE FROM patient WHERE cin LIKE 'ZZ9%'
                         """)) {
                dossiers.executeUpdate();
                patients.executeUpdate();
            }
            cn.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Nettoyage des données de test impossible", e);
        }
    }
}