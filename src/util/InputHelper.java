package util;

import exception.DataValidationException;
import java.util.Scanner;

/**
 * Reads and validates console input one complete line at a time.
 *
 * <p>Required text readers trim accepted input and retry after validation failures.
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
            view.ConsoleScreen.inputPrompted();
            char[] password = console.readPassword("%s", cancelPrompt(prompt));
            if (password == null) {
                throw new java.util.NoSuchElementException("Input closed.");
            }
            try {
                return interpret(new String(password));
            } finally {
                java.util.Arrays.fill(password, '\0');
            }
        }
        return readLine(scanner, prompt);
    }

    /**
     * Reads optional text without changing its whitespace or empty-value semantics.
     * The reserved command /cancel aborts entry; //cancel enters a literal /cancel.
     */
    public static String readLine(Scanner scanner, String prompt) {
        view.ConsoleTheme.prompt(cancelPrompt(prompt));
        return interpret(scanner.nextLine());
    }

    private static String cancelPrompt(String prompt) {
        return prompt + "[/cancel to cancel] ";
    }

    private static String interpret(String input) {
        if (input.trim().equalsIgnoreCase("/cancel")) {
            throw new exception.InputCancelledException();
        }
        if (input.trim().equalsIgnoreCase("//cancel")) {
            int slash = input.indexOf('/');
            return input.substring(0, slash) + input.substring(slash + 1);
        }
        return input;
    }

    /**
     * Reads navigation choices, treating Enter, zero and /cancel as Back or Exit.
     * Data fields with meaningful zero values continue to use getInt.
     */
    public static int getMenuChoice(Scanner scanner, String prompt, int max) {
        while (true) {
            view.ConsoleTheme.prompt(prompt + "[Enter = 0] ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty() || input.equalsIgnoreCase("/cancel")) {
                return 0;
            }
            try {
                validateInt(input, "Menu choice", 0, max);
                return Integer.parseInt(input);
            } catch (DataValidationException e) {
                view.ConsoleTheme.warning(e.getMessage());
            }
        }
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
            String input = readLine(scanner, prompt);
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
            String input = readLine(scanner, prompt + "(Enter to cancel) ");
            if (input.trim().isEmpty()) {
                throw new exception.InputCancelledException();
            }
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
            String input = readLine(scanner, prompt);
            try {
                validateDate(input, "Date");
                return input.trim();
            } catch (DataValidationException e) {
                view.ConsoleTheme.warning(e.getMessage());
            }
        }
    }
}
