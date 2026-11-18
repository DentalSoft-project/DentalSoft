package ma.dentalsoft.repository.jdbc;

import ma.dentalsoft.domain.Consultation;
import ma.dentalsoft.domain.Intervention;
import ma.dentalsoft.dto.DossierMedicalDTO;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Transforme les lignes JDBC en objets du module métier.
 */
public final class RowMappers {

    private RowMappers() {
    }

    public static DossierMedicalDTO toDossierMedical(ResultSet resultSet) throws SQLException {
        return new DossierMedicalDTO(
                resultSet.getLong("id"),
                resultSet.getLong("patient_id"),
                resultSet.getString("observations_cliniques")
        );
    }

    public static Consultation toConsultation(ResultSet resultSet) throws SQLException {
        var consultation = new Consultation(
                resultSet.getLong("dossier_id"),
                resultSet.getLong("medecin_id"),
                getNullableLong(resultSet, "rdv_id")
        );
        consultation.setId(resultSet.getLong("id"));
        var dateConsultation = resultSet.getTimestamp("date_consultation");
        consultation.setDateConsultation(
                dateConsultation == null ? null : dateConsultation.toLocalDateTime()
        );
        consultation.setObservation(resultSet.getString("observation"));
        consultation.setStatut(resultSet.getString("statut"));
        return consultation;
    }

    public static Intervention toIntervention(ResultSet resultSet) throws SQLException {
        var intervention = new Intervention(
                resultSet.getLong("consultation_id"),
                resultSet.getLong("acte_id"),
                resultSet.getString("libelle_acte_snapshot"),
                resultSet.getString("dent"),
                resultSet.getInt("quantite"),
                resultSet.getBigDecimal("prix_unitaire")
        );
        intervention.setId(resultSet.getLong("id"));
        return intervention;
    }

    private static Long getNullableLong(ResultSet resultSet, String columnName) throws SQLException {
        var value = resultSet.getLong(columnName);
        return resultSet.wasNull() ? null : value;
    }
}
