
package ma.dentalsoft.ui.components.badges;

import java.awt.Color;
import java.util.Objects;

/**
 * Immutable visual style for a DentalSoft status badge.
 */
public final class BadgeStyle {

    private final Color background;
    private final Color foreground;
    private final Color border;

    public BadgeStyle(
            Color background,
            Color foreground,
            Color border) {

        this.background = Objects.requireNonNull(
                background, "background"
        );

        this.foreground = Objects.requireNonNull(
                foreground, "foreground"
        );

        this.border = Objects.requireNonNull(
                border, "border"
        );
    }

    public Color getBackground() {
        return background;
    }

    public Color getForeground() {
        return foreground;
    }

    public Color getBorder() {
        return border;
    }
}
