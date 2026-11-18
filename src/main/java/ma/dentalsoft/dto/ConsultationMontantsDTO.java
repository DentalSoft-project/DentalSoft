package ma.dentalsoft.dto;

import java.math.BigDecimal;
import java.util.List;

/** Projection de lecture financière du SQL M5, sans observations cliniques. */
public record ConsultationMontantsDTO(long consultationId, long dossierId, String statut,
                                     List<Ligne> interventions) {
    public ConsultationMontantsDTO {
        interventions = List.copyOf(interventions);
    }

    public record Ligne(int quantite, BigDecimal prixUnitaire) { }
}
