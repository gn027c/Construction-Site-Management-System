package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
public class InputHelper {

    public static String getString(Scanner scanner, String prompt) {
        String input = "";
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                break;
            }
            System.out.println("Error: Input cannot be empty! Please try again.");
        }
        return input;
    }

    public static int getInt(Scanner scanner, String prompt, int min, int max) {
        int value = 0;
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    break;
                } else {
                    System.out.printf("Error: Value must be between %d and %d!\n", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format! Please enter an integer.");
            }
        }
        return value;
    }

    /**
     * Prompts the user to input a date in "dd/MM/yyyy" format and validates it.
     */
    public static String getDate(Scanner scanner, String prompt) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);

        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                sdf.parse(input);
                return input;
            } catch (ParseException e) {
                System.out.println("Error: Invalid date format or non-existent date! Please use dd/MM/yyyy (e.g., 25/12/2024).");
            }
        }
    }
}
