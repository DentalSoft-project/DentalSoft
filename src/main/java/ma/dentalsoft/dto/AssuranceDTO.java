package ma.dentalsoft.dto;

/**
 * Élément de la liste déroulante « Assurance » du formulaire patient.
 * toString() renvoie le libellé : c'est ce que la JComboBox affiche.
 */
public record AssuranceDTO(long id, String libelle) {

    @Override
    public String toString() {
        return libelle;
    }
}
