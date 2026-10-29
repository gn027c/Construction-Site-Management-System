package model;

import exception.DataValidationException;
import exception.InvalidPhoneNumberException;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import util.DateTimeHelper;

/**
 * Provides validation rules shared by domain models within this package.
 *
 * <p>Required text is trimmed and rejected when blank. Other guards preserve
 * field-specific rules and report failures through domain validation exceptions.
 * These methods perform no console input, repository lookup or file access.
 */
final class ModelValidation {
    private static final Pattern PHONE = Pattern.compile("0\\d{9}");

    private ModelValidation() {
    }

    /**
     * Trims required text and rejects null, empty or whitespace-only values.
     *
     * @param value the supplied text
     * @param field the field name used in validation errors
     * @return the trimmed, nonempty text
     * @throws DataValidationException if the normalized text is empty
     */
    static String requiredText(String value, String field) {
        String normalized = optionalText(value);
        if (normalized.isEmpty()) {
            throw new DataValidationException(field, "Must not be blank.");
        }
        return normalized;
    }

    /**
     * Normalizes optional text without treating absence as a validation failure.
     *
     * @param value the supplied text, possibly null
     * @return an empty string for null; otherwise the trimmed text
     */
    static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Rejects a missing value while preserving the caller's static type.
     *
     * <p>The declaration {@code <T>} introduces the type used by both the
     * parameter and return value. No cast is needed at the call site.
     *
     * @param <T> the type of the supplied value
     * @param value the value to check
     * @param field the field name used in validation errors
     * @return the original non-null value
     * @throws DataValidationException if the value is null
     */
    static <T> T requiredValue(T value, String field) {
        if (value == null) {
            throw new DataValidationException(field, "Must not be null.");
        }
        return value;
    }

    /**
     * Trims a phone number and applies the ten-digit, leading-zero rule.
     *
     * @param value the supplied phone number
     * @return the validated and trimmed number
     * @throws InvalidPhoneNumberException if the number does not match the rule
     */
    static String phoneNumber(String value) {
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
     *
     * @param value the supplied code
     * @param field the field name used in validation errors
     * @return the trimmed code
     * @throws DataValidationException if the code is blank or conflicts with CSV encoding
     */
    static String personCode(String value, String field) {
        String normalized = requiredText(value, field);
        if (normalized.contains(";") || normalized.equalsIgnoreCase("NONE")) {
            throw new DataValidationException(field, "Must not contain ';' or use the reserved code NONE.");
        }
        return normalized;
    }

    /**
     * Validates the exact shared timestamp format without rewriting the value.
     *
     * @param value the timestamp in {@code yyyy-MM-dd HH:mm:ss} format
     * @return the original valid timestamp
     * @throws DataValidationException if the timestamp is missing or invalid
     */
    static String timestamp(String value) {
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
     * Accepts an unknown date as an empty string or validates an exact date.
     *
     * @param value an empty string or a date in {@code dd/MM/yyyy} format
     * @param field the field name used in validation errors
     * @return an empty string or the formatted valid date
     * @throws DataValidationException if the value is null or the date is invalid
     */
    static String optionalDate(String value, String field) {
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
