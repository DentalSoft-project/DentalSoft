package ma.dentalsoft.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Role {
    public static final String ADMIN = "ADMIN";
    public static final String MEDECIN = "MEDECIN";
    public static final String SECRETAIRE = "SECRETAIRE";

    private final Long idRole;
    private final String code;
    private final String libelle;
    private final Set<Permission> permissions;

    public Role(Long idRole, String code, String libelle, Set<Permission> permissions) {
        this.idRole = idRole;
        this.code = code;
        this.libelle = libelle;
        var copie = EnumSet.noneOf(Permission.class);
        copie.addAll(permissions);
        this.permissions = Collections.unmodifiableSet(copie);
    }

    public Long getIdRole() { return idRole; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
    public Set<Permission> getPermissions() { return permissions; }

    public boolean a(Permission permission) {
        return permissions.contains(permission);
    }
}
