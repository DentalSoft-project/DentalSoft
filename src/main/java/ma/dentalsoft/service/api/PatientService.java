package ma.dentalsoft.service.api;

import ma.dentalsoft.domain.Patient;
import ma.dentalsoft.dto.PatientRow;
import ma.dentalsoft.exceptions.DentalSoftException;

import java.util.List;

public interface PatientService {

    /**
     * Crée le patient et son dossier médical dans une seule transaction.
     * En cas d'échec, aucune des deux lignes n'est conservée (RG-01).
     */
    Patient creerPatient(Patient patient, Long utilisateurId) throws DentalSoftException;

    /**
     * Recherche les patients actifs par nom, prénom, téléphone ou identifiant.
     */
    List<PatientRow> rechercher(String motCle) throws DentalSoftException;
}