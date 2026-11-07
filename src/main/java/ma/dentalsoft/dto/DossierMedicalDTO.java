package ma.dentalsoft.dto;

public class DossierMedicalDTO {
    private Long idDossier;
    private Long idPatient;
    private String observationsCliniques; // Donnée sensible

    // Constructeur
    public DossierMedicalDTO(Long idDossier, Long idPatient, String observationsCliniques) {
        this.idDossier = idDossier;
        this.idPatient = idPatient;
        this.observationsCliniques = observationsCliniques;
    }

    // Getters
    public Long getIdDossier() { return idDossier; }
    public Long getIdPatient() { return idPatient; }

    // Getter spécifique qui applique la règle RG-12 (Seul le médecin voit les notes)
    public String getObservationsCliniques() {
        if (ma.dentalsoft.security.SessionManager.hasAccess("MEDECIN")) {
            return observationsCliniques;
        }
        // Si c'est la secrétaire, on renvoie null ou un message
        return null;
    }
}
