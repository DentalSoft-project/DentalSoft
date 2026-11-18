package ma.dentalsoft;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import ma.dentalsoft.config.ApplicationContext;
import ma.dentalsoft.service.api.AssuranceService;
import ma.dentalsoft.service.api.AuthService;
import ma.dentalsoft.service.api.PatientService;
import ma.dentalsoft.session.SessionManager;
import ma.dentalsoft.ui.controllers.LoginController;
import ma.dentalsoft.ui.controllers.PatientController;
import ma.dentalsoft.ui.theme.UiTheme;
import ma.dentalsoft.ui.views.AppFrame;


public class DentalSoftApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UiTheme.install(); // thème FlatLaf commun (design system), avant toute fenêtre
                var contexte = ApplicationContext.getInstance();
                var authService = contexte.getBean(AuthService.class);
                var controller = new LoginController(authService, SessionManager.getInstance());
                var patientController = new PatientController(
                        contexte.getBean(PatientService.class),
                        contexte.getBean(AssuranceService.class),
                        SessionManager.getInstance());
                new AppFrame(controller, patientController).setVisible(true);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null,
                        "Démarrage impossible :\n" + e.getMessage(),
                        "DentalSoft", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}