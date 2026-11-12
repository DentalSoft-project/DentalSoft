package ma.dentalsoft.repository.api;

import java.util.List;
import ma.dentalsoft.domain.Role;

public interface RoleRepo {
    Role findById(Long idRole);
    Role findByCode(String code);
    List<Role> findAll();
}
