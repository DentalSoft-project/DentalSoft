package ma.dentalsoft.domain;

/**
 * Dossier médical : un seul par patient (RG-01).
 * Il est créé dans la même transaction que le patient.
 */
public class DossierMedical extends BaseEntity {

    private Long patientId;

    public DossierMedical() {
    }

    public DossierMedical(Long patientId) {
        this.patientId = patientId;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
}