package ma.dentalsoft.service.impl;

import ma.dentalsoft.domain.Utilisateur;
import ma.dentalsoft.repository.api.UtilisateurRepo;
import ma.dentalsoft.service.api.AuthService;
import ma.dentalsoft.shared.AuthenticationException;
import ma.dentalsoft.shared.PasswordHasher;

public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepo repo;

    public AuthServiceImpl(UtilisateurRepo repo) {
        this.repo = repo;
    }

    @Override
    public Utilisateur login(String login, String motDePasse) {
        if (login == null || login.isBlank() || motDePasse == null || motDePasse.isEmpty()) {
            throw new AuthenticationException("Veuillez saisir votre identifiant et votre mot de passe.");
        }

        var utilisateur = repo.findByLogin(login.trim());

        // Même message pour « login inconnu » et « mauvais mot de passe » : on ne révèle rien.
        if (utilisateur == null || !PasswordHasher.verify(motDePasse, utilisateur.getMotDePasseHash())) {
            throw new AuthenticationException("Identifiant ou mot de passe incorrect.");
        }
        if (!utilisateur.isActif()) {
            throw new AuthenticationException("Ce compte est désactivé. Contactez l'administrateur.");
        }
        return utilisateur;
    }
}
