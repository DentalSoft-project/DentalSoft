package ma.dentalsoft.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Normalement, ces données viennent de application.properties (BNF-02)
    // Ici, c'est une version simplifiée pour le S1.
    private static final String URL = "jdbc:mysql://localhost:3306/dentalsoft_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    // Méthode pour obtenir une connexion
    public static Connection getConnection() throws SQLException {
        try {
            // Chargement du driver MySQL (Optionnel avec les versions récentes de JDBC, mais bien de le garder)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur : Driver MySQL introuvable. Vérifiez le pom.xml");
            e.printStackTrace();
            return null;
        }
    }

    // Méthode utilitaire pour fermer la connexion proprement
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}