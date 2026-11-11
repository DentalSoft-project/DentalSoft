package ma.dentalsoft.service.impl;

import ma.dentalsoft.config.JdbcConnectionFactory;
import ma.dentalsoft.domain.DossierMedical;
import ma.dentalsoft.domain.Patient;
import ma.dentalsoft.dto.PageResultatDTO;
import ma.dentalsoft.dto.PatientRow;
import ma.dentalsoft.exceptions.DatabaseException;
import ma.dentalsoft.exceptions.DentalSoftException;
import ma.dentalsoft.exceptions.ValidationException;
import ma.dentalsoft.repository.api.DossierMedicalRepository;
import ma.dentalsoft.repository.api.PatientRepository;
import ma.dentalsoft.service.api.PatientService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public class PatientServiceImpl implements PatientService {

    private static final Pattern TELEPHONE = Pattern.compile("^(\\+212|0)[5-7][0-9]{8}$");
    private static final Pattern CIN = Pattern.compile("^[A-Z]{1,2}[0-9]{5,6}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final PatientRepository patientRepository;
    private final DossierMedicalRepository dossierRepository;

    public PatientServiceImpl(PatientRepository patientRepository, DossierMedicalRepository dossierRepository) {
        this.patientRepository = patientRepository;
        this.dossierRepository = dossierRepository;
    }

    @Override
    public Patient creerPatient(Patient patient, Long utilisateurId) throws DentalSoftException {
        normaliser(patient);
        valider(patient);

        try (Connection cn = JdbcConnectionFactory.getConnection()) {
            cn.setAutoCommit(false);
            try {
                if (patient.getCin() != null && patientRepository.existeParCin(cn, patient.getCin())) {
                    throw new ValidationException("Un patient avec ce CIN existe déjà : " + patient.getCin());
                }
                if (patientRepository.existeParIdentite(cn, patient.getNom(), patient.getPrenom(), patient.getDateNaissance())) {
                    throw new ValidationException("Un patient portant ce nom, ce prénom et cette date de naissance existe déjà.");
                }
                patient.setArchive(false);
                patient.setCreePar(utilisateurId);
                patient.setDateCreation(LocalDateTime.now());
                patientRepository.inserer(cn, patient);

                DossierMedical dossier = new DossierMedical(patient.getId());
                dossier.setCreePar(utilisateurId);
                dossier.setDateCreation(LocalDateTime.now());
                dossierRepository.inserer(cn, dossier);

                cn.commit();
                return patient;
            } catch (SQLIntegrityConstraintViolationException e) {
                annuler(cn);
                throw new ValidationException("Un patient avec ce CIN ou ce dossier existe déjà.", e);
            } catch (DentalSoftException e) {
                annuler(cn); throw e;
            } catch (SQLException e) {
                annuler(cn); throw new DatabaseException("Échec de la création du patient et de son dossier.", e);
            } catch (RuntimeException e) {
                annuler(cn); throw new DatabaseException("Erreur inattendue pendant la création du patient.", e);
            } finally {
                restaurerAutoCommit(cn);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Connexion à la base impossible.", e);
        }
    }

    @Override
    public List<PatientRow> rechercher(String motCle) throws DentalSoftException {
        try (Connection cn = JdbcConnectionFactory.getConnection()) {
            return patientRepository.rechercher(cn, motCle);
        } catch (SQLException e) {
            throw new DatabaseException("Recherche des patients impossible.", e);
        }
    }

    // --- TON TRAVAIL (T08) ---
    @Override
    public PageResultatDTO<PatientRow> rechercherPaginer(String motCle, int page, int taillePage) throws DentalSoftException {
        int offset = (page - 1) * taillePage;
        try (Connection cn = JdbcConnectionFactory.getConnection()) {
            int total = patientRepository.compter(cn, motCle);
            List<PatientRow> patients = patientRepository.rechercherPaginer(cn, motCle, taillePage, offset);
            return new PageResultatDTO<>(patients, total, page, taillePage);
        } catch (SQLException e) {
            throw new DatabaseException("Recherche paginée des patients impossible.", e);
        }
    }

    private void valider(Patient patient) throws ValidationException {
        if (patient == null) throw new ValidationException("Le patient est obligatoire.");
        if (estVide(patient.getNom())) throw new ValidationException("Le nom est obligatoire.");
        if (estVide(patient.getPrenom())) throw new ValidationException("Le prénom est obligatoire.");
        if (patient.getDateNaissance() == null) throw new ValidationException("La date de naissance est obligatoire.");
        if (patient.getDateNaissance().isAfter(LocalDate.now())) throw new ValidationException("La date de naissance ne peut pas être dans le futur.");
        if (patient.getSexe() == null) throw new ValidationException("Le sexe est obligatoire.");
        if (estVide(patient.getTelephone())) throw new ValidationException("Le téléphone est obligatoire.");
        if (!TELEPHONE.matcher(patient.getTelephone()).matches()) throw new ValidationException("Téléphone invalide.");
        if (patient.getCin() != null && !CIN.matcher(patient.getCin()).matches()) throw new ValidationException("CIN invalide.");
        if (patient.getEmail() != null && !EMAIL.matcher(patient.getEmail()).matches()) throw new ValidationException("Adresse e-mail invalide.");
    }

    private void normaliser(Patient patient) {
        if (patient == null) return;
        patient.setNom(trim(patient.getNom()));
        patient.setPrenom(trim(patient.getPrenom()));
        patient.setAdresse(trim(patient.getAdresse()));
        if (patient.getCin() != null) {
            String cin = patient.getCin().trim().toUpperCase();
            patient.setCin(cin.isEmpty() ? null : cin);
        }
        if (patient.getEmail() != null) {
            String email = patient.getEmail().trim();
            patient.setEmail(email.isEmpty() ? null : email);
        }
        if (patient.getTelephone() != null) {
            patient.setTelephone(patient.getTelephone().replaceAll("[\\s.()-]", ""));
        }
    }

    private static String trim(String valeur) { return valeur == null ? null : valeur.trim(); }
    private static boolean estVide(String valeur) { return valeur == null || valeur.isBlank(); }
    private static void annuler(Connection cn) { try { cn.rollback(); } catch (SQLException ignored) {} }
    private static void restaurerAutoCommit(Connection cn) { try { cn.setAutoCommit(true); } catch (SQLException ignored) {} }
}