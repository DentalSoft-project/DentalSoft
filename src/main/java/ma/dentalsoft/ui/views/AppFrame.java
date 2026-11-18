package ma.dentalsoft.ui.views;

import javax.swing.JComponent;
import javax.swing.JFrame;
import ma.dentalsoft.ui.controllers.LoginController;
import ma.dentalsoft.ui.controllers.PatientController;

/** Fenêtre unique de l'application : alterne entre l'écran de connexion et l'écran principal. */
public class AppFrame extends JFrame {

    private final LoginController controller;
    private final PatientController patientController;

    public AppFrame(LoginController controller, PatientController patientController) {
        super("DentalSoft");
        this.controller = controller;
        this.patientController = patientController;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);
        afficherConnexion();
    }

    private void afficherConnexion() {
        afficher(new ma.dentalsoft.ui.views.LoginPanel(controller, this::afficherPrincipal));
    }

    private void afficherPrincipal() {
        afficher(new MainPanel(controller.getSession(), patientController, () -> {
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
