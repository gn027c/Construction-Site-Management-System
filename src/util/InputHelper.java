package util;

import java.util.Scanner;

/**
 * TASK: Member 1 (Huynh Nguyen Hoang Khang - SE201461)
 * DESCRIPTION:
 * - Utility class to handle safe input operations from Console.
 * - Prevents Scanner buffer issues and handles data type exceptions.
 */
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
}
