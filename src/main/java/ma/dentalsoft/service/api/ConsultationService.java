package ma.dentalsoft.service.api;

import ma.dentalsoft.domain.Consultation;
import ma.dentalsoft.domain.Intervention;
import java.math.BigDecimal;

public interface ConsultationService {

    // US-M5-01
    Consultation demarrerConsultation(Long dossierId, Long rdvId);

    // US-M5-03
    Intervention ajouterIntervention(Long consultationId, Long acteId, String libelle, String dent, int quantite, BigDecimal prix);

    // US-M5-04
    BigDecimal calculerTotalConsultation(Long consultationId);
}