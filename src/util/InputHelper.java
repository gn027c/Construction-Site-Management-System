package util;

import exception.DataValidationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

/**
 * TASK: Member 1 (Huynh Nguyen Hoang Khang - SE201461)
 * DESCRIPTION:
 * - Utility class to handle safe input operations from Console.
 * - Prevents Scanner buffer issues and handles data type exceptions.
 */
public class InputHelper {

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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            sdf.parse(input.trim());
        } catch (ParseException e) {
            throw new DataValidationException(fieldName, "Invalid date format or non-existent date! Must be dd/MM/yyyy.");
        }
    }

   
    public static String getString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                validateString(input, "Input");
                return input.trim();
            } catch (DataValidationException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    
    public static int getInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                validateInt(input, "Integer Value", min, max);
                return Integer.parseInt(input.trim());
            } catch (DataValidationException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    
    public static String getDate(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                validateDate(input, "Date");
                return input.trim();
            } catch (DataValidationException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
