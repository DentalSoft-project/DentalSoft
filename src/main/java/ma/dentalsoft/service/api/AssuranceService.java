package ma.dentalsoft.service.api;

import ma.dentalsoft.dto.AssuranceDTO;
import ma.dentalsoft.exceptions.DentalSoftException;

import java.util.List;

public interface AssuranceService {

    /** Liste des assurances pour le formulaire patient. Nécessite la permission GERER_PATIENTS. */
    List<AssuranceDTO> listerAssurances() throws DentalSoftException;
}
