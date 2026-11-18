package ma.dentalsoft.ui.models;

import ma.dentalsoft.domain.Sexe;
import ma.dentalsoft.dto.PatientRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Test unitaire pur (sans base de données ni Swing visible). */
class PatientTableModelTest {

    @Test
    @DisplayName("Le modèle affiche les valeurs formatées et « — » pour les champs vides")
    void afficheLesLignes() {
        var modele = new PatientTableModel();
        modele.setRows(List.of(new PatientRow(1L, "Alami", "Sara", LocalDate.of(1990, 5, 12),
                Sexe.F, null, "0612345678", null, false)));

        assertEquals(1, modele.getRowCount());
        assertEquals("Sara Alami", modele.getValueAt(0, 0));
        assertEquals("Féminin", modele.getValueAt(0, 2));
        assertEquals("12/05/1990", modele.getValueAt(0, 3));
        assertEquals("06 12 34 56 78", modele.getValueAt(0, 4));
        assertEquals("—", modele.getValueAt(0, 5));
        assertEquals("—", modele.getValueAt(0, 6));
    }

    @Test
    @DisplayName("setRows(null) vide le tableau")
    void listeNulleVideLeTableau() {
        var modele = new PatientTableModel();
        modele.setRows(null);
        assertEquals(0, modele.getRowCount());
    }
}
