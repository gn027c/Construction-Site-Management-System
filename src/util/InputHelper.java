package util;

import exception.DataValidationException;
import java.util.Scanner;

/**
 * Reads and validates console input one complete line at a time.
 *
 * <p>Interactive readers trim accepted input and retry after validation failures.
 * End of input propagates to the application loop so it can attempt to save and exit.
 */
public class InputHelper {

    /**
     * Reads a password without echo when a native console is available.
     * Redirected input and embedded terminals use the shared line reader.
     */
    public static String getPassword(Scanner scanner, String prompt) {
        java.io.Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword("%s", prompt);
            if (password == null) {
                throw new java.util.NoSuchElementException("Input closed.");
            }
            try {
                return new String(password);
            } finally {
                java.util.Arrays.fill(password, '\0');
            }
        }
        view.ConsoleTheme.prompt(prompt);
        return scanner.nextLine();
    }
    public static void validateString(String input, String fieldName) throws DataValidationException {
        if (input == null || input.trim().isEmpty()) {
            throw new DataValidationException(fieldName, "Cannot be empty or whitespace.");
        }
    }

    public static void validateInt(String input, String fieldName, int min, int max) throws DataValidationException {
        validateString(input, fieldName);
        try {
            int value = Integer.parseInt(input.trim());
            if (value < min || value > max) {
                throw new DataValidationException(fieldName, "Value must be between " + min + " and " + max + ".");
            }
        } catch (NumberFormatException e) {
            throw new DataValidationException(fieldName, "Invalid number format! Must be an integer.");
        }
    }

    public static void validateDate(String input, String fieldName) throws DataValidationException {
        validateString(input, fieldName);
        try {
            DateTimeHelper.parseDate(input.trim());
        } catch (java.time.format.DateTimeParseException e) {
            throw new DataValidationException(fieldName, "Expected a valid " + DateTimeHelper.DATE_FORMAT + " date.");
        }
    }

    public static String getString(Scanner scanner, String prompt) {
        while (true) {
            view.ConsoleTheme.prompt(prompt);
            String input = scanner.nextLine();
            try {
                validateString(input, "Input");
                return input.trim();
            } catch (DataValidationException e) {
                view.ConsoleTheme.warning(e.getMessage());
            }
        }
    }

    public static int getInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            view.ConsoleTheme.prompt(prompt);
            String input = scanner.nextLine();
            try {
                validateInt(input, "Integer Value", min, max);
                return Integer.parseInt(input.trim());
            } catch (DataValidationException e) {
                view.ConsoleTheme.warning(e.getMessage());
            }
        }
    }

    public static String getDate(Scanner scanner, String prompt) {
        while (true) {
            view.ConsoleTheme.prompt(prompt);
            String input = scanner.nextLine();
            try {
                validateDate(input, "Date");
                return input.trim();
            } catch (DataValidationException e) {
                view.ConsoleTheme.warning(e.getMessage());
            }
        }
    }
}
