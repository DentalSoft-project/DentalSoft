package ma.dentalsoft.ui.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import ma.dentalsoft.domain.Utilisateur;
import ma.dentalsoft.shared.AuthentificationException;
import ma.dentalsoft.ui.controllers.LoginController;
import ma.dentalsoft.ui.views.EtatEcran;

/** Écran de connexion (fonctionnalité transversale d'authentification). */
public class LoginPanel extends JPanel {

    private static final Color COULEUR = new Color(0x0E7C86);

    private final LoginController controller;
    private final Runnable apresConnexion;

    private final JTextField champLogin = new JTextField(20);
    private final JPasswordField champMotDePasse = new JPasswordField(20);
    private final JButton boutonConnexion = new JButton("Se connecter");
    private final JLabel message = new JLabel(" ", SwingConstants.CENTER);

    public LoginPanel(LoginController controller, Runnable apresConnexion) {
        this.controller = controller;
        this.apresConnexion = apresConnexion;
        construire();
        setEtat(EtatEcran.VIDE, "");
    }

    private void construire() {
        setLayout(new GridBagLayout());
        var c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(5, 10, 5, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        var titre = new JLabel("DentalSoft", SwingConstants.CENTER);
        titre.setFont(titre.getFont().deriveFont(Font.BOLD, 34f));
        titre.setForeground(COULEUR);
        var slogan = new JLabel("Better Smile.", SwingConstants.CENTER);
        slogan.setFont(slogan.getFont().deriveFont(Font.ITALIC, 14f));

        c.gridy = 0; add(titre, c);
        c.gridy = 1; c.insets = new Insets(0, 10, 25, 10); add(slogan, c);
        c.insets = new Insets(5, 10, 0, 10);
        c.gridy = 2; add(new JLabel("Identifiant"), c);
        c.gridy = 3; c.insets = new Insets(0, 10, 10, 10); add(champLogin, c);
        c.insets = new Insets(5, 10, 0, 10);
        c.gridy = 4; add(new JLabel("Mot de passe"), c);
        c.gridy = 5; c.insets = new Insets(0, 10, 15, 10); add(champMotDePasse, c);
        c.gridy = 6; c.insets = new Insets(5, 10, 5, 10); add(boutonConnexion, c);
        c.gridy = 7; add(message, c);

        boutonConnexion.addActionListener(e -> seConnecter());
        champMotDePasse.addActionListener(e -> seConnecter());          // touche Entrée
        champLogin.addActionListener(e -> champMotDePasse.requestFocusInWindow());
    }

    private void seConnecter() {
        var login = champLogin.getText().trim();
        var motDePasse = new String(champMotDePasse.getPassword());

        if (login.isEmpty() || motDePasse.isEmpty()) {
            setEtat(EtatEcran.SAISIE_INVALIDE, "Veuillez saisir votre identifiant et votre mot de passe.");
            return;
        }

        setEtat(EtatEcran.CHARGEMENT, "Connexion en cours...");

        // La requête SQL se fait hors du thread Swing pour ne pas figer la fenêtre.
        new SwingWorker<Utilisateur, Void>() {
            @Override
            protected Utilisateur doInBackground() {
                return controller.connecter(login, motDePasse);
            }

            @Override
            protected void done() {
                try {
                    get();
                    champMotDePasse.setText("");
                    setEtat(EtatEcran.SUCCES, "Connexion réussie.");
                    apresConnexion.run();
                } catch (ExecutionException e) {
                    var cause = e.getCause();
                    if (cause instanceof AuthentificationException) {
                        setEtat(EtatEcran.ERREUR, cause.getMessage());
                    } else {
                        setEtat(EtatEcran.ERREUR, "Erreur technique : base de données indisponible ?");
                        cause.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void setEtat(EtatEcran etat, String texte) {
        message.setText(texte.isEmpty() ? " " : texte);
        message.setForeground(switch (etat) {
            case ERREUR, SAISIE_INVALIDE -> new Color(0xB00020);
            case SUCCES -> new Color(0x1B7F3B);
            default -> Color.DARK_GRAY;
        });
        var enCours = etat == EtatEcran.CHARGEMENT;
        boutonConnexion.setEnabled(!enCours);
        champLogin.setEnabled(!enCours);
        champMotDePasse.setEnabled(!enCours);
    }
}
