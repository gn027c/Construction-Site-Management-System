package model.common;

import exception.DataValidationException;
import exception.InvalidPhoneNumberException;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import util.DateTimeHelper;

public final class ModelValidation {
    private static final Pattern PHONE = Pattern.compile("0\\d{9}");

    private ModelValidation() {
    }

    public static String requiredText(String value, String field) {
        String normalized = optionalText(value);
        if (normalized.isEmpty()) {
            throw new DataValidationException(field, "Must not be blank.");
        }
        return normalized;
    }

    public static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }

    public static <T> T requiredValue(T value, String field) {
        if (value == null) {
            throw new DataValidationException(field, "Must not be null.");
        }
        return value;
    }

    public static String phoneNumber(String value) {
        String normalized = optionalText(value);
        if (!PHONE.matcher(normalized).matches()) {
            throw new InvalidPhoneNumberException(value);
        }
        return normalized;
    }

    public static String personCode(String value, String field) {
        return util.IdentifierHelper.normalize(value, field);
    }

    public static String identifier(String value, String field) {
        return util.IdentifierHelper.normalize(value, field);
    }

    public static String timestamp(String value) {
        try {
            DateTimeHelper.parseTimestamp(value);
        } catch (DateTimeParseException e) {
            throw new DataValidationException(
                    "timestamp",
                    "Must match " + DateTimeHelper.TIMESTAMP_FORMAT + " and be a valid date and time."
            );
        }
        return value;
    }

    public static String optionalDate(String value, String field) {
        requiredValue(value, field);
        if (value.isEmpty()) {
            return "";
        }
        try {
            return DateTimeHelper.formatDate(DateTimeHelper.parseDate(value));
        } catch (DateTimeParseException e) {
            throw new DataValidationException(
                    field,
                    "Must match " + DateTimeHelper.DATE_FORMAT + " and be a valid date."
            );
        }
    }
}
