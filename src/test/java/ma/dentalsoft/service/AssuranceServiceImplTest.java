package ma.dentalsoft.service;

import ma.dentalsoft.domain.Permission;
import ma.dentalsoft.domain.Role;
import ma.dentalsoft.domain.Secretaire;
import ma.dentalsoft.exceptions.DentalSoftException;
import ma.dentalsoft.repository.impl.jdbc_mysql.JdbcAssuranceRepository;
import ma.dentalsoft.service.api.AssuranceService;
import ma.dentalsoft.service.impl.AssuranceServiceImpl;
import ma.dentalsoft.session.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Test d'intégration sur dentalsoftbd (lecture seule). */
class AssuranceServiceImplTest {

    private final AssuranceService service = new AssuranceServiceImpl(new JdbcAssuranceRepository());

    @AfterEach
    void tearDown() {
        SessionManager.getInstance().fermer();
    }

    private static void ouvrirSession(Set<Permission> permissions) {
        Role role = new Role(1L, Role.SECRETAIRE, "Secrétaire", permissions);
        SessionManager.getInstance().ouvrir(
                new Secretaire(1L, "Test", "Secretaire", "test", "x", true, role, null, null));
    }

    @Test
    @DisplayName("Avec GERER_PATIENTS, la liste des assurances est lue sans erreur")
    void listeLesAssurances() throws Exception {
        ouvrirSession(Set.of(Permission.GERER_PATIENTS));
        assertNotNull(service.listerAssurances());
    }

    @Test
    @DisplayName("Sans la permission, la lecture est refusée")
    void refuseSansPermission() {
        ouvrirSession(Set.of());
        assertThrows(DentalSoftException.class, service::listerAssurances);
    }
}
