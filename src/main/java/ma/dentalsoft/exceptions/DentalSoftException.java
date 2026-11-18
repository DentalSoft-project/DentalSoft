package ma.dentalsoft.exceptions;

/**
 * SOCLE M3 - exception racine des erreurs applicatives.
 * À aligner avec le package exceptions du groupe si besoin.
 */
public class DentalSoftException extends Exception {

    public DentalSoftException(String message) {
        super(message);
    }

    public DentalSoftException(String message, Throwable cause) {
        super(message, cause);
    }
}