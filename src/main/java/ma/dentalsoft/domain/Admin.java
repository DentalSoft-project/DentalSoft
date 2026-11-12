package ma.dentalsoft.domain;

public class Admin extends Staff {
    public Admin(Long id, String nom, String prenom, String login,
                 String motDePasseHash, boolean actif, Role role) {
        super(id, nom, prenom, login, motDePasseHash, actif, role);
    }
}
