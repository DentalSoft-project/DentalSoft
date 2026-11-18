package ma.dentalsoft.repository.impl.jdbc_mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import ma.dentalsoft.domain.Permission;
import ma.dentalsoft.domain.Role;
import ma.dentalsoft.repository.api.RoleRepo;
import ma.dentalsoft.shared.RepositoryException;

/** Implémentation JDBC/MySQL du repository des rôles (avec leurs permissions). */
public class RoleRepoImpl implements RoleRepo {

    private static final String SELECT_ROLE = "SELECT id_role, code, libelle FROM roles";
    private final Connection connection;

    public RoleRepoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Role findById(Long idRole) {
        if (idRole == null) {
            return null;
        }
        return findOne(SELECT_ROLE + " WHERE id_role = ?", idRole);
    }

    @Override
    public Role findByCode(String code) {
        if (code == null) {
            return null;
        }
        return findOne(SELECT_ROLE + " WHERE code = ?", code);
    }

    @Override
    public List<Role> findAll() {
        var roles = new ArrayList<Role>();
        try (var st = connection.prepareStatement(SELECT_ROLE + " ORDER BY id_role");
             var rs = st.executeQuery()) {
            while (rs.next()) {
                roles.add(mapper(rs));
            }
            return roles;
        } catch (SQLException e) {
            throw new RepositoryException("Erreur pendant la lecture des rôles", e);
        }
    }

    private Role findOne(String sql, Object parametre) {
        try (var st = connection.prepareStatement(sql)) {
            st.setObject(1, parametre);
            try (var rs = st.executeQuery()) {
                return rs.next() ? mapper(rs) : null;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erreur pendant la recherche du rôle " + parametre, e);
        }
    }

    private Role mapper(ResultSet rs) throws SQLException {
        var id = rs.getLong("id_role");
        return new Role(id, rs.getString("code"), rs.getString("libelle"), chargerPermissions(id));
    }

    private Set<Permission> chargerPermissions(long idRole) throws SQLException {
        var permissions = EnumSet.noneOf(Permission.class);
        try (var st = connection.prepareStatement(
                "SELECT permission FROM role_permissions WHERE id_role = ?")) {
            st.setLong(1, idRole);
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    try {
                        permissions.add(Permission.valueOf(rs.getString("permission")));
                    } catch (IllegalArgumentException ignoree) {
                        // permission inconnue du code : on l'ignore
                    }
                }
            }
        }
        return permissions;
    }
}
