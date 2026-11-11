
package ma.dentalsoft.ui.components.buttons;

import com.formdev.flatlaf.FlatClientProperties;
import ma.dentalsoft.ui.theme.UiColors;

import javax.swing.JButton;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard DentalSoft button styles.
 *
 * Colors follow the approved visual identity.
 * Additional shades and dimensions are initial values
 * to validate against the Stitch mockups.
 */
public final class ButtonStyles {

    private static final Color WHITE = Color.WHITE;

    // Provisional neutral from the visual style guide.
    private static final Color INVERTED_BACKGROUND =
            new Color(0x252B30);

    public static final ButtonStyle PRIMARY = button ->
            applyStyle(
                    button,
                    UiColors.PRIMARY,
                    WHITE,
                    UiColors.PRIMARY
            );

    public static final ButtonStyle SECONDARY = button ->
            applyStyle(
                    button,
                    UiColors.ACCENT,
                    UiColors.PRIMARY.darker(),
                    UiColors.ACCENT
            );

    public static final ButtonStyle INVERTED = button ->
            applyStyle(
                    button,
                    INVERTED_BACKGROUND,
                    WHITE,
                    INVERTED_BACKGROUND
            );

    public static final ButtonStyle OUTLINED = button ->
            applyStyle(
                    button,
                    WHITE,
                    UiColors.PRIMARY,
                    UiColors.SECONDARY
            );

    private ButtonStyles() {
        throw new AssertionError("Utility class");
    }

    private static void applyStyle(
            JButton button,
            Color background,
            Color foreground,
            Color borderColor) {

        Map<String, Object> style = new HashMap<>();

        style.put("background", background);
        style.put("foreground", foreground);

        style.put("background", background);
        style.put("foreground", foreground);
        style.put("borderColor", borderColor);

// Preserve the button's intended colors when focused
        style.put("focusedBackground", background);
        style.put("focusedForeground", foreground);

// Preserve text visibility during mouse interactions
        style.put("hoverForeground", foreground);
        style.put("pressedForeground", foreground);

        style.put("borderColor", borderColor);

        style.put(
                "hoverBackground",
                blend(background, UiColors.PRIMARY, 0.10f)
        );

        style.put(
                "pressedBackground",
                blend(background, UiColors.PRIMARY, 0.18f)
        );

        style.put("arc", 10);
        style.put("borderWidth", 1);
        style.put("focusWidth", 2);

        button.putClientProperty(
                FlatClientProperties.STYLE,
                style
        );
    }

    private static Color blend(
            Color first,
            Color second,
            float ratio) {

        float inverse = 1.0f - ratio;

        return new Color(
                Math.round(first.getRed() * inverse
                        + second.getRed() * ratio),
                Math.round(first.getGreen() * inverse
                        + second.getGreen() * ratio),
                Math.round(first.getBlue() * inverse
                        + second.getBlue() * ratio)
        );
    }
}
