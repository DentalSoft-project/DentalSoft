
package ma.dentalsoft.ui.components.buttons;

import com.formdev.flatlaf.FlatClientProperties;
import ma.dentalsoft.ui.theme.UiTypography;

import javax.swing.JButton;
import java.awt.Cursor;
import java.awt.Insets;
import java.util.Objects;

/**
 * Factory for consistently styled DentalSoft buttons.
 *
 * New button styles can be introduced by providing
 * another ButtonStyle implementation without modifying
 * this factory.
 */
public final class ButtonFactory {

    private static final int MINIMUM_HEIGHT = 36;

    private ButtonFactory() {
        throw new AssertionError("Utility class");
    }

    public static JButton create(
            String text,
            ButtonStyle style) {

        Objects.requireNonNull(text, "Button text is required");
        Objects.requireNonNull(style, "Button style is required");

        JButton button = new JButton(text);

        button.setFont(
                UiTypography.bold(UiTypography.LABEL_SIZE)
        );

        button.setMargin(new Insets(8, 14, 8, 14));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.putClientProperty(
                FlatClientProperties.MINIMUM_HEIGHT,
                MINIMUM_HEIGHT
        );

        style.applyTo(button);

        return button;
    }
}
