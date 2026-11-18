package ma.dentalsoft.service.impl;

import ma.dentalsoft.config.JdbcConnectionFactory;
import ma.dentalsoft.domain.Permission;
import ma.dentalsoft.dto.AssuranceDTO;
import ma.dentalsoft.exceptions.DatabaseException;
import ma.dentalsoft.exceptions.DentalSoftException;
import ma.dentalsoft.repository.api.AssuranceRepository;
import ma.dentalsoft.service.api.AssuranceService;
import ma.dentalsoft.session.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AssuranceServiceImpl implements AssuranceService {

    private final AssuranceRepository repository;

    public AssuranceServiceImpl(AssuranceRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AssuranceDTO> listerAssurances() throws DentalSoftException {
        if (!SessionManager.getInstance().peut(Permission.GERER_PATIENTS)) {
            throw new DentalSoftException("Cette action n'est pas autorisée pour votre rôle.");
        }
        try (Connection cn = JdbcConnectionFactory.getConnection()) {
            return repository.listerToutes(cn);
        } catch (SQLException e) {
            throw new DatabaseException("Lecture des assurances impossible.", e);
        }
    }
}
