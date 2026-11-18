package ma.dentalsoft.exceptions;

/**
 * Erreur technique d'accès à la base (SQLException encapsulée).
 */
public class DatabaseException extends DentalSoftException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}