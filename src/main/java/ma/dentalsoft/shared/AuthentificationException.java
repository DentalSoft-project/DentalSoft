package ma.dentalsoft.shared;

/** Échec d'authentification : le message est affichable tel quel à l'utilisateur. */
public class AuthentificationException extends RuntimeException {
    public AuthentificationException(String message) {
        super(message);
    }
}
