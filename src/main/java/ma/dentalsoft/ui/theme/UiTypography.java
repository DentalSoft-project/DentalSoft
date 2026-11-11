
package ma.dentalsoft.ui.theme;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Centralized typography configuration for DentalSoft.
 *
 * Defines the font family, typography scale and font styles
 * used throughout the application's Swing interface.
 *
 * Inter is the preferred font, as specified in the
 * DentalSoft visual identity. If Inter is unavailable,
 * Java's logical SansSerif font is used.
 *
 */
public final class UiTypography {

    // Font families
    private static final String PREFERRED_FAMILY = "Inter";
    private static final String FALLBACK_FAMILY = Font.SANS_SERIF;

    private static final String FONT_FAMILY = resolveFontFamily();

    // Typography scale (initial values)
    public static final int PAGE_TITLE_SIZE = 24;
    public static final int SECTION_TITLE_SIZE = 18;
    public static final int BODY_SIZE = 14;
    public static final int LABEL_SIZE = 13;
    public static final int CAPTION_SIZE = 12;

    private UiTypography() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    /**
     * Resolves the preferred font family.
     *
     * @return Inter if installed, otherwise SansSerif
     */
    private static String resolveFontFamily() {

        String[] availableFonts = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();

        for (String family : availableFonts) {
            if (PREFERRED_FAMILY.equalsIgnoreCase(family)) {
                return family;
            }
        }

        return FALLBACK_FAMILY;
    }

    /**
     * Creates a regular font of the specified size.
     *
     * @param size font size in points
     * @return regular font
     */
    public static Font regular(int size) {
        return createFont(Font.PLAIN, size);
    }

    /**
     * Creates a bold font of the specified size.
     *
     * @param size font size in points
     * @return bold font
     */
    public static Font bold(int size) {
        return createFont(Font.BOLD, size);
    }

    /**
     * Creates the font for a main page heading.
     */
    public static Font pageTitle() {
        return bold(PAGE_TITLE_SIZE);
    }

    /**
     * Creates the font for section headings.
     */
    public static Font sectionTitle() {
        return bold(SECTION_TITLE_SIZE);
    }

    /**
     * Creates the font for normal body text.
     */
    public static Font body() {
        return regular(BODY_SIZE);
    }

    /**
     * Creates the font for form labels and field headings.
     */
    public static Font label() {
        return regular(LABEL_SIZE);
    }

    /**
     * Creates the font for captions and supporting text.
     */
    public static Font caption() {
        return regular(CAPTION_SIZE);
    }

    /**
     * Returns the resolved font family name.
     */
    public static String getFontFamily() {
        return FONT_FAMILY;
    }

    /**
     * Creates a font after validating its size.
     */
    private static Font createFont(int style, int size) {

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Font size must be greater than zero"
            );
        }

        return new Font(FONT_FAMILY, style, size);
    }
}
