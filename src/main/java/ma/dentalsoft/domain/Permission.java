package ma.dentalsoft.domain;

/** Permissions de l'application. Adapte cette liste à la conception de ton groupe. */
public enum Permission {
    GERER_UTILISATEURS("Gérer les utilisateurs"),
    GERER_ROLES("Gérer les rôles et permissions"),
    GERER_PATIENTS("Gérer les patients"),
    GERER_AGENDA("Gérer l'agenda et les rendez-vous"),
    GERER_CONSULTATIONS("Gérer les consultations"),
    GERER_CAISSE("Gérer la caisse et la facturation");

    private final String libelle;

    Permission(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
