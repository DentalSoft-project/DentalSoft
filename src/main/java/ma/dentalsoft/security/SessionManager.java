package ma.dentalsoft.security;

public class SessionManager {
    private static String currentUserLogin;
    private static String currentUserRole; // "ADMIN", "MEDECIN", "SECRETAIRE"

    // Appelé lors de la connexion réussie (T02 de Kawtar)
    public static void login(String login, String role) {
        currentUserLogin = login;
        currentUserRole = role;
    }

    public static void logout() {
        currentUserLogin = null;
        currentUserRole = null;
    }

    // Méthode utilisée par les services pour vérifier les droits (RG-12)
    public static boolean hasAccess(String requiredRole) {
        if (currentUserRole == null || requiredRole == null) return false;
        // Le médecin hérite des droits de la secrétaire (RG-02)
        if (requiredRole.equals("SECRETAIRE") && currentUserRole.equals("MEDECIN")) {
            return true;
        }
        return currentUserRole.equals(requiredRole);
    }

    public static String getCurrentUserLogin() {
        return currentUserLogin;
    }
}