package ma.dentalsoft.shared.formatting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Shared display formatting utilities for DentalSoft.
 *
 * Provides consistent presentation of dates, monetary
 * amounts in Moroccan dirhams, and telephone numbers.
 *
 * This class handles presentation only. Input validation,
 * monetary calculations and business rules belong to
 * their respective application layers.
 */
public final class DisplayFormats {

    private static final Locale LOCALE =
            Locale.forLanguageTag("fr-MA");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", LOCALE);

    private static final String CURRENCY_SUFFIX = " DH";

    private DisplayFormats() {
        throw new AssertionError(
                "Utility class cannot be instantiated"
        );
    }

    /**
     * Formats a date using the required dd/MM/yyyy pattern.
     *
     * Example: 2026-11-11 -> 11/11/2026
     *
     * @param date date to format
     * @return formatted date
     */
    public static String formatDate(LocalDate date) {

        Objects.requireNonNull(date, "date");

        return DATE_FORMATTER.format(date);
    }

    /**
     * Formats an amount in Moroccan dirhams.
     *
     * Examples:
     * 1250.00 -> "1 250,00 DH"
     * 75.50   -> "75,50 DH"
     *
     * <p>Requires an amount representable with two
     * decimal places. No silent rounding is performed.</p>
     *
     * @param amount monetary amount
     * @return formatted amount in DH
     */
    public static String formatAmount(BigDecimal amount) {

        Objects.requireNonNull(amount, "amount");

        DecimalFormatSymbols symbols =
                new DecimalFormatSymbols(LOCALE);

        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator(' ');

        DecimalFormat formatter =
                new DecimalFormat("#,##0.00", symbols);

        formatter.setRoundingMode(RoundingMode.UNNECESSARY);

        return formatter.format(amount) + CURRENCY_SUFFIX;
    }

    /**
     * Formats recognized Moroccan telephone numbers.
     *
     * Examples:
     * "0612345678"      -> "06 12 34 56 78"
     * "+212612345678"   -> "+212 6 12 34 56 78"
     *
     * <p>Unrecognized formats are returned trimmed
     * without modification. This method does not
     * validate whether a number is assigned or active.</p>
     *
     * @param phone telephone number
     * @return formatted telephone number
     */
    public static String formatPhone(String phone) {

        Objects.requireNonNull(phone, "phone");

        String normalized = phone.trim()
                .replaceAll("[\\p{Z}\\s.\\-]", "");

        // Moroccan national format: 10 digits
        if (normalized.matches("0[0-9]{9}")) {

            return normalized.substring(0, 2) + " "
                    + normalized.substring(2, 4) + " "
                    + normalized.substring(4, 6) + " "
                    + normalized.substring(6, 8) + " "
                    + normalized.substring(8, 10);
        }

        // Moroccan international format: +212 + 9 digits
        if (normalized.matches("\\+212[0-9]{9}")) {

            return "+212 "
                    + normalized.substring(4, 5) + " "
                    + normalized.substring(5, 7) + " "
                    + normalized.substring(7, 9) + " "
                    + normalized.substring(9, 11) + " "
                    + normalized.substring(11, 13);
        }

        return phone.trim();
    }
}
