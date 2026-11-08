package ma.dentalsoft.service.api;

import ma.dentalsoft.domain.Utilisateur;

public interface AuthService {
    /**
     * Vérifie les identifiants et renvoie l'utilisateur authentifié.
     *
     * @throws ma.dentalsoft.shared.AuthenticationException si les identifiants sont invalides
     *         ou si le compte est désactivé
     */
    Utilisateur login(String login, String motDePasse);
}
