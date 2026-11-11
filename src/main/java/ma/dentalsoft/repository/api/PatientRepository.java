package ma.dentalsoft.repository.api;

import ma.dentalsoft.domain.Patient;
import ma.dentalsoft.dto.PatientRow;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrat d'accès aux patients, indépendant du SGBD.
 * La connexion est fournie par le service : le DAO ne l'ouvre pas
 * et ne fait jamais de commit (la transaction appartient au service).
 */
public interface PatientRepository {

    Patient inserer(Connection cn, Patient patient) throws SQLException;

    boolean existeParCin(Connection cn, String cin) throws SQLException;

    boolean existeParIdentite(Connection cn, String nom, String prenom,
                              LocalDate dateNaissance) throws SQLException;

    List<PatientRow> rechercher(Connection cn, String motCle) throws SQLException;
}