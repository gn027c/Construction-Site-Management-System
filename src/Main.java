import view.MenuView;
import util.InputHelper;
import service.PersonnelService;
import service.ZoneService;
import service.AttendanceService;
import service.IncidentService;
import java.util.Scanner;

/**
 * TASK (shared file - each member edits only their own handler):
 * - Member 1 (Huynh Nguyen Hoang Khang - SE201461): main() loop, service wiring, final integration.
 * - Member 2 (Tran Ngoc Anh Tuan - SE201513): handlePersonMenu().
 * - Member 3 (Le Tan Thien - SE201852): handleZoneMenu().
 * - Member 4 (Nguyen Tan Loi - SE211059): handleAttendanceMenu(), handleIncidentMenu().
 * DESCRIPTION:
 * - Application Console Entry Point.
 * - Contains main() method to manage navigation and the main menu loop.
 */
public class Main {

    static final PersonnelService personnelService = new PersonnelService();
    static final ZoneService zoneService = new ZoneService();
    static final AttendanceService attendanceService = new AttendanceService(personnelService, zoneService);
    static final IncidentService incidentService = new IncidentService(personnelService);

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
                   // (Tran Ngoc Anh Tuan - SE201513
                    break;
                case 2:
                    //(Tran Ngoc Anh Tuan - SE201513
                    break;
                case 3:
                    //(Tran Ngoc Anh Tuan - SE201513
                    break;
                case 0:
                  // (Tran Ngoc Anh Tuan - SE201513
                    break;
            }
        }
    }
 
 
    private static String readNonEmpty(Scanner scanner, String prompt) {
        String s;
        do {
            System.out.print(prompt);
            s = scanner.nextLine().trim();
            if (s.isEmpty()) System.out.println("Value must not be empty.");
        } while (s.isEmpty());
        return s;
    }

    private static void handleZoneMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showZoneMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    // TODO: Member 3 (Le Tan Thien - SE201852) call add zone
                    break;
                case 2:
                    // TODO: Member 3 (Le Tan Thien - SE201852) call display zone list
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
                    // TODO: Member 4 (Nguyen Tan Loi - SE211059) call check-in / check-out
                    break;
                case 2:
                    // TODO: Member 4 (Nguyen Tan Loi - SE211059) call display attendance records
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
            int choice = InputHelper.getInt(scanner, "Enter option (0-5): ", 0, 5);
            switch (choice) {
                case 1:
                    // TODO: Member 4 (Nguyen Tan Loi - SE211059) call report incident
                    break;
                case 2:
                    // TODO: Member 4 (Nguyen Tan Loi - SE211059) call display incident list
                    break;
                case 3:
                    // TODO: Member 4 (Nguyen Tan Loi - SE211059) call assign incident
                    break;
                case 4:
                    // TODO: Member 4 (Nguyen Tan Loi - SE211059) call resolve incident
                    break;
                case 5:
                    // TODO: Member 4 (Nguyen Tan Loi - SE211059) call purge resolved incidents
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }
}
