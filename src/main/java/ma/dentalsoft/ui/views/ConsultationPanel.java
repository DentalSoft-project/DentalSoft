package ma.dentalsoft.ui.views;

import ma.dentalsoft.domain.Consultation;
import ma.dentalsoft.security.SessionManager;
import ma.dentalsoft.service.api.ConsultationService;
import ma.dentalsoft.service.impl.ConsultationServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ConsultationPanel extends JPanel {

    private ConsultationService service;
    private Consultation consultationCourante;

    private JTextField txtActe;
    private JTextField txtDent;
    private JTextField txtPrix;
    private JTextArea txtRecapitulatif;

    public ConsultationPanel() {
        this.service = new ConsultationServiceImpl();
        setLayout(new BorderLayout());

        // 1. Initialisation d'une consultation de test
        SessionManager.login("docteur", "MEDECIN");
        consultationCourante = service.demarrerConsultation(10L, 1L);

        // 2. Formulaire d'ajout d'intervention
        JPanel panelForm = new JPanel(new GridLayout(4, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Ajouter une intervention"));

        panelForm.add(new JLabel("Acte :"));
        txtActe = new JTextField("Détartrage");
        panelForm.add(txtActe);

        panelForm.add(new JLabel("Dent (ex: 11) :"));
        txtDent = new JTextField();
        panelForm.add(txtDent);

        panelForm.add(new JLabel("Prix (MAD) :"));
        txtPrix = new JTextField("450.00");
        panelForm.add(txtPrix);

        JButton btnAjouter = new JButton("Ajouter l'acte");
        panelForm.add(new JLabel()); // Espace vide
        panelForm.add(btnAjouter);

        // 3. Zone de récapitulatif
        txtRecapitulatif = new JTextArea(10, 30);
        txtRecapitulatif.setEditable(false);
        txtRecapitulatif.setText("--- Récapitulatif de la consultation ---\n");

        // 4. Action du bouton
        btnAjouter.addActionListener(e -> {
            try {
                String libelle = txtActe.getText();
                String dent = txtDent.getText();
                BigDecimal prix = new BigDecimal(txtPrix.getText());

                service.ajouterIntervention(consultationCourante.getId(), 1L, libelle, dent, 1, prix);

                // Mise à jour de l'affichage
                BigDecimal total = service.calculerTotalConsultation(consultationCourante.getId());
                txtRecapitulatif.append("- " + libelle + " (Dent " + dent + ") : " + prix + " MAD\n");
                txtRecapitulatif.append(">>> TOTAL ACTUEL : " + total + " MAD\n\n");

                txtDent.setText(""); // Reset
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Ajout à la fenêtre
        add(panelForm, BorderLayout.NORTH);
        add(new JScrollPane(txtRecapitulatif), BorderLayout.CENTER);
    }
}