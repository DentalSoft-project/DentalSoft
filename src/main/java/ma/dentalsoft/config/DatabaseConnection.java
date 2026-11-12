package ma.dentalsoft.config;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getConnection() throws SQLException {
        return JdbcConnectionFactory.getConnection();
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Fermeture de la connexion impossible.", e);
            }
        }
    }
}