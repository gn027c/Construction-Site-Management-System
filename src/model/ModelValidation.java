package model;

import exception.DataValidationException;
import exception.InvalidPhoneNumberException;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import util.DateTimeHelper;

/** Package-private guards shared by domain models; never performs console or repository I/O. */
final class ModelValidation {
    private static final Pattern PHONE = Pattern.compile("0\\d{9}");
    private ModelValidation() { }

    static String requiredText(String value, String field) {
        String normalized = optionalText(value);
        if (normalized.isEmpty()) throw new DataValidationException(field, "Must not be blank.");
        return normalized;
    }

    static String optionalText(String value) { return value == null ? "" : value.trim(); }

    static <T> T requiredValue(T value, String field) {
        if (value == null) throw new DataValidationException(field, "Must not be null.");
        return value;
    }

    static String phoneNumber(String value) {
        String normalized = optionalText(value);
        if (!PHONE.matcher(normalized).matches()) throw new InvalidPhoneNumberException(value);
        return normalized;
    }

    static String personCode(String value, String field) {
        String normalized = requiredText(value, field);
        if (normalized.contains(";") || normalized.equalsIgnoreCase("NONE"))
            throw new DataValidationException(field, "Must not contain ';' or use the reserved code NONE.");
        return normalized;
    }

    static String timestamp(String value) {
        try { DateTimeHelper.parseTimestamp(value); }
        catch (DateTimeParseException e) {
            throw new DataValidationException("timestamp", "Must match " + DateTimeHelper.TIMESTAMP_FORMAT + " and be a valid date and time.");
        }
        return value;
    }

    static String optionalDate(String value, String field) {
        requiredValue(value, field);
        if (value.isEmpty()) return "";
        try { return DateTimeHelper.formatDate(DateTimeHelper.parseDate(value)); }
        catch (DateTimeParseException e) {
            throw new DataValidationException(field, "Must match " + DateTimeHelper.DATE_FORMAT + " and be a valid date.");
        }
    }
}
