package ma.dentalsoft;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import ma.dentalsoft.config.ApplicationContext;
import ma.dentalsoft.service.api.AuthService;
import ma.dentalsoft.session.SessionManager;
import ma.dentalsoft.ui.controllers.LoginController;
import ma.dentalsoft.ui.views.AppFrame;


public class DentalSoftApplication {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignoree) {
            // on garde le look par défaut
        }

        SwingUtilities.invokeLater(() -> {
            try {
                var authService = ApplicationContext.getInstance().getBean(AuthService.class);
                var controller = new LoginController(authService, SessionManager.getInstance());
                new AppFrame(controller).setVisible(true);
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null,
                        "Démarrage impossible :\n" + e.getMessage(),
                        "DentalSoft", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}