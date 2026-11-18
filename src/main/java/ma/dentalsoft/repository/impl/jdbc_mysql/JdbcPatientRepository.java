package ma.dentalsoft.repository.impl.jdbc_mysql;

import ma.dentalsoft.domain.Patient;
import ma.dentalsoft.domain.Sexe;
import ma.dentalsoft.dto.PatientRow;
import ma.dentalsoft.repository.api.PatientRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcPatientRepository implements PatientRepository {

    private static final String SQL_INSERT = """
            INSERT INTO patient
                (nom, prenom, date_naissance, sexe, cin, telephone, email, adresse,
                 id_assurance, archive, cree_par)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_EXISTE_CIN = "SELECT 1 FROM patient WHERE cin = ? LIMIT 1";

    private static final String SQL_EXISTE_IDENTITE = """
            SELECT 1 FROM patient WHERE nom = ? AND prenom = ? AND date_naissance = ? LIMIT 1
            """;

    private static final String SQL_RECHERCHE = """
            SELECT p.id, p.nom, p.prenom, p.date_naissance, p.sexe, p.cin,
                   p.telephone, p.archive, a.libelle AS assurance
            FROM patient p
            LEFT JOIN assurance a ON a.id = p.id_assurance
            WHERE p.archive = FALSE
            """;

    @Override
    public Patient inserer(Connection cn, Patient patient) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getNom());
            ps.setString(2, patient.getPrenom());
            ps.setDate(3, java.sql.Date.valueOf(patient.getDateNaissance()));
            ps.setString(4, patient.getSexe().name());
            setNullableString(ps, 5, patient.getCin());
            ps.setString(6, patient.getTelephone());
            setNullableString(ps, 7, patient.getEmail());
            setNullableString(ps, 8, patient.getAdresse());
            setNullableLong(ps, 9, patient.getIdAssurance());
            ps.setBoolean(10, patient.isArchive());
            setNullableLong(ps, 11, patient.getCreePar());

            int lignes = ps.executeUpdate();
            if (lignes != 1) throw new SQLException("Insertion du patient : " + lignes + " ligne(s) affectée(s)");

            try (ResultSet cles = ps.getGeneratedKeys()) {
                if (cles.next()) patient.setId(cles.getLong(1));
            }
        }
        return patient;
    }

    @Override
    public boolean existeParCin(Connection cn, String cin) throws SQLException {
        if (cin == null || cin.isBlank()) return false;
        try (PreparedStatement ps = cn.prepareStatement(SQL_EXISTE_CIN)) {
            ps.setString(1, cin.trim());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    @Override
    public boolean existeParIdentite(Connection cn, String nom, String prenom, LocalDate dateNaissance) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_EXISTE_IDENTITE)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setDate(3, java.sql.Date.valueOf(dateNaissance));
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    @Override
    public List<PatientRow> rechercher(Connection cn, String motCle) throws SQLException {
        boolean filtre = motCle != null && !motCle.isBlank();
        String sql = SQL_RECHERCHE + (filtre ? " AND (p.nom LIKE ? OR p.prenom LIKE ? OR p.telephone LIKE ? OR CAST(p.id AS CHAR) = ?) " : "") + " ORDER BY p.nom, p.prenom";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (filtre) {
                String valeur = motCle.trim();
                String like = "%" + valeur + "%";
                ps.setString(1, like); ps.setString(2, like); ps.setString(3, like); ps.setString(4, valeur);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<PatientRow> lignes = new ArrayList<>();
                while (rs.next()) {
                    lignes.add(new PatientRow(rs.getLong("id"), rs.getString("nom"), rs.getString("prenom"),
                            rs.getDate("date_naissance").toLocalDate(), Sexe.valueOf(rs.getString("sexe")),
                            rs.getString("cin"), rs.getString("telephone"), rs.getString("assurance"), rs.getBoolean("archive")));
                }
                return lignes;
            }
        }
    }

    // --- TON TRAVAIL (T08) ---
    @Override
    public List<PatientRow> rechercherPaginer(Connection cn, String motCle, int limite, int offset) throws SQLException {
        boolean filtre = motCle != null && !motCle.isBlank();
        String sql = SQL_RECHERCHE + (filtre ? " AND (p.nom LIKE ? OR p.prenom LIKE ? OR p.telephone LIKE ? OR CAST(p.id AS CHAR) = ?) " : "") + " ORDER BY p.nom, p.prenom LIMIT ? OFFSET ?";

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            int index = 1;
            if (filtre) {
                String valeur = motCle.trim();
                String like = "%" + valeur + "%";
                ps.setString(index++, like); ps.setString(index++, like); ps.setString(index++, like); ps.setString(index++, valeur);
            }
            ps.setInt(index++, limite);
            ps.setInt(index, offset);

            try (ResultSet rs = ps.executeQuery()) {
                List<PatientRow> lignes = new ArrayList<>();
                while (rs.next()) {
                    lignes.add(new PatientRow(rs.getLong("id"), rs.getString("nom"), rs.getString("prenom"),
                            rs.getDate("date_naissance").toLocalDate(), Sexe.valueOf(rs.getString("sexe")),
                            rs.getString("cin"), rs.getString("telephone"), rs.getString("assurance"), rs.getBoolean("archive")));
                }
                return lignes;
            }
        }
    }

    @Override
    public int compter(Connection cn, String motCle) throws SQLException {
        boolean filtre = motCle != null && !motCle.isBlank();
        String sql = "SELECT COUNT(*) FROM patient p WHERE p.archive = FALSE " + (filtre ? " AND (p.nom LIKE ? OR p.prenom LIKE ? OR p.telephone LIKE ? OR CAST(p.id AS CHAR) = ?)" : "");

        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (filtre) {
                String valeur = motCle.trim();
                String like = "%" + valeur + "%";
                ps.setString(1, like); ps.setString(2, like); ps.setString(3, like); ps.setString(4, valeur);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return 0;
            }
        }
    }

    private static void setNullableString(PreparedStatement ps, int index, String valeur) throws SQLException {
        if (valeur == null || valeur.isBlank()) ps.setNull(index, Types.VARCHAR);
        else ps.setString(index, valeur.trim());
    }

    private static void setNullableLong(PreparedStatement ps, int index, Long valeur) throws SQLException {
        if (valeur == null) ps.setNull(index, Types.BIGINT);
        else ps.setLong(index, valeur);
    }
}