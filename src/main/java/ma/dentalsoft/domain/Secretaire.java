package ma.dentalsoft.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class Secretaire extends Staff {
    private final String numCnss;
    private final Double commission;

    public Secretaire(Long id, String nom, String prenom, String login,
                      String motDePasseHash, boolean actif, Role role,
                      String numCnss, Double commission) {
        super(id, nom, prenom, login, motDePasseHash, actif, role);
        this.numCnss = numCnss;
        this.commission = commission;
    }

    public String getNumCnss() { return numCnss; }
    public Double getCommission() { return commission; }

    @Override
    public Map<String, String> getInfosSpecifiques() {
        var infos = new LinkedHashMap<String, String>();
        infos.put("N° CNSS", numCnss == null ? "—" : numCnss);
        infos.put("Commission", commission == null ? "—" : commission + " %");
        return infos;
    }
}