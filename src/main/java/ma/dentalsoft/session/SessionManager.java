package ma.dentalsoft.session;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import ma.dentalsoft.domain.Permission;
import ma.dentalsoft.domain.Utilisateur;

/**
 * Session de l'utilisateur connecté (singleton). Les permissions sont copiées à l'ouverture :
 * un changement de permissions en base ne s'applique donc qu'à la prochaine connexion.
 */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private Utilisateur utilisateur;
    private Set<Permission> permissions = EnumSet.noneOf(Permission.class);
    private LocalDateTime debut;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public synchronized void ouvrir(Utilisateur utilisateur) {
        Objects.requireNonNull(utilisateur, "utilisateur");
        var copie = EnumSet.noneOf(Permission.class);
        copie.addAll(utilisateur.getRole().getPermissions());
        this.utilisateur = utilisateur;
        this.permissions = copie;
        this.debut = LocalDateTime.now();
    }

    public synchronized void fermer() {
        this.utilisateur = null;
        this.permissions = EnumSet.noneOf(Permission.class);
        this.debut = null;
    }

    public synchronized boolean isConnecte() {
        return utilisateur != null;
    }

    public synchronized Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public synchronized LocalDateTime getDebut() {
        return debut;
    }

    public synchronized Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(EnumSet.copyOf(permissions));
    }

    /** Vrai si un utilisateur est connecté ET possède cette permission. */
    public synchronized boolean peut(Permission permission) {
        return utilisateur != null && permissions.contains(permission);
    }

    public synchronized boolean aLeRole(String codeRole) {
        return utilisateur != null && utilisateur.getRole().getCode().equals(codeRole);
    }
}
