package ma.dentalsoft.domain;

import java.time.LocalDateTime;

public class Consultation {
    private Long id;
    private Long dossierId; // Le patient concerné (créé au S1)
    private Long medecinId;
    private Long rdvId; // Optionnel, lien avec le RDV de Zineb
    private LocalDateTime dateConsultation;
    private String observation;
    private String statut; // "EN_COURS", "TERMINEE", "ANNULEE"

    // Constructeur
    public Consultation(Long dossierId, Long medecinId, Long rdvId) {
        this.dossierId = dossierId;
        this.medecinId = medecinId;
        this.rdvId = rdvId;
        this.dateConsultation = LocalDateTime.now();
        this.statut = "EN_COURS"; // Par défaut
    }

    // Getters et Setters (nécessaires pour le DAO et le Service)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDossierId() { return dossierId; }
    public Long getMedecinId() { return medecinId; }
    public Long getRdvId() { return rdvId; }
    public LocalDateTime getDateConsultation() { return dateConsultation; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}