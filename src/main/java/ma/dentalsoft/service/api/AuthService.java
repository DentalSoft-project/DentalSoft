package ma.dentalsoft.service.api;

import ma.dentalsoft.domain.Utilisateur;
import ma.dentalsoft.shared.AuthentificationException;

public interface AuthService {
    /**
     * Vérifie les identifiants et renvoie l'utilisateur authentifié.
     *
     * @throws AuthentificationException si les identifiants sont invalides
     *         ou si le compte est désactivé
     */
    Utilisateur login(String login, String motDePasse);
}
