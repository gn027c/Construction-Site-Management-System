package util;

import exception.DataValidationException;
import java.util.Scanner;

public class InputHelper {
    public static String getPassword(Scanner scanner, String prompt) {
        return readLine(scanner, prompt);
    }

    public static String readLine(Scanner scanner, String prompt) {
        view.ConsoleTheme.prompt(cancelPrompt(prompt));
        return interpret(scanner.nextLine());
    }

    private static String cancelPrompt(String prompt) {
        return prompt;
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

    public static int getMenuChoice(Scanner scanner, String prompt, int max) {
        while (true) {
            view.ConsoleTheme.prompt(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("/cancel")) {
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

    public static boolean confirm(Scanner scanner, String prompt) {
        view.ConsoleTheme.prompt(prompt + " (y/N): ");
        String line = scanner.nextLine().trim();
        return line.equalsIgnoreCase("y") || line.equalsIgnoreCase("yes");
    }

    private static final java.util.regex.Pattern PERSON_NAME_PATTERN =
            java.util.regex.Pattern.compile("^[\\p{L}\\s.'-]{2,50}$");

    public static String capitalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        String[] words = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static String getPersonName(Scanner scanner, String prompt) {
        while (true) {
            String input = readLine(scanner, prompt);
            if (input == null || input.trim().isEmpty()) {
                view.ConsoleTheme.warning("Full name cannot be empty or whitespace.");
                continue;
            }
            String trimmed = input.trim();
            if (trimmed.length() < 2) {
                view.ConsoleTheme.warning("Full name must be at least 2 characters.");
                continue;
            }
            if (trimmed.matches("^\\d+$")) {
                view.ConsoleTheme.warning("Full name cannot be pure numbers. Please enter a valid name.");
                continue;
            }
            if (trimmed.equals(trimmed.toLowerCase()) || trimmed.equals(trimmed.toUpperCase())) {
                return capitalizeName(trimmed);
            }
            return trimmed;
        }
    }

    private static final java.util.regex.Pattern PHONE_PATTERN =
            java.util.regex.Pattern.compile("^0\\d{9}$");

    public static String getPhoneNumber(Scanner scanner, String prompt) {
        while (true) {
            String input = readLine(scanner, prompt);
            if (input == null || input.trim().isEmpty()) {
                view.ConsoleTheme.warning("Phone number cannot be empty.");
                continue;
            }
            String trimmed = input.trim();
            if (!PHONE_PATTERN.matcher(trimmed).matches()) {
                view.ConsoleTheme.warning("Invalid phone number. It must have 10 digits and start with 0.");
                continue;
            }
            return trimmed;
        }
    }
}
