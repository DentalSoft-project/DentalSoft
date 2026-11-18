package ma.dentalsoft.repository.impl.jdbc_mysql;

import ma.dentalsoft.domain.DossierMedical;
import ma.dentalsoft.repository.api.DossierMedicalRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Optional;

/**
 * Implémentation MySQL du contrat DossierMedicalRepository.
 * L'unicité (patient_id UNIQUE) est garantie par la base : RG-01.
 */
public class JdbcDossierMedicalRepository implements DossierMedicalRepository {

    private static final String SQL_INSERT = """
            INSERT INTO dossier_medical (patient_id, cree_par)
            VALUES (?, ?)
            """;

    private static final String SQL_PAR_PATIENT = """
            SELECT id, patient_id, date_creation, date_derniere_modification,
                   cree_par, modifie_par
            FROM dossier_medical
            WHERE patient_id = ?
            """;

    @Override
    public DossierMedical inserer(Connection cn, DossierMedical dossier) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, dossier.getPatientId());
            if (dossier.getCreePar() == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, dossier.getCreePar());
            }

            int lignes = ps.executeUpdate();
            if (lignes != 1) {
                throw new SQLException("Insertion du dossier médical : " + lignes + " ligne(s)");
            }

            try (ResultSet cles = ps.getGeneratedKeys()) {
                if (cles.next()) {
                    dossier.setId(cles.getLong(1));
                }
            }
        }
        return dossier;
    }

    @Override
    public Optional<DossierMedical> trouverParPatientId(Connection cn, long patientId)
            throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_PAR_PATIENT)) {
            ps.setLong(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                DossierMedical dossier = new DossierMedical(rs.getLong("patient_id"));
                dossier.setId(rs.getLong("id"));

                Timestamp creation = rs.getTimestamp("date_creation");
                if (creation != null) {
                    dossier.setDateCreation(creation.toLocalDateTime());
                }

                Timestamp modification = rs.getTimestamp("date_derniere_modification");
                if (modification != null) {
                    dossier.setDateDerniereModification(modification.toLocalDateTime());
                }

                long creePar = rs.getLong("cree_par");
                dossier.setCreePar(rs.wasNull() ? null : creePar);

                long modifiePar = rs.getLong("modifie_par");
                dossier.setModifiePar(rs.wasNull() ? null : modifiePar);

                return Optional.of(dossier);
            }
        }
    }
}