package ma.dentalsoft.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Facture persistée ; aucun montant fourni librement par l'écran. */
public class Facture extends BaseEntity {
    private final Long consultationId;
    private final Long situationFinanciereId;
    private final String numero;
    private final LocalDateTime dateFacture;
    private final BigDecimal total;
    private final String statut;

    public Facture(Long consultationId, Long situationFinanciereId, String numero,
                   LocalDateTime dateFacture, BigDecimal total, String statut) {
        this.consultationId = consultationId;
        this.situationFinanciereId = situationFinanciereId;
        this.numero = numero;
        this.dateFacture = dateFacture;
        this.total = total;
        this.statut = statut;
    }

    public Long getConsultationId() { return consultationId; }
    public Long getSituationFinanciereId() { return situationFinanciereId; }
    public String getNumero() { return numero; }
    public LocalDateTime getDateFacture() { return dateFacture; }
    public BigDecimal getTotal() { return total; }
    public String getStatut() { return statut; }
}
