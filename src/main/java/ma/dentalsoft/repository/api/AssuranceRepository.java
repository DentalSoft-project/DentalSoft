package ma.dentalsoft.repository.api;

import ma.dentalsoft.dto.AssuranceDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Lecture du référentiel des assurances (lecture seule). */
public interface AssuranceRepository {

    List<AssuranceDTO> listerToutes(Connection cn) throws SQLException;
}
