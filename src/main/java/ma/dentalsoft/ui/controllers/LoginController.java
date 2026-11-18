package ma.dentalsoft.ui.controllers;

import ma.dentalsoft.domain.Utilisateur;
import ma.dentalsoft.service.api.AuthService;
import ma.dentalsoft.session.SessionManager;

/** Contrôleur Swing : fait le lien entre l'écran de connexion, le service et la session. */
public class LoginController {

    private final AuthService authService;
    private final SessionManager session;

    public LoginController(AuthService authService, SessionManager session) {
        this.authService = authService;
        this.session = session;
    }

    /** Authentifie puis ouvre la session. Peut lever AuthenticationException ou RepositoryException. */
    public Utilisateur connecter(String login, String motDePasse) {
        var utilisateur = authService.login(login, motDePasse);
        session.ouvrir(utilisateur);
        return utilisateur;
    }

    public void deconnecter() {
        session.fermer();
    }

    public SessionManager getSession() {
        return session;
    }
}
