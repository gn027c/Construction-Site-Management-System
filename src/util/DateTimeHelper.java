package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Objects;

/**
 * Provides the application's shared, strict date and timestamp formats.
 *
 * <p>Dates use {@code dd/MM/yyyy}; timestamps use {@code yyyy-MM-dd HH:mm:ss}.
 * Only real calendar values with years from 0001 through 9999 are accepted.
 * Parsing does not trim input or convert alternative formats.
 */
public final class DateTimeHelper {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Use uuuu for strict calendar validation without requiring an era.
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
            "dd/MM/uuuu",
            Locale.ROOT
    ).withResolverStyle(ResolverStyle.STRICT);

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "uuuu-MM-dd HH:mm:ss",
            Locale.ROOT
    ).withResolverStyle(ResolverStyle.STRICT);

    private DateTimeHelper() {
    }

    /**
     * Formats a date using the shared date pattern.
     *
     * @param value a non-null date with a year from 0001 through 9999
     * @return the formatted date
     * @throws IllegalArgumentException if the year is outside the supported range
     * @throws NullPointerException if the value is null
     */
    public static String formatDate(LocalDate value) {
        Objects.requireNonNull(value, "date");
        checkYear(value.getYear());
        return value.format(DATE_FORMATTER);
    }

    /**
     * Parses an exact date and rejects impossible calendar dates.
     *
     * @param value a date in {@code dd/MM/yyyy} format without surrounding whitespace
     * @return the parsed date
     * @throws DateTimeParseException if the value or year is invalid
     */
    public static LocalDate parseDate(String value) {
        if (value == null || value.length() != DATE_FORMAT.length()) {
            throw new DateTimeParseException("Expected " + DATE_FORMAT, value == null ? "" : value, 0);
        }
        LocalDate parsed = LocalDate.parse(value, DATE_FORMATTER);
        if (parsed.getYear() < 1) {
            throw new DateTimeParseException("Date year must be between 0001 and 9999", value, 0);
        }
        return parsed;
    }

    private static void checkYear(int year) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException("Year must be between 0001 and 9999.");
        }
    }

    /**
     * Formats local date and time to second precision using the shared timestamp pattern.
     *
     * @param value a non-null timestamp with a year from 0001 through 9999
     * @return the formatted timestamp; fractional seconds are omitted
     * @throws IllegalArgumentException if the year is outside the supported range
     * @throws NullPointerException if the value is null
     */
    public static String formatTimestamp(LocalDateTime value) {
        Objects.requireNonNull(value, "timestamp");
        checkYear(value.getYear());
        return value.format(TIMESTAMP_FORMATTER);
    }

    /**
     * Parses an exact timestamp and rejects impossible dates or times.
     *
     * @param value a timestamp in {@code yyyy-MM-dd HH:mm:ss} format
     * @return the parsed local date and time, without a time zone
     * @throws DateTimeParseException if the value or year is invalid
     */
    public static LocalDateTime parseTimestamp(String value) {
        if (value == null || value.length() != TIMESTAMP_FORMAT.length()) {
            throw new DateTimeParseException("Expected " + TIMESTAMP_FORMAT, value == null ? "" : value, 0);
        }
        LocalDateTime parsed = LocalDateTime.parse(value, TIMESTAMP_FORMATTER);
        if (parsed.getYear() < 1) {
            throw new DateTimeParseException("Timestamp year must be between 0001 and 9999", value, 0);
        }
        return parsed;
    }
}
