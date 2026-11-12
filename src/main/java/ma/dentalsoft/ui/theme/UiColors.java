
package ma.dentalsoft.ui.theme;

import java.awt.Color;

/**
 * Centralized color palette for the DentalSoft application.
 *
 * Contains the official brand colors defined in Chapter 3
 * of the DentalSoft specification document.
 *
 * UI components should reference these constants instead of
 * declaring their own color values.
 *
 */
public final class UiColors {

    /**
     * Primary brand color.
     * Used for navigation, headings, primary actions
     * and active elements.
     */
    public static final Color PRIMARY = new Color(0x1F4E79);

    /**
     * Accent color.
     * Used for creation actions, confirmations,
     * positive states and interface accents.
     */
    public static final Color ACCENT = new Color(0x14B8A6);

    /**
     * Secondary color.
     * Used for supporting text, labels,
     * informational elements and secondary details.
     */
    public static final Color SECONDARY = new Color(0x64748B);

    /**
     * Main application background color.
     * Used for the background of pages and content areas.
     */
    public static final Color BACKGROUND = new Color(0xF6F8FB);

    /**
     * Prevents instantiation of this utility class.
     */
    /**
     * Background of content cards and form surfaces.
     */
    public static final Color SURFACE = Color.WHITE;

    /**
     * Subtle border for cards and containers.
     * Provisional value to validate against Stitch.
     */
    public static final Color BORDER = new Color(0xE3E8EE);
    /**
     * Main text color for content and data.
     * Provisional value to verify against Stitch.
     */
    public static final Color TEXT = new Color(0x1F2937);

    /**
     * Highlight for selected table rows.
     * Provisional value to verify against Stitch.
     */
    public static final Color SELECTION_BACKGROUND = new Color(0xE7F5F6);

    private UiColors() {
        throw new AssertionError("Utility class cannot be instantiated");
    }
}
