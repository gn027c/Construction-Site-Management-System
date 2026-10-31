package util;

import exception.DataValidationException;

/**
 * Validates and compares account passwords.
 *
 * <p>Passwords are stored as plain text in {@code accounts.csv}, which is excluded
 * from Git and from business exports. Anyone who can read the data directory can
 * read the passwords, so the directory must stay private to administrators.
 */
public final class PasswordHelper {
    private static final int MAX_LENGTH = 128;

    private PasswordHelper() {
    }

    /**
     * Returns the password unchanged after checking it has 1 to 128 characters.
     * Whitespace is preserved.
     */
    public static String validate(String password) {
        if (!isValid(password)) {
            throw new DataValidationException("password", "Must contain between 1 and 128 characters.");
        }
        return password;
    }

    /**
     * Reports whether a password has 1 to 128 characters.
     */
    public static boolean isValid(String password) {
        return password != null && !password.isEmpty() && password.length() <= MAX_LENGTH;
    }

    /**
     * Reports whether an entered password equals the stored one.
     */
    public static boolean matches(String entered, String stored) {
        return isValid(entered) && isValid(stored) && entered.equals(stored);
    }
}
