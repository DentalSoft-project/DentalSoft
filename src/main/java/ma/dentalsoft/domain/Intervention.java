package ma.dentalsoft.domain;

import java.math.BigDecimal;

public class Intervention {
    private Long id;
    private Long consultationId;
    private Long acteId; // Vient du catalogue de Khalil
    private String libelleActeSnapshot; // On garde le nom de l'acte au moment du soin
    private String dent; // Optionnel (ex: "11", "48")
    private int quantite;
    private BigDecimal prixUnitaire; // Règle RG-08 : BigDecimal obligatoire

    // Constructeur
    public Intervention(Long consultationId, Long acteId, String libelle, String dent, int quantite, BigDecimal prix) {
        this.consultationId = consultationId;
        this.acteId = acteId;
        this.libelleActeSnapshot = libelle;
        this.dent = dent;
        this.quantite = quantite;
        this.prixUnitaire = prix;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConsultationId() { return consultationId; }
    public String getLibelleActeSnapshot() { return libelleActeSnapshot; }
    public String getDent() { return dent; }
    public int getQuantite() { return quantite; }
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }

    // Méthode utilitaire pour calculer le sous-total
    public BigDecimal getSousTotal() {
        return prixUnitaire.multiply(new BigDecimal(quantite));
    }
}