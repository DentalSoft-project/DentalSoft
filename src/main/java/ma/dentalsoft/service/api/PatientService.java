package ma.dentalsoft.service.api;

import ma.dentalsoft.domain.Patient;
import ma.dentalsoft.dto.PageResultatDTO;
import ma.dentalsoft.dto.PatientRow;
import ma.dentalsoft.exceptions.DentalSoftException;

import java.util.List;

public interface PatientService {

    Patient creerPatient(Patient patient, Long utilisateurId) throws DentalSoftException;

    List<PatientRow> rechercher(String motCle) throws DentalSoftException;

    // --- TON TRAVAIL (T08) ---
    PageResultatDTO<PatientRow> rechercherPaginer(String motCle, int page, int taillePage) throws DentalSoftException;
}