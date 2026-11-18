package ma.dentalsoft.repository.api;

import ma.dentalsoft.domain.DossierMedical;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface DossierMedicalRepository {

    DossierMedical inserer(Connection cn, DossierMedical dossier) throws SQLException;

    Optional<DossierMedical> trouverParPatientId(Connection cn, long patientId)
            throws SQLException;
}