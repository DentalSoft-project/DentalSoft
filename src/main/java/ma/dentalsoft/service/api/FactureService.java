package ma.dentalsoft.service.api;

import ma.dentalsoft.dto.ApercuFactureDTO;
import ma.dentalsoft.domain.Facture;
import ma.dentalsoft.exceptions.DentalSoftException;

public interface FactureService {

    /**
     * Prépare le montant depuis les interventions M5, sans créer de facture.
     * L'existence, la validation et la présence d'interventions ne sont pas
     * encore garanties par M5 : cet aperçu ne permet pas la facturation.
     */
    ApercuFactureDTO previsualiserFacture(Long consultationId);

    /**
     * Crée un brouillon depuis une consultation TERMINEE persistée avec interventions.
     * Le numéro est fourni explicitement : aucune convention automatique imposée.
     * Le total est recalculé en transaction, jamais repris depuis l'aperçu mémoire.
     */
    Facture genererBrouillon(Long consultationId, String numero) throws DentalSoftException;
}
