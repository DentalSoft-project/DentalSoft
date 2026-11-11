package ma.dentalsoft.ui.view;

import ma.dentalsoft.security.SessionManager;
import ma.dentalsoft.dto.DossierMedicalDTO;
import javax.swing.*;
import java.awt.*;

public class TestSecurityFrame extends JFrame {

    public TestSecurityFrame() {
        setTitle("Test S1 - Sécurité et DTO (Rania)");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Création d'un faux dossier
        DossierMedicalDTO dossier = new DossierMedicalDTO(1L, 10L, "Allergie à la pénicilline (Secret)");

        JLabel lblRole = new JLabel("Rôle actuel : ");
        JTextField txtRole = new JTextField(10);

        JLabel lblNotes = new JLabel("Notes cliniques : ");
        JTextField txtNotes = new JTextField(20);
        txtNotes.setEditable(false); // L'utilisateur ne peut pas modifier

        JButton btnSecretaire = new JButton("Simuler Secrétaire");
        JButton btnMedecin = new JButton("Simuler Médecin");

        // Action quand on clique sur Secrétaire
        btnSecretaire.addActionListener(e -> {
            SessionManager.login("secretaire", "SECRETAIRE");
            txtRole.setText("SECRETAIRE");
            // Le DTO renvoie null car la secrétaire n'a pas le droit
            txtNotes.setText(String.valueOf(dossier.getObservationsCliniques()));
        });

        // Action quand on clique sur Médecin
        btnMedecin.addActionListener(e -> {
            SessionManager.login("docteur", "MEDECIN");
            txtRole.setText("MEDECIN");
            // Le DTO renvoie le texte car le médecin a le droit
            txtNotes.setText(dossier.getObservationsCliniques());
        });

        add(lblRole); add(txtRole);
        add(lblNotes); add(txtNotes);
        add(btnSecretaire); add(btnMedecin);
    }

    // Juste pour tester
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TestSecurityFrame().setVisible(true);
        });
    }
}
