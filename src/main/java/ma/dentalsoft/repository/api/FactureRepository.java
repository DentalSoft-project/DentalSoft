package ma.dentalsoft.repository.api;

import ma.dentalsoft.domain.Facture;
import ma.dentalsoft.dto.ConsultationMontantsDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/** Connexion fournie par le service ; aucun commit/rollback dans le repository. */
public interface FactureRepository {
    Optional<ConsultationMontantsDTO> verrouillerConsultation(Connection cn, long consultationId)
            throws SQLException;

    Optional<Facture> trouverParConsultation(Connection cn, long consultationId) throws SQLException;

    long obtenirSituationFinanciere(Connection cn, long dossierId, long auteurId) throws SQLException;

    Facture inserer(Connection cn, Facture facture) throws SQLException;
}
