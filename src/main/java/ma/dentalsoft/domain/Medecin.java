package ma.dentalsoft.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class Medecin extends Staff {
    private final String specialite;

    public Medecin(Long id, String nom, String prenom, String login,
                   String motDePasseHash, boolean actif, Role role, String specialite) {
        super(id, nom, prenom, login, motDePasseHash, actif, role);
        this.specialite = specialite;
    }

    public String getSpecialite() { return specialite; }

    @Override
    public Map<String, String> getInfosSpecifiques() {
        var infos = new LinkedHashMap<String, String>();
        infos.put("Spécialité", specialite == null ? "—" : specialite);
        return infos;
    }
}
