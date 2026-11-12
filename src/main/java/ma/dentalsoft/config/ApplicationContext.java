package ma.dentalsoft.config;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Factory + Singleton : crée les composants (repositories, services) à partir des noms de classes
 * écrits dans application.properties, puis les fournit via getBean().
 * Contrairement à la version de départ, toute erreur de configuration arrête le démarrage avec
 * un message clair au lieu de laisser des beans à null.
 */
public final class ApplicationContext {

    /** Ordre de création : un composant ne peut dépendre que de ceux qui le précèdent. */
    private static final List<String> COMPOSANTS =
            List.of("dao.roleRepo", "dao.utilisateurRepo", "service.authService");

    private static ApplicationContext instance;

    private final Map<Class<?>, Object> beans = new HashMap<>();
    private Connection connection;

    private ApplicationContext() {
        try {
            var props = chargerProprietes();
            connection = JdbcConnectionFactory.createConnection(props);
            beans.put(Connection.class, connection);

            for (var cle : COMPOSANTS) {
                var nomClasse = props.getProperty(cle);
                if (nomClasse == null || nomClasse.isBlank()) {
                    throw new IllegalStateException("Propriété obligatoire absente : " + cle);
                }
                creer(nomClasse.trim());
            }
            Runtime.getRuntime().addShutdownHook(new Thread(this::fermerConnexion));
        } catch (IllegalStateException e) {
            fermerConnexion();
            throw e;
        } catch (ReflectiveOperationException | IOException e) {
            fermerConnexion();
            throw new IllegalStateException("Erreur de configuration de l'application : " + e, e);
        }
    }

    public static synchronized ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public <T> T getBean(Class<T> type) {
        var bean = beans.get(type);
        if (bean == null) {
            throw new IllegalStateException("Aucun composant enregistré pour " + type.getSimpleName());
        }
        return type.cast(bean);
    }

    private Properties chargerProprietes() throws IOException {
        var loader = Thread.currentThread().getContextClassLoader();
        try (var in = loader.getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IllegalStateException("Fichier application.properties introuvable.");
            }
            var props = new Properties();
            props.load(in);
            return props;
        }
    }

    /** Instancie la classe avec le premier constructeur dont tous les paramètres sont déjà des beans. */
    private void creer(String nomClasse) throws ReflectiveOperationException {
        var classe = Class.forName(nomClasse);
        for (Constructor<?> constructeur : classe.getConstructors()) {
            var types = constructeur.getParameterTypes();
            var arguments = new Object[types.length];
            var resolu = true;
            for (var i = 0; i < types.length; i++) {
                arguments[i] = beans.get(types[i]);
                if (arguments[i] == null) {
                    resolu = false;
                    break;
                }
            }
            if (resolu) {
                var bean = constructeur.newInstance(arguments);
                beans.put(classe, bean);
                for (var contrat : classe.getInterfaces()) {
                    beans.put(contrat, bean); // on peut demander le bean par son interface
                }
                return;
            }
        }
        throw new IllegalStateException("Aucun constructeur utilisable pour " + nomClasse
                + " (dépendances manquantes ou mauvais ordre dans ApplicationContext).");
    }

    private void fermerConnexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur à la fermeture de la connexion : " + e.getMessage());
        }
    }
}
