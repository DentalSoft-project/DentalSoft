package ma.dentalsoft.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * SOCLE TEMPORAIRE M3 - point d'entrée unique pour obtenir une connexion JDBC.
 * Lit /application.properties (clés db.url, db.user, db.password).
 * À remplacer par la version de Rania (EN-05) quand elle sera disponible.
 */
public final class JdbcConnectionFactory {

    private static final String CONFIG_FILE = "/application.properties";

    private static final Properties PROPRIETES = charger();
    private static final String URL = requis("db.url");
    private static final String UTILISATEUR = requis("db.user");
    private static final String MOT_DE_PASSE = PROPRIETES.getProperty("db.password", "");

    private JdbcConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, UTILISATEUR, MOT_DE_PASSE);
    }

    private static Properties charger() {
        Properties proprietes = new Properties();
        try (InputStream in = JdbcConnectionFactory.class.getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                        "Fichier " + CONFIG_FILE + " introuvable dans src/main/resources");
            }
            proprietes.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Lecture de " + CONFIG_FILE + " impossible", e);
        }
        return proprietes;
    }

    private static String requis(String cle) {
        String valeur = PROPRIETES.getProperty(cle);
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalStateException("Propriété obligatoire absente : " + cle);
        }
        return valeur.trim();
    }
}