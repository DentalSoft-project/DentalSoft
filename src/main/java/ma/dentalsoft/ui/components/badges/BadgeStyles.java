
package ma.dentalsoft.ui.components.badges;

import ma.dentalsoft.ui.theme.UiColors;

import java.awt.Color;

/**
 * Standard semantic badge styles for DentalSoft.
 *
 * <p>These styles represent visual meaning, not
 * application-specific business statuses.</p>
 */
public final class BadgeStyles {

    /**
     * Successful or confirmed states.
     */
    public static final BadgeStyle SUCCESS = new BadgeStyle(
            new Color(0xE7F7F3),
            new Color(0x086A58),
            new Color(0xC3EBE1)
    );

    /**
     * Informational or upcoming states.
     */
    public static final BadgeStyle INFO = new BadgeStyle(
            new Color(0xEAF1F8),
            UiColors.PRIMARY,
            new Color(0xD0E0EE)
    );

    /**
     * Pending states or warnings.
     */
    public static final BadgeStyle WARNING = new BadgeStyle(
            new Color(0xFFF5E6),
            new Color(0x8C5609),
            new Color(0xF4DCB5)
    );

    /**
     * Errors, cancellations or critical risks.
     */
    public static final BadgeStyle DANGER = new BadgeStyle(
            new Color(0xFCEAED),
            new Color(0xA52331),
            new Color(0xF3CDD4)
    );

    /**
     * Neutral or inactive states.
     */
    public static final BadgeStyle NEUTRAL = new BadgeStyle(
            new Color(0xF0F2F5),
            UiColors.SECONDARY,
            new Color(0xDDE3EA)
    );

    private BadgeStyles() {
        throw new AssertionError("Utility class");
    }
}
