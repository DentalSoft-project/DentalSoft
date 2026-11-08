package ma.dentalsoft.domain;

import java.util.Map;

/** Classe de base de tous les comptes de l'application. */
public abstract class Utilisateur {
    private final Long idUtilisateur;
    private final String nom;
    private final String prenom;
    private final String login;
    private final String motDePasseHash;
    private final boolean actif;
    private final Role role;

    protected Utilisateur(Long idUtilisateur, String nom, String prenom, String login,
                          String motDePasseHash, boolean actif, Role role) {
        this.idUtilisateur = idUtilisateur;
        this.nom = nom;
        this.prenom = prenom;
        this.login = login;
        this.motDePasseHash = motDePasseHash;
        this.actif = actif;
        this.role = role;
    }

    public Long getIdUtilisateur() { return idUtilisateur; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getLogin() { return login; }
    public String getMotDePasseHash() { return motDePasseHash; }
    public boolean isActif() { return actif; }
    public Role getRole() { return role; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    /** Champs propres au type d'utilisateur, affichés dans le profil (libellé -> valeur). */
    public Map<String, String> getInfosSpecifiques() {
        return Map.of();
    }
}
