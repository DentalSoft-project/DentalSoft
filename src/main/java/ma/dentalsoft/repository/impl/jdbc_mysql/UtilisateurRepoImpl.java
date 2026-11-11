package ma.dentalsoft.repository.impl.jdbc_mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ma.dentalsoft.domain.Admin;
import ma.dentalsoft.domain.Medecin;
import ma.dentalsoft.domain.Role;
import ma.dentalsoft.domain.Secretaire;
import ma.dentalsoft.domain.Utilisateur;
import ma.dentalsoft.repository.api.RoleRepo;
import ma.dentalsoft.repository.api.UtilisateurRepo;
import ma.dentalsoft.shared.RepositoryException;

/** Implémentation JDBC/MySQL du repository des utilisateurs. */
public class UtilisateurRepoImpl implements UtilisateurRepo {

    private static final String SELECT =
            "SELECT id_utilisateur, nom, prenom, login, mot_de_passe, actif, id_role, "
                    + "specialite, num_cnss, commission FROM utilisateurs";

    private final Connection connection;
    private final RoleRepo roleRepo;

    public UtilisateurRepoImpl(Connection connection, RoleRepo roleRepo) {
        this.connection = connection;
        this.roleRepo = roleRepo;
    }

    @Override
    public Utilisateur findByLogin(String login) {
        if (login == null) {
            return null;
        }
        return findOne(SELECT + " WHERE login = ?", login);
    }

    @Override
    public Utilisateur findById(Long idUtilisateur) {
        if (idUtilisateur == null) {
            return null;
        }
        return findOne(SELECT + " WHERE id_utilisateur = ?", idUtilisateur);
    }

    @Override
    public List<Utilisateur> findAll() {
        var utilisateurs = new ArrayList<Utilisateur>();
        try (var st = connection.prepareStatement(SELECT + " ORDER BY id_utilisateur");
             var rs = st.executeQuery()) {
            while (rs.next()) {
                utilisateurs.add(mapper(rs));
            }
            return utilisateurs;
        } catch (SQLException e) {
            throw new RepositoryException("Erreur pendant la lecture des utilisateurs", e);
        }
    }

    private Utilisateur findOne(String sql, Object parametre) {
        try (var st = connection.prepareStatement(sql)) {
            st.setObject(1, parametre);
            try (var rs = st.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erreur pendant la recherche de l'utilisateur", e);
        }
    }

    /** Construit la bonne sous-classe (Admin, Medecin, Secretaire) selon le code du rôle. */
    private Utilisateur mapper(ResultSet rs) throws SQLException {
        var id = rs.getLong("id_utilisateur");
        var nom = rs.getString("nom");
        var prenom = rs.getString("prenom");
        var login = rs.getString("login");
        var hash = rs.getString("mot_de_passe");
        var actif = rs.getBoolean("actif");
        var role = roleRepo.findById(rs.getLong("id_role"));
        if (role == null) {
            throw new RepositoryException("Rôle introuvable pour l'utilisateur " + login);
        }

        return switch (role.getCode()) {
            case Role.ADMIN -> new Admin(id, nom, prenom, login, hash, actif, role);
            case Role.MEDECIN -> new Medecin(id, nom, prenom, login, hash, actif, role,
                    rs.getString("specialite"));
            case Role.SECRETAIRE -> {
                var commission = rs.getBigDecimal("commission");
                yield new Secretaire(id, nom, prenom, login, hash, actif, role,
                        rs.getString("num_cnss"),
                        commission == null ? null : commission.doubleValue());
            }
            default -> throw new RepositoryException("Rôle non géré : " + role.getCode());
        };
    }
}
