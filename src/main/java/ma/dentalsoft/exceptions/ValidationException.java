package ma.dentalsoft.exceptions;

/**
 * Erreur de validation métier : donnée saisie incorrecte ou règle violée.
 */
public class ValidationException extends DentalSoftException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}