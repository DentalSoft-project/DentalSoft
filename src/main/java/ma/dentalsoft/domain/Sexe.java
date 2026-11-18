package ma.dentalsoft.domain;

/**
 * Correspond à l'ENUM MySQL ('M','F').
 * Le libellé sert à l'affichage dans les combo box et les tableaux.
 */
public enum Sexe {

    M("Masculin"),
    F("Féminin");

    private final String libelle;

    Sexe(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}