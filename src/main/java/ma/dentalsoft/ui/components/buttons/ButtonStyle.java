package ma.dentalsoft.ui.components.buttons;

import javax.swing.JButton;

/**
 * Contract for applying a visual style to a Swing button.
 */
@FunctionalInterface
public interface ButtonStyle {

    void applyTo(JButton button);
}
