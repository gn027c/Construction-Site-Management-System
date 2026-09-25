import view.MenuView;
import util.InputHelper;
import java.util.Scanner;

/**
 * TASK: Member 1 (Huynh Nguyen Hoang Khang - SE201461)
 * DESCRIPTION:
 * - Application Console Entry Point.
 * - Contains main() method to manage navigation and the main menu loop.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            MenuView.showMainMenu();
            int choice = InputHelper.getInt(scanner, "Enter your choice (0-4): ", 0, 4);

            switch (choice) {
                case 1:
                    handlePersonMenu(scanner);
                    break;
                case 2:
                    handleZoneMenu(scanner);
                    break;
                case 3:
                    handleAttendanceMenu(scanner);
                    break;
                case 4:
                    handleIncidentMenu(scanner);
                    break;
                case 0:
                    exit = true;
                    System.out.println("Thank you for using SmartSite System! Goodbye.");
                    break;
                default:
                    System.out.println("Invalid selection!");
            }
        }
        scanner.close();
    }

    private static void handlePersonMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showPersonMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-3): ", 0, 3);
            switch (choice) {
                case 1:
                    System.out.println("-> [Tuan] Adding new person...");
                    break;
                case 2:
                    System.out.println("-> Displaying person list...");
                    break;
                case 3:
                    System.out.println("-> Searching / Updating person...");
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }

    private static void handleZoneMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showZoneMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    System.out.println("-> Adding new zone...");
                    break;
                case 2:
                    System.out.println("-> Displaying zone list...");
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }

    private static void handleAttendanceMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showAttendanceMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    System.out.println("-> [Loi] Recording Check-In/Out...");
                    break;
                case 2:
                    System.out.println("-> Viewing attendance history...");
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }

    private static void handleIncidentMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showIncidentMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    System.out.println("-> Reporting new incident...");
                    break;
                case 2:
                    System.out.println("-> Displaying incident list...");
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }
}
