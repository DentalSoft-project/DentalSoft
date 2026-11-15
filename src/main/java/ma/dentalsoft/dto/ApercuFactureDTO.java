package ma.dentalsoft.dto;

import java.math.BigDecimal;

/** Montant indicatif uniquement : ni facture enregistrée, ni preuve d'éligibilité. */
public record ApercuFactureDTO(Long consultationId, BigDecimal total) {
}
