package ma.dentalsoft.ui.models;

import ma.dentalsoft.dto.PatientRow;
import ma.dentalsoft.shared.formatting.DisplayFormats;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/** Modèle d'affichage du tableau des patients : ne contient aucune logique métier. */
public class PatientTableModel extends AbstractTableModel {

    private static final String[] COLONNES =
            {"Nom complet", "Âge", "Sexe", "Date de naissance", "Téléphone", "CIN", "Assurance"};
    private static final String VIDE = "—";

    private List<PatientRow> lignes = List.of();

    public void setRows(List<PatientRow> nouvellesLignes) {
        this.lignes = nouvellesLignes == null ? List.of() : List.copyOf(nouvellesLignes);
        fireTableDataChanged();
    }

    public PatientRow getRow(int index) {
        return lignes.get(index);
    }

    @Override
    public int getRowCount() {
        return lignes.size();
    }

    @Override
    public int getColumnCount() {
        return COLONNES.length;
    }

    @Override
    public String getColumnName(int colonne) {
        return COLONNES[colonne];
    }

    @Override
    public Object getValueAt(int ligne, int colonne) {
        PatientRow p = lignes.get(ligne);
        return switch (colonne) {
            case 0 -> p.nomComplet();
            case 1 -> String.valueOf(p.age());
            case 2 -> p.sexe() == null ? VIDE : p.sexe().getLibelle();
            case 3 -> p.dateNaissance() == null ? VIDE : DisplayFormats.formatDate(p.dateNaissance());
            case 4 -> p.telephone() == null ? VIDE : DisplayFormats.formatPhone(p.telephone());
            case 5 -> valeurOuVide(p.cin());
            case 6 -> valeurOuVide(p.assurance());
            default -> throw new IndexOutOfBoundsException("Colonne inconnue : " + colonne);
        };
    }

    private static String valeurOuVide(String valeur) {
        return valeur == null || valeur.isBlank() ? VIDE : valeur;
    }
}
