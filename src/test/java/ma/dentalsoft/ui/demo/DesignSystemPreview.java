
package ma.dentalsoft.ui.demo;

import com.formdev.flatlaf.FlatClientProperties;

import ma.dentalsoft.ui.components.buttons.ButtonFactory;
import ma.dentalsoft.ui.components.buttons.ButtonStyle;
import ma.dentalsoft.ui.components.buttons.ButtonStyles;
import ma.dentalsoft.ui.theme.UiColors;
import ma.dentalsoft.ui.theme.UiTheme;
import ma.dentalsoft.ui.theme.UiTypography;
import ma.dentalsoft.ui.components.forms.FormFieldPanel;
import ma.dentalsoft.ui.components.CardPanel;
import ma.dentalsoft.ui.components.tables.TableStyler;
import javax.swing.table.DefaultTableModel;
import ma.dentalsoft.ui.components.badges.BadgeStyles;
import ma.dentalsoft.ui.components.badges.StatusBadge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Standalone visual preview of DentalSoft UI components.
 *
 * <p>Used to verify the shared design system against
 * the approved Stitch mockups before integration.</p>
 *
 * <p>This class is not part of the production workflow.</p>
 */
public final class DesignSystemPreview {

    private DesignSystemPreview() {
        throw new AssertionError("Utility class");
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            UiTheme.install();
            createWindow();
        });
    }

    private static void createWindow() {

        JFrame frame = new JFrame(
                "DentalSoft - Design System Preview"
        );

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        frame.setContentPane(createContent());
        frame.setMinimumSize(new Dimension(700, 420));
        frame.setSize(850, 480);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    private static JPanel createContent() {

        JPanel root = new JPanel(new BorderLayout(0, 24));
        root.setBackground(UiColors.BACKGROUND);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(
                new BoxLayout(header, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("DentalSoft Design System");
        title.setFont(UiTypography.pageTitle());
        title.setForeground(UiColors.PRIMARY);

        JLabel subtitle = new JLabel(
                "Aperçu des composants Swing partagés"
        );
        subtitle.setFont(UiTypography.body());
        subtitle.setForeground(UiColors.SECONDARY);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        // Preview card
        CardPanel card = new CardPanel(
                new BorderLayout(0, 20),
                24,
                12
        );

        JLabel sectionTitle = new JLabel("Boutons");
        sectionTitle.setFont(UiTypography.sectionTitle());
        sectionTitle.setForeground(UiColors.PRIMARY);

        card.add(sectionTitle, BorderLayout.NORTH);

        // GridBagLayout preserves preferred button dimensions
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 24);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;

        String[] headers = {
                "Variante", "Actif", "Désactivé"
        };

        for (int col = 0; col < headers.length; col++) {
            gbc.gridx = col;
            gbc.gridy = 0;
            grid.add(createHeader(headers[col]), gbc);
        }

        JLabel feedback = new JLabel(
                "Testez les boutons avec la souris et le clavier."
        );
        feedback.setFont(UiTypography.caption());
        feedback.setForeground(UiColors.SECONDARY);

        addButtonRow(
                grid, 1, "Primary", "Enregistrer",
                ButtonStyles.PRIMARY, feedback
        );

        addButtonRow(
                grid, 2, "Secondary", "Nouveau patient",
                ButtonStyles.SECONDARY, feedback
        );

        addButtonRow(
                grid, 3, "Inverted", "Confirmer",
                ButtonStyles.INVERTED, feedback
        );

        addButtonRow(
                grid, 4, "Outlined", "Annuler",
                ButtonStyles.OUTLINED, feedback
        );

        // Keep the button showcase at the top-left
        JPanel previewContainer = new JPanel(
                new BorderLayout()
        );
        previewContainer.setOpaque(false);
        previewContainer.add(grid, BorderLayout.NORTH);


        JPanel showcase = new JPanel();
        showcase.setLayout(
                new BoxLayout(showcase, BoxLayout.Y_AXIS)
        );
        showcase.setOpaque(false);

        previewContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        showcase.add(previewContainer);

        showcase.add(Box.createVerticalStrut(24));

        JPanel formPreview = createFormPreview();
        formPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        showcase.add(formPreview);

        showcase.add(Box.createVerticalStrut(24));

        JPanel tablePreview = createTablePreview();
        tablePreview.setAlignmentX(Component.LEFT_ALIGNMENT);

        showcase.add(tablePreview);

        showcase.add(Box.createVerticalStrut(24));

        JPanel badgePreview = createBadgePreview();
        badgePreview.setAlignmentX(Component.LEFT_ALIGNMENT);

        badgePreview.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        badgePreview.getPreferredSize().height
                )
        );

        showcase.add(badgePreview);

        JScrollPane scrollPane = new JScrollPane(showcase);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);

        card.add(feedback, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);

        return root;
    }


    private static JLabel createHeader(String text) {

        JLabel label = new JLabel(text);
        label.setFont(UiTypography.label());
        label.setForeground(UiColors.SECONDARY);

        return label;
    }


    private static void addButtonRow(
            JPanel container,
            int row,
            String variant,
            String text,
            ButtonStyle style,
            JLabel feedback) {

        JLabel variantLabel = new JLabel(variant);
        variantLabel.setFont(UiTypography.body());
        variantLabel.setForeground(UiColors.PRIMARY);

        JButton enabled = ButtonFactory.create(text, style);

        enabled.addActionListener(event ->
                feedback.setText(
                        "Bouton sélectionné : " + variant
                )
        );

        JButton disabled = ButtonFactory.create(text, style);
        disabled.setEnabled(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(8, 12, 8, 24);

        gbc.gridx = 0;
        container.add(variantLabel, gbc);

        gbc.gridx = 1;
        container.add(enabled, gbc);

        gbc.gridx = 2;
        container.add(disabled, gbc);
    }

    private static JPanel createFormPreview() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 24, 8)
        );

        panel.setOpaque(false);

        // Normal field
        JTextField nameInput = new JTextField(20);

        nameInput.putClientProperty(
                FlatClientProperties.PLACEHOLDER_TEXT,
                "Ex. Salma Benjelloun"
        );

        FormFieldPanel nameField = new FormFieldPanel(
                "Nom du patient",
                nameInput,
                true
        );

        // Invalid field
        JTextField emailInput = new JTextField(20);
        emailInput.setText("adresse-invalide");

        FormFieldPanel emailField = new FormFieldPanel(
                "Adresse e-mail",
                emailInput,
                true
        );

        emailField.setError(
                "Veuillez saisir une adresse e-mail valide."
        );

        panel.add(nameField);
        panel.add(emailField);

        return panel;
    }


    private static JPanel createTablePreview() {

        JPanel section = new JPanel(
                new BorderLayout(0, 12)
        );

        section.setOpaque(false);

        JLabel title = new JLabel("Tableau des patients");
        title.setFont(UiTypography.sectionTitle());
        title.setForeground(UiColors.PRIMARY);

        String[] columns = {
                "Patient",
                "Téléphone",
                "Statut",
                "Dernière visite"
        };

        Object[][] data = {
                {
                        "Salma Benjelloun",
                        "0612345678",
                        "Actif",
                        "05/11/2026"
                },
                {
                        "Youssef El Amrani",
                        "0623456789",
                        "Actif",
                        "08/11/2026"
                },
                {
                        "Amine Alaoui",
                        "0634567890",
                        "Inactif",
                        "20/10/2026"
                }
        };

        DefaultTableModel model = new DefaultTableModel(
                data, columns
        ) {
            @Override
            public boolean isCellEditable(
                    int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);

        TableStyler.applyTo(table);

        // Sorting is enabled by this particular screen,
        // not by the shared styling utility.
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        UiColors.BORDER
                )
        );

        scrollPane.getViewport().setBackground(
                UiColors.SURFACE
        );

        scrollPane.setPreferredSize(
                new Dimension(760, 180)
        );

        section.add(title, BorderLayout.NORTH);
        section.add(scrollPane, BorderLayout.CENTER);

        section.setPreferredSize(
                new Dimension(780, 235)
        );

        section.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 235)
        );

        return section;
    }


    private static JPanel createBadgePreview() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 8)
        );

        panel.setOpaque(false);

        panel.add(new StatusBadge(
                "Confirmé",
                BadgeStyles.SUCCESS
        ));

        panel.add(new StatusBadge(
                "À venir",
                BadgeStyles.INFO
        ));

        panel.add(new StatusBadge(
                "En attente",
                BadgeStyles.WARNING
        ));

        panel.add(new StatusBadge(
                "Annulé",
                BadgeStyles.DANGER
        ));

        panel.add(new StatusBadge(
                "Inactif",
                BadgeStyles.NEUTRAL
        ));

        return panel;
    }



}
