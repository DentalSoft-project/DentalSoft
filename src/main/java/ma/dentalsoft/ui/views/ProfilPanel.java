package ma.dentalsoft.ui.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import ma.dentalsoft.domain.Permission;
import ma.dentalsoft.session.SessionManager;

/** Vue « Profil personnel » (T07) : affiche les informations de l'utilisateur connecté. */
public class ProfilPanel extends JPanel {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JPanel infos = new JPanel(new GridBagLayout());
    private int ligne = 0;

    public ProfilPanel(SessionManager session) {
        setLayout(new BorderLayout(0, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        var titre = new JLabel("Mon profil");
        titre.setFont(titre.getFont().deriveFont(Font.BOLD, 22f));
        add(titre, BorderLayout.NORTH);

        var u = session.getUtilisateur();
        if (u == null) {
            add(new JLabel("Aucun utilisateur connecté."), BorderLayout.CENTER);
            return;
        }

        ajouter("Nom complet", u.getNomComplet());
        ajouter("Identifiant", u.getLogin());
        ajouter("Rôle", u.getRole().getLibelle());
        ajouter("Statut du compte", u.isActif() ? "Actif" : "Désactivé");
        u.getInfosSpecifiques().forEach(this::ajouter);
        ajouter("Session ouverte le", session.getDebut().format(FORMAT));
        add(infos, BorderLayout.CENTER);

        var liste = session.getPermissions().stream()
                .map(Permission::getLibelle)
                .sorted()
                .map(p -> "• " + p)
                .collect(Collectors.joining("\n"));
        var zone = new JTextArea(liste.isEmpty() ? "Aucune permission" : liste);
        zone.setEditable(false);
        zone.setOpaque(false);
        var scroll = new JScrollPane(zone);
        scroll.setBorder(BorderFactory.createTitledBorder("Mes permissions"));
        scroll.setPreferredSize(new java.awt.Dimension(300, 140));
        add(scroll, BorderLayout.SOUTH);
    }

    private void ajouter(String libelle, String valeur) {
        var c = new GridBagConstraints();
        c.gridy = ligne++;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 0, 4, 20);

        var l = new JLabel(libelle + " :");
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        c.gridx = 0;
        infos.add(l, c);

        c.gridx = 1;
        c.weightx = 1;
        infos.add(new JLabel(valeur), c);
    }
}
class ProfilPanel {
}
