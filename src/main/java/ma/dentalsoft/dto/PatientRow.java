package ma.dentalsoft.dto;

import ma.dentalsoft.domain.Sexe;

import java.time.LocalDate;
import java.time.Period;

/**
 * Ligne de lecture pour le tableau Swing : évite de charger le domaine
 * complet et porte le libellé d'assurance issu de la jointure.
 */
public record PatientRow(
        long id,
        String nom,
        String prenom,
        LocalDate dateNaissance,
        Sexe sexe,
        String cin,
        String telephone,
        String assurance,
        boolean archive
) {

    public int age() {
        if (dateNaissance == null) {
            return 0;
        }
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    public String nomComplet() {
        return prenom + " " + nom;
    }
}