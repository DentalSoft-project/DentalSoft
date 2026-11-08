package ma.dentalsoft.domain;

/** Personnel du cabinet : parent de Admin, Medecin et Secretaire. */
public abstract class Staff extends Utilisateur {
    protected Staff(Long id, String nom, String prenom, String login,
                    String motDePasseHash, boolean actif, Role role) {
        super(id, nom, prenom, login, motDePasseHash, actif, role);
    }
}
