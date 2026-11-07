package util;

import exception.DataValidationException;

public final class ValidationHelper {

    private ValidationHelper() {
    }

    public static String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new DataValidationException(message);
        }
        return value.trim();
    }

    public static String requireText(String value, String fieldName, String reason) {
        if (value == null || value.trim().isEmpty()) {
            throw new DataValidationException(fieldName, reason);
        }
        return value.trim();
    }

    public static <T> T requireNotNull(T value, String message) {
        if (value == null) {
            throw new DataValidationException(message);
        }
        return value;
    }

    public static <T> T requireNotNull(T value, String fieldName, String reason) {
        if (value == null) {
            throw new DataValidationException(fieldName, reason);
        }
        return value;
    }

    public static String requireCode(String code, String fieldName, String reason) {
        if (code == null || !code.trim().matches("[A-Za-z0-9_-]+")) {
            throw new DataValidationException(fieldName, reason);
        }
        return code.trim();
    }

    public static String requirePhone(String phone) {
        if (phone == null || !phone.trim().matches("0\\d{9}")) {
            throw new DataValidationException(
                    "Invalid phone number format: " + phone + ". Expected 10 digits starting with 0.");
        }
        return phone.trim();
    }

    public static String requireUsername(String username) {
        String normalized = username == null ? "" : username.trim();
        if (!normalized.matches("[a-zA-Z0-9_.-]{3,32}")) {
            throw new DataValidationException("username", "Use 3 to 32 letters, digits, dots, underscores or hyphens.");
        }
        return normalized;
    }

    public static String requirePassword(String password) {
        if (password == null || password.isEmpty() || password.length() > 128) {
            throw new DataValidationException("password", "Must contain between 1 and 128 characters.");
        }
        return password;
    }
}

