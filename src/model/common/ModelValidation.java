package model.common;

import exception.DataValidationException;
import exception.InvalidPhoneNumberException;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import util.DateTimeHelper;

/**
 * Provides validation rules shared by domain models across packages.
 *
 * <p>Required text is trimmed and rejected when blank. Other guards preserve
 * field-specific rules and report failures through domain validation exceptions.
 * These methods perform no console input, repository lookup or file access.
 */
public final class ModelValidation {
    private static final Pattern PHONE = Pattern.compile("0\\d{9}");

    private ModelValidation() {
    }

    /**
     * Trims required text and rejects null, empty or whitespace-only values.
     */
    public static String requiredText(String value, String field) {
        String normalized = optionalText(value);
        if (normalized.isEmpty()) {
            throw new DataValidationException(field, "Must not be blank.");
        }
        return normalized;
    }

    /**
     * Converts null to an empty string and trims any supplied text.
     */
    public static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Rejects null and returns the original value while preserving its type.
     */
    public static <T> T requiredValue(T value, String field) {
        if (value == null) {
            throw new DataValidationException(field, "Must not be null.");
        }
        return value;
    }

    /**
     * Trims a phone number and applies the ten-digit, leading-zero rule.
     */
    public static String phoneNumber(String value) {
        String normalized = optionalText(value);
        if (!PHONE.matcher(normalized).matches()) {
            throw new InvalidPhoneNumberException(value);
        }
        return normalized;
    }

    /**
     * Validates a personnel code used by records and restricted-zone permissions.
     *
     * <p>Semicolons delimit codes in zone CSV. The token {@code NONE} represents
     * an empty permission list, so neither is allowed as a personnel code.
     */
    public static String personCode(String value, String field) {
        return util.IdentifierHelper.normalize(value, field);
    }

    public static String identifier(String value, String field) {
        return util.IdentifierHelper.normalize(value, field);
    }

    /**
     * Validates {@code yyyy-MM-dd HH:mm:ss} without rewriting the value.
     */
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

    /**
     * Accepts an empty string for an unknown date or validates {@code dd/MM/yyyy}.
     * Null and invalid calendar dates are rejected.
     */
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
