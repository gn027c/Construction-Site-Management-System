package util;

import exception.DataValidationException;

public final class PasswordHelper {
    private static final int MAX_LENGTH = 128;

    private PasswordHelper() {
    }

    public static String validate(String password) {
        if (!isValid(password)) {
            throw new DataValidationException("password", "Must contain between 1 and 128 characters.");
        }
        return password;
    }

    public static boolean isValid(String password) {
        return password != null && !password.isEmpty() && password.length() <= MAX_LENGTH;
    }

    public static boolean matches(String entered, String stored) {
        return isValid(entered) && isValid(stored) && entered.equals(stored);
    }
}
