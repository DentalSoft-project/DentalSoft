package ma.dentalsoft.ui.view;

import javax.swing.JComponent;
import javax.swing.JFrame;
import ma.dentalsoft.ui.controllers.LoginController;

/** Fenêtre unique de l'application : alterne entre l'écran de connexion et l'écran principal. */
public class AppFrame extends JFrame {

    private final LoginController controller;

    public AppFrame(LoginController controller) {
        super("DentalSoft");
        this.controller = controller;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        afficherConnexion();
    }

    private void afficherConnexion() {
        afficher(new LoginPanel(controller, this::afficherPrincipal));
    }

    private void afficherPrincipal() {
        afficher(new MainPanel(controller.getSession(), () -> {
            controller.deconnecter();
            afficherConnexion();
        }));
    }

    private void afficher(JComponent panneau) {
        setContentPane(panneau);
        revalidate();
        repaint();
    }
}
