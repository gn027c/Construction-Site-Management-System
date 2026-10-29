package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Objects;

/** Shared strict date and timestamp formats. No legacy format conversion. */
public final class DateTimeHelper {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/uuuu", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    // uuuu supports strict calendar validation without requiring an era.
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);

    private DateTimeHelper() { }

    public static String formatDate(LocalDate value) {
        Objects.requireNonNull(value, "date");
        checkYear(value.getYear());
        return value.format(DATE_FORMATTER);
    }

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

    public static String formatTimestamp(LocalDateTime value) {
        Objects.requireNonNull(value, "timestamp");
        checkYear(value.getYear());
        return value.format(TIMESTAMP_FORMATTER);
    }

    /** Accept exactly yyyy-MM-dd HH:mm:ss, with a real calendar date and time. */
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
