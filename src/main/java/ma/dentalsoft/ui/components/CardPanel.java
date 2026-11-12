
package ma.dentalsoft.ui.components;

import ma.dentalsoft.ui.theme.UiColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

/**
 * Reusable rounded content container for DentalSoft.
 *
 * Provides a consistent surface, border, corner radius,
 * and internal padding for Swing content.
 *
 * The card accepts any Swing components and layout manager.
 * It contains no application-specific business logic.
 *
 * Dimensions are initial design values to be validated
 * against the approved Stitch mockups.
 */
public class CardPanel extends JPanel {

    private static final int DEFAULT_RADIUS = 12;
    private static final int DEFAULT_PADDING = 20;

    private final int cornerRadius;

    /**
     * Creates a card with BorderLayout.
     */
    public CardPanel() {
        this(new BorderLayout());
    }

    /**
     * Creates a card with the specified layout manager.
     *
     * @param layout layout manager for the card's content
     */
    public CardPanel(LayoutManager layout) {
        this(layout, DEFAULT_PADDING, DEFAULT_RADIUS);
    }

    /**
     * Creates a card with configurable padding and radius.
     *
     * @param layout layout manager
     * @param padding internal padding in pixels
     * @param cornerRadius corner arc diameter in pixels
     */
    public CardPanel(
            LayoutManager layout,
            int padding,
            int cornerRadius) {

        super(Objects.requireNonNull(layout, "layout"));

        if (padding < 0 || cornerRadius < 0) {
            throw new IllegalArgumentException(
                    "Padding and corner radius cannot be negative"
            );
        }

        this.cornerRadius = cornerRadius;

        setOpaque(false);
        setBackground(UiColors.SURFACE);

        setBorder(new EmptyBorder(
                padding,
                padding,
                padding,
                padding
        ));
    }

    /**
     * Paints the rounded card background and border.
     */
    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        if (getWidth() <= 1 || getHeight() <= 1) {
            return;
        }

        Graphics2D g2 = (Graphics2D) graphics.create();

        try {
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            float width = getWidth() - 1f;
            float height = getHeight() - 1f;

            RoundRectangle2D.Float shape =
                    new RoundRectangle2D.Float(
                            0.5f,
                            0.5f,
                            width - 1f,
                            height - 1f,
                            cornerRadius,
                            cornerRadius
                    );

            // Card surface
            g2.setColor(getBackground());
            g2.fill(shape);

            // Subtle outline
            g2.setColor(UiColors.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(shape);

        } finally {
            g2.dispose();
        }
    }
}
