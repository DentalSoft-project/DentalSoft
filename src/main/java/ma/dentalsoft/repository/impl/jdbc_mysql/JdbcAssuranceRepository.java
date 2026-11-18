package ma.dentalsoft.repository.impl.jdbc_mysql;

import ma.dentalsoft.dto.AssuranceDTO;
import ma.dentalsoft.repository.api.AssuranceRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcAssuranceRepository implements AssuranceRepository {

    private static final String SQL_TOUTES = "SELECT id, libelle FROM assurance ORDER BY libelle";

    @Override
    public List<AssuranceDTO> listerToutes(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_TOUTES);
             ResultSet rs = ps.executeQuery()) {
            List<AssuranceDTO> assurances = new ArrayList<>();
            while (rs.next()) {
                assurances.add(new AssuranceDTO(rs.getLong("id"), rs.getString("libelle")));
            }
            return assurances;
        }
    }
}
