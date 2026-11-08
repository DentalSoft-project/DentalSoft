package ma.dentalsoft.ui.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import ma.dentalsoft.domain.Permission;
import ma.dentalsoft.session.SessionManager;

/**
 * Fenêtre principale après connexion. Le menu est construit selon les permissions du rôle :
 * navigation par rôle (US-M1-04) et permissions sur menus et boutons (US-M1-05).
 */
public class MainPanel extends JPanel {

    private static final Color COULEUR = new Color(0x0E7C86);

    /** Une entrée de menu ; « requise » = null signifie accessible à tout utilisateur connecté. */
    private record Entree(String cle, String libelle, Permission requise) {}

    private static final List<Entree> ENTREES = List.of(
            new Entree("ACCUEIL", "Accueil", null),
            new Entree("PATIENTS", "Patients", Permission.GERER_PATIENTS),
            new Entree("AGENDA", "Agenda", Permission.GERER_AGENDA),
            new Entree("CONSULTATIONS", "Consultations", Permission.GERER_CONSULTATIONS),
            new Entree("CAISSE", "Caisse", Permission.GERER_CAISSE),
            new Entree("UTILISATEURS", "Utilisateurs", Permission.GERER_UTILISATEURS),
            new Entree("ROLES", "Rôles et permissions", Permission.GERER_ROLES),
            new Entree("PROFIL", "Mon profil", null));

    private final CardLayout cartes = new CardLayout();
    private final JPanel contenu = new JPanel(cartes);

    public MainPanel(SessionManager session, Runnable apresDeconnexion) {
        setLayout(new BorderLayout());
        add(creerEntete(session, apresDeconnexion), BorderLayout.NORTH);

        var menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        for (var entree : ENTREES) {
            if (entree.requise() != null && !session.peut(entree.requise())) {
                continue; // pas la permission : l'entrée n'existe pas pour cet utilisateur
            }
            contenu.add(creerPage(entree, session), entree.cle());

            var bouton = new JButton(entree.libelle());
            bouton.setAlignmentX(LEFT_ALIGNMENT);
            bouton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            bouton.addActionListener(e -> cartes.show(contenu, entree.cle()));
            menu.add(bouton);
            menu.add(Box.createVerticalStrut(8));
        }

        add(menu, BorderLayout.WEST);
        add(contenu, BorderLayout.CENTER);
        cartes.show(contenu, "ACCUEIL");
    }

    private JPanel creerEntete(SessionManager session, Runnable apresDeconnexion) {
        var entete = new JPanel(new BorderLayout());
        entete.setBackground(COULEUR);
        entete.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        var titre = new JLabel("DentalSoft — Better Smile.");
        titre.setForeground(Color.WHITE);
        titre.setFont(titre.getFont().deriveFont(Font.BOLD, 18f));
        entete.add(titre, BorderLayout.WEST);

        var u = session.getUtilisateur();
        var droite = new JPanel();
        droite.setOpaque(false);
        var qui = new JLabel(u.getNomComplet() + "  (" + u.getRole().getLibelle() + ")");
        qui.setForeground(Color.WHITE);
        var deconnexion = new JButton("Déconnexion");
        deconnexion.addActionListener(e -> apresDeconnexion.run());
        droite.add(qui);
        droite.add(deconnexion);
        entete.add(droite, BorderLayout.EAST);
        return entete;
    }

    private JComponent creerPage(Entree entree, SessionManager session) {
        return switch (entree.cle()) {
            case "ACCUEIL" -> texteCentre("Bienvenue, " + session.getUtilisateur().getPrenom() + " !");
            case "PROFIL" -> new ProfilPanel(session);
            default -> texteCentre("Module « " + entree.libelle() + " » : à venir.");
        };
    }

    private JComponent texteCentre(String texte) {
        var label = new JLabel(texte, SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 20f));
        return label;
    }
}
