
package ma.dentalsoft.ui.components.tables;

import ma.dentalsoft.ui.theme.UiColors;
import ma.dentalsoft.ui.theme.UiTypography;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Objects;

/**
 * Shared table styling utility for DentalSoft.
 *
 * Applies a consistent visual appearance to standard
 * Swing JTable components without changing their data,
 * sorting or business logic.
 *
 * Visual dimensions are initial values to validate
 * against the approved Stitch mockups.
 */
public final class TableStyler {

    private static final int ROW_HEIGHT = 40;
    private static final int HEADER_HEIGHT = 40;
    private static final int HORIZONTAL_PADDING = 12;

    private TableStyler() {
        throw new AssertionError(
                "Utility class cannot be instantiated"
        );
    }

    /**
     * Applies the shared DentalSoft table appearance.
     *
     * <p>Call this method before installing any
     * screen-specific cell renderers.</p>
     *
     * @param table the JTable to style
     */
    public static void applyTo(JTable table) {

        Objects.requireNonNull(table, "table");

        configureTable(table);
        configureHeader(table.getTableHeader());

        // Default renderer for ordinary text cells.
        // Specialized renderers can be installed afterward.
        table.setDefaultRenderer(
                Object.class,
                new TextCellRenderer()
        );
    }

    /**
     * Configures the table's general appearance.
     */
    private static void configureTable(JTable table) {

        table.setBackground(UiColors.SURFACE);
        table.setForeground(UiColors.TEXT);
        table.setFont(UiTypography.body());

        table.setRowHeight(ROW_HEIGHT);

        // Subtle horizontal separators.
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UiColors.BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));

        // Consistent selection behavior.
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setSelectionBackground(
                UiColors.SELECTION_BACKGROUND
        );

        table.setSelectionForeground(UiColors.TEXT);

        table.setFillsViewportHeight(true);
    }

    /**
     * Configures column headers.
     */
    private static void configureHeader(JTableHeader header) {

        if (header == null) {
            return;
        }

        header.setFont(
                UiTypography.bold(UiTypography.LABEL_SIZE)
        );

        header.setForeground(UiColors.SECONDARY);
        header.setBackground(UiColors.SURFACE);

        header.setReorderingAllowed(false);

        Dimension size = header.getPreferredSize();

        header.setPreferredSize(
                new Dimension(size.width, HEADER_HEIGHT)
        );
    }

    /**
     * Shared renderer for ordinary table text.
     *
     * <p>Uses consistent padding and preserves selected
     * row colors and keyboard focus visibility.</p>
     */
    private static final class TextCellRenderer
            extends DefaultTableCellRenderer {

        private final Border normalBorder =
                BorderFactory.createEmptyBorder(
                        0, HORIZONTAL_PADDING,
                        0, HORIZONTAL_PADDING
                );

        private final Border focusedBorder =
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                UiColors.PRIMARY
                        ),
                        BorderFactory.createEmptyBorder(
                                0, HORIZONTAL_PADDING - 1,
                                0, HORIZONTAL_PADDING - 1
                        )
                );

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            super.getTableCellRendererComponent(
                    table,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column
            );

            setFont(UiTypography.body());
            setHorizontalAlignment(SwingConstants.LEFT);

            setBorder(
                    hasFocus ? focusedBorder : normalBorder
            );

            return this;
        }
    }
}
