package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/** Shared attendance timestamp format. No legacy format conversion. */
public final class DateTimeHelper {
    // uuuu supports strict calendar validation without requiring an era.
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter
            .ofPattern("uuuu-MM-dd HH:mm:ss", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);

    private DateTimeHelper() { }

    public static String formatTimestamp(LocalDateTime value) {
        if (value.getYear() < 1 || value.getYear() > 9999) {
            throw new IllegalArgumentException("Timestamp year must be between 0001 and 9999.");
        }
        return value.format(TIMESTAMP_FORMATTER);
    }

    /** Accept exactly yyyy-MM-dd HH:mm:ss, with a real calendar date and time. */
    public static LocalDateTime parseTimestamp(String value) {
        if (value == null || value.length() != 19) {
            throw new DateTimeParseException("Expected yyyy-MM-dd HH:mm:ss", value == null ? "" : value, 0);
        }
        LocalDateTime parsed = LocalDateTime.parse(value, TIMESTAMP_FORMATTER);
        if (parsed.getYear() < 1) {
            throw new DateTimeParseException("Timestamp year must be between 0001 and 9999", value, 0);
        }
        return parsed;
    }
}
