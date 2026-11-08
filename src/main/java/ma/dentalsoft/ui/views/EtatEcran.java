package ma.dentalsoft.ui.view;

/** Les 5 états d'un écran demandés par le dossier de spécification. */
public enum EtatEcran {
    VIDE,              // écran vierge, rien de saisi
    SAISIE_INVALIDE,   // champs manquants ou incorrects
    CHARGEMENT,        // opération en cours
    ERREUR,            // échec (identifiants faux, base indisponible...)
    SUCCES             // opération réussie
}
