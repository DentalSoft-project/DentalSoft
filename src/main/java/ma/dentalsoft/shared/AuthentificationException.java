package ma.dentalsoft.shared;

/** Échec d'authentification : le message est affichable tel quel à l'utilisateur. */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
