
package ma.dentalsoft.ui.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * Global Swing theme configuration for DentalSoft.
 *
 * Initializes FlatLaf and applies the shared visual
 * defaults defined by the DentalSoft design system.
 *
 * This class must be initialized once before creating
 * any Swing components.
 *
 * Component-specific styles are managed separately
 * by the reusable UI component classes.
 */
public final class UiTheme {

    // Initial design values to validate against Stitch mockups
    private static final int COMPONENT_ARC = 12;
    private static final int BUTTON_ARC = 12;
    private static final int TEXT_FIELD_ARC = 10;
    private static final int FOCUS_WIDTH = 2;
    private static final int TABLE_ROW_HEIGHT = 36;

    private UiTheme() {
        throw new AssertionError(
                "Utility class cannot be instantiated"
        );
    }

    /**
     * Installs the shared DentalSoft theme.
     *
     * Must be called before the application creates
     * its Swing windows and components.
     */
    public static void install() {

        // Initialize the application's Look and Feel.
        FlatLightLaf.setup();

        configureTypography();
        configureComponents();
        configureTables();
    }

    /**
     * Applies the default font to Swing components.
     */
    private static void configureTypography() {

        UIManager.put(
                "defaultFont",
                new FontUIResource(UiTypography.body())
        );
    }

    /**
     * Configures common component appearance.
     */
    private static void configureComponents() {

        // Main application background
        UIManager.put(
                "Panel.background",
                UiColors.BACKGROUND
        );

        // Rounded components
        UIManager.put("Component.arc", COMPONENT_ARC);
        UIManager.put("Button.arc", BUTTON_ARC);
        UIManager.put("TextComponent.arc", TEXT_FIELD_ARC);

        // Keyboard focus indication
        UIManager.put("Component.focusWidth", FOCUS_WIDTH);
    }

    /**
     * Configures default table presentation.
     */
    private static void configureTables() {

        UIManager.put(
                "Table.rowHeight",
                TABLE_ROW_HEIGHT
        );
    }
}
