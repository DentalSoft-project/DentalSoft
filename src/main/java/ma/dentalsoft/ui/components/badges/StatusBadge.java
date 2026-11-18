
package ma.dentalsoft.ui.components.badges;

import ma.dentalsoft.ui.theme.UiTypography;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

/**
 * Reusable status badge for the DentalSoft UI.
 *
 * <p>Displays a text label using a configurable
 * semantic appearance.</p>
 *
 * <p>The badge contains no business logic and can
 * represent any application status.</p>
 */
public final class StatusBadge extends JLabel {

    private BadgeStyle badgeStyle;

    /**
     * Creates a status badge.
     *
     * @param text displayed status text
     * @param style visual appearance
     */
    public StatusBadge(String text, BadgeStyle style) {

        super(
                Objects.requireNonNull(text, "text"),
                SwingConstants.CENTER
        );

        setOpaque(false);

        setFont(
                UiTypography.caption().deriveFont(Font.BOLD)
        );

        setBorder(
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        );

        setBadgeStyle(style);
    }

    /**
     * Changes the appearance of the badge.
     *
     * <p>Call on the Swing Event Dispatch Thread.</p>
     */
    public void setBadgeStyle(BadgeStyle style) {

        badgeStyle = Objects.requireNonNull(
                style, "style"
        );

        setBackground(style.getBackground());
        setForeground(style.getForeground());

        repaint();
    }

    /**
     * Paints the rounded badge background and outline.
     */
    @Override
    protected void paintComponent(Graphics graphics) {

        if (getWidth() > 2 && getHeight() > 2) {

            Graphics2D g2 = (Graphics2D) graphics.create();

            try {
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                float arc = Math.min(
                        getWidth() - 2f,
                        getHeight() - 2f
                );

                RoundRectangle2D shape =
                        new RoundRectangle2D.Float(
                                0.5f,
                                0.5f,
                                getWidth() - 1f,
                                getHeight() - 1f,
                                arc,
                                arc
                        );

                // Background
                g2.setColor(badgeStyle.getBackground());
                g2.fill(shape);

                // Border
                g2.setColor(badgeStyle.getBorder());
                g2.setStroke(new BasicStroke(1f));
                g2.draw(shape);

            } finally {
                g2.dispose();
            }
        }

        super.paintComponent(graphics);
    }
}
