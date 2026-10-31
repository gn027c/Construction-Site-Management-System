package util;

import exception.DataValidationException;

/**
 * Validates plaintext passwords according to assignment requirements.
 * Passwords are kept and verified directly as plaintext without hashing.
 */
public final class PasswordHelper {
    private PasswordHelper() {
    }

    /**
     * Validates and returns the plaintext password.
     * Keeps method name for compatibility with authentication services.
     */
    public static String hash(String password) {
        return validate(password);
    }

    /**
     * Validates that the plaintext password is between 1 and 128 characters.
     */
    public static String validate(String password) {
        if (password == null || password.isEmpty() || password.length() > 128) {
            throw new DataValidationException("password", "Must contain between 1 and 128 characters.");
        }
        return password;
    }

    /**
     * Checks if the stored password value is non-empty and valid.
     */
    public static boolean isValidHash(String encoded) {
        return encoded != null && !encoded.isEmpty() && encoded.length() <= 128;
    }

    /**
     * Verifies that the input plaintext matches the stored plaintext password.
     */
    public static boolean verify(String password, String encoded) {
        if (password == null || password.isEmpty() || password.length() > 128 || !isValidHash(encoded)) {
            return false;
        }
        return password.equals(encoded);
    }
}
