
package ma.dentalsoft.ui.components.forms;

import com.formdev.flatlaf.FlatClientProperties;
import ma.dentalsoft.ui.theme.UiColors;
import ma.dentalsoft.ui.theme.UiTypography;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Reusable labeled form field for DentalSoft.
 *
 * Provides consistent labels, required-field indicators
 * and validation error presentation.
 *
 * This component is responsible only for presentation.
 * Validation rules belong to the application's
 * controller or validation layer.
 */
public final class FormFieldPanel extends JPanel {

    private static final int VERTICAL_SPACING = 6;

    // Provisional error color until confirmed against the mockups.
    private static final Color ERROR_COLOR =
            new Color(0xC62828);

    private final JComponent input;
    private final JLabel errorLabel;

    /**
     * Creates a labeled form field.
     *
     * @param labelText field label
     * @param input Swing input component
     * @param required whether to display the required indicator
     */
    public FormFieldPanel(
            String labelText,
            JComponent input,
            boolean required) {

        super(new BorderLayout(0, VERTICAL_SPACING));

        Objects.requireNonNull(labelText, "labelText");
        this.input = Objects.requireNonNull(input, "input");

        if (labelText.isBlank()) {
            throw new IllegalArgumentException(
                    "Field label cannot be blank"
            );
        }

        setOpaque(false);

        // Field label
        JLabel label = new JLabel(labelText);
        label.setFont(UiTypography.label());
        label.setForeground(UiColors.SECONDARY);
        label.setLabelFor(input);

        JPanel labelPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 4, 0)
        );
        labelPanel.setOpaque(false);
        labelPanel.add(label);

        // Required-field indicator
        if (required) {
            JLabel requiredMarker = new JLabel("*");
            requiredMarker.setFont(UiTypography.bold(13));
            requiredMarker.setForeground(ERROR_COLOR);

            labelPanel.add(requiredMarker);
        }

        // Input styling
        input.setFont(UiTypography.body());

        // Validation message
        errorLabel = new JLabel();
        errorLabel.setFont(UiTypography.caption());
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setVisible(false);

        add(labelPanel, BorderLayout.NORTH);
        add(input, BorderLayout.CENTER);
        add(errorLabel, BorderLayout.SOUTH);
    }

    /**
     * Displays a validation error.
     *
     * @param message error message to display
     */
    public void setError(String message) {

        if (message == null || message.isBlank()) {
            clearError();
            return;
        }

        errorLabel.setText(message);
        errorLabel.setVisible(true);

        input.putClientProperty(
                FlatClientProperties.OUTLINE,
                FlatClientProperties.OUTLINE_ERROR
        );

        revalidate();
        repaint();
    }

    /**
     * Removes the current validation error.
     */
    public void clearError() {

        errorLabel.setText("");
        errorLabel.setVisible(false);

        input.putClientProperty(
                FlatClientProperties.OUTLINE,
                null
        );

        revalidate();
        repaint();
    }

    /**
     * Returns the wrapped Swing input.
     */
    public JComponent getInput() {
        return input;
    }
}
