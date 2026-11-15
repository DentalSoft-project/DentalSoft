package ma.dentalsoft.repository.impl.jdbc_mysql;

import ma.dentalsoft.domain.Facture;
import ma.dentalsoft.dto.ConsultationMontantsDTO;
import ma.dentalsoft.repository.api.FactureRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;

/** Lecture financière des tables M5 et écriture M6 dans la même transaction JDBC. */
public class JdbcFactureRepository implements FactureRepository {

    @Override
    public Optional<ConsultationMontantsDTO> verrouillerConsultation(Connection cn, long id)
            throws SQLException {
        if (cn.getAutoCommit()) {
            throw new SQLException("La lecture verrouillée exige une transaction.");
        }
        try (var ps = cn.prepareStatement(
                "SELECT id, dossier_id, statut FROM consultation WHERE id = ? FOR UPDATE")) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                long dossierId = rs.getLong("dossier_id");
                String statut = rs.getString("statut");
                var lignes = new ArrayList<ConsultationMontantsDTO.Ligne>();
                try (var interventions = cn.prepareStatement("""
                        SELECT quantite, prix_unitaire FROM intervention
                        WHERE consultation_id = ? ORDER BY id FOR UPDATE
                        """)) {
                    interventions.setLong(1, id);
                    try (var details = interventions.executeQuery()) {
                        while (details.next()) {
                            lignes.add(new ConsultationMontantsDTO.Ligne(
                                    details.getInt("quantite"), details.getBigDecimal("prix_unitaire")));
                        }
                    }
                }
                return Optional.of(new ConsultationMontantsDTO(id, dossierId, statut, lignes));
            }
        }
    }

    @Override
    public Optional<Facture> trouverParConsultation(Connection cn, long consultationId)
            throws SQLException {
        try (var ps = cn.prepareStatement("""
                SELECT id, consultation_id, id_sf, numero, date_facture, total, statut,
                       date_creation, cree_par
                FROM facture WHERE consultation_id = ?
                """)) {
            ps.setLong(1, consultationId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                var facture = new Facture(rs.getLong("consultation_id"), rs.getLong("id_sf"),
                        rs.getString("numero"), rs.getTimestamp("date_facture").toLocalDateTime(),
                        rs.getBigDecimal("total"), rs.getString("statut"));
                facture.setId(rs.getLong("id"));
                facture.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                long auteur = rs.getLong("cree_par");
                facture.setCreePar(rs.wasNull() ? null : auteur);
                return Optional.of(facture);
            }
        }
    }

    @Override
    public long obtenirSituationFinanciere(Connection cn, long dossierId, long auteurId)
            throws SQLException {
        // L'unicité du dossier sérialise les créations concurrentes de la même SF.
        // Les montants existants sont conservés ; aucun agrégat pour un brouillon.
        try (var ps = cn.prepareStatement("""
                INSERT INTO situation_financiere (id_dossier, cree_par) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)
                """)) {
            ps.setLong(1, dossierId);
            ps.setLong(2, auteurId);
            ps.executeUpdate();
        }
        try (var ps = cn.prepareStatement(
                "SELECT id FROM situation_financiere WHERE id_dossier = ? FOR UPDATE")) {
            ps.setLong(1, dossierId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Situation financière introuvable après création.");
                return rs.getLong(1);
            }
        }
    }

    @Override
    public Facture inserer(Connection cn, Facture facture) throws SQLException {
        try (var ps = cn.prepareStatement("""
                INSERT INTO facture (consultation_id, id_sf, numero, date_facture, total, statut, cree_par)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, facture.getConsultationId());
            ps.setLong(2, facture.getSituationFinanciereId());
            ps.setString(3, facture.getNumero());
            ps.setTimestamp(4, Timestamp.valueOf(facture.getDateFacture()));
            ps.setBigDecimal(5, facture.getTotal());
            ps.setString(6, facture.getStatut());
            ps.setLong(7, facture.getCreePar());
            if (ps.executeUpdate() != 1) throw new SQLException("Échec de l'insertion de la facture.");
            try (var keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Identifiant de facture non retourné.");
                facture.setId(keys.getLong(1));
            }
        }
        return facture;
    }
}
