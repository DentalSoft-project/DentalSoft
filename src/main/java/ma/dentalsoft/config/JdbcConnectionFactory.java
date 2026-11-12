package ma.dentalsoft.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Crée la connexion JDBC à partir de application.properties. Échoue clairement si impossible. */
public final class JdbcConnectionFactory {

    private JdbcConnectionFactory() {}

    public static Connection getConnection() throws SQLException {
        return createConnection(charger());
    }

    public static Connection createConnection(Properties props) {
        var driver = requis(props, "dataSource.driver");
        var url = requis(props, "dataSource.url");
        var user = requis(props, "dataSource.user");
        var pass = requis(props, "dataSource.pass");
        try {
            Class.forName(driver);
            var connection = DriverManager.getConnection(url, user, pass);
            System.out.println("Connecté à la base : " + url);
            return connection;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Pilote JDBC introuvable : " + driver, e);
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Connexion à MySQL impossible (" + url + ") : " + e.getMessage()
                            + "\nVérifie que MySQL est démarré, que schema.sql a été exécuté "
                            + "et que user/pass sont corrects dans application.properties.", e);
        }
    }

    private static String requis(Properties props, String cle) {
        var valeur = props.getProperty(cle);
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalStateException("Propriété obligatoire absente : " + cle);
        }
        return valeur.trim();
    }

    private static Properties charger() {
        Properties props = new Properties();
        try (InputStream in = JdbcConnectionFactory.class.getResourceAsStream("/application.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "Fichier /application.properties introuvable dans src/main/resources");
            }
            props.load(in);
            return props;
        } catch (IOException e) {
            throw new IllegalStateException("Lecture de /application.properties impossible", e);
        }
    }
}
