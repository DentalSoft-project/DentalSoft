package ma.dentalsoft.shared;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hachage des mots de passe avec PBKDF2 (fourni par le JDK, aucune dépendance).
 * Format stocké en base : pbkdf2$iterations$sel$hash  (sel et hash en Base64).
 */
public final class PasswordHasher {

    private static final String ALGORITHME = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int TAILLE_SEL = 16;
    private static final int TAILLE_CLE_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {}

    public static String hash(String motDePasse) {
        var sel = new byte[TAILLE_SEL];
        RANDOM.nextBytes(sel);
        var hash = deriver(motDePasse, sel, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(sel) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String motDePasse, String stocke) {
        if (motDePasse == null || stocke == null) {
            return false;
        }
        var parties = stocke.split("\\$");
        if (parties.length != 4 || !"pbkdf2".equals(parties[0])) {
            return false;
        }
        try {
            var iterations = Integer.parseInt(parties[1]);
            var sel = Base64.getDecoder().decode(parties[2]);
            var attendu = Base64.getDecoder().decode(parties[3]);
            var calcule = deriver(motDePasse, sel, iterations);
            return MessageDigest.isEqual(attendu, calcule); // comparaison en temps constant
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] deriver(String motDePasse, byte[] sel, int iterations) {
        var spec = new PBEKeySpec(motDePasse.toCharArray(), sel, iterations, TAILLE_CLE_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHME).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Hachage du mot de passe impossible", e);
        } finally {
            spec.clearPassword();
        }
    }
}
