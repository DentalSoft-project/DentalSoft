package ma.dentalsoft.repository.api;

import java.util.List;
import ma.dentalsoft.domain.Utilisateur;

public interface UtilisateurRepo {
    Utilisateur findByLogin(String login);
    Utilisateur findById(Long idUtilisateur);
    List<Utilisateur> findAll();
}
