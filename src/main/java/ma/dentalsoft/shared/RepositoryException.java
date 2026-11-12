package ma.dentalsoft.shared;

/** Erreur dans la couche d'accès aux données (masque SQLException aux couches supérieures). */
public class RepositoryException extends RuntimeException {
    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
