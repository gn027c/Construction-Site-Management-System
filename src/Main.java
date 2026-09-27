import view.MenuView;
import util.InputHelper;
import controller.SiteManagerController;
import model.AttendanceRecord;
import model.Incident;
import java.util.List;
import java.util.Scanner;

/**
 * TASK (shared file - each member edits only their own handler):
 * - Member 1 (Huynh Nguyen Hoang Khang - SE201461): main() loop, controller wiring, final integration.
 * - Member 2 (Tran Ngoc Anh Tuan - SE201513): handlePersonMenu().
 * - Member 3 (Le Tan Thien - SE201852): handleZoneMenu().
 * - Member 4 (Nguyen Tan Loi - SE211059): handleAttendanceMenu(), handleIncidentMenu().
 * DESCRIPTION:
 * - Application Console Entry Point.
 * - Contains main() method to manage navigation and the main menu loop.
 */
public class Main {

    static final SiteManagerController controller = new SiteManagerController();

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
                    // Call add person function here
                    break;
                case 2:
                    // Call display person list function here
                    break;
                case 3:
                    // Call search / update person function here
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
                    // Call add zone function here
                    break;
                case 2:
                    // Call display zone list function here
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
                    recordCheckInOut(scanner);
                    break;
                case 2:
                    List<AttendanceRecord> records = controller.getAttendanceManager().getAllAttendanceRecords();
                    if (records.isEmpty()) {
                        System.out.println("No attendance records yet.");
                    }
                    for (AttendanceRecord r : records) {
                        System.out.println(r);
                    }
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }

    private static void recordCheckInOut(Scanner scanner) {
        String personCode = InputHelper.getString(scanner, "Person code: ");
        String zoneId = InputHelper.getString(scanner, "Zone ID: ");
        int typeChoice = InputHelper.getInt(scanner, "1. Check-in  2. Check-out: ", 1, 2);
        AttendanceRecord.CheckType type = (typeChoice == 1)
                ? AttendanceRecord.CheckType.CHECK_IN
                : AttendanceRecord.CheckType.CHECK_OUT;

        controller.recordAttendance(personCode, zoneId, type);
    }

    private static void handleIncidentMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showIncidentMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-5): ", 0, 5);
            switch (choice) {
                case 1:
                    reportIncident(scanner);
                    break;
                case 2:
                    List<Incident> incidents = controller.getIncidentManager().getAllIncidents();
                    if (incidents.isEmpty()) {
                        System.out.println("No incidents recorded.");
                    }
                    for (Incident inc : incidents) {
                        System.out.println(inc);
                    }
                    break;
                case 3: {
                    String incidentId = InputHelper.getString(scanner, "Incident ID: ");
                    String assigneeCode = InputHelper.getString(scanner, "Assignee person code: ");
                    controller.assignIncident(incidentId, assigneeCode);
                    break;
                }
                case 4: {
                    String incidentId = InputHelper.getString(scanner, "Incident ID: ");
                    if (controller.getIncidentManager().resolveIncident(incidentId)) {
                        System.out.println("Incident resolved.");
                    } else {
                        System.out.println("Error: Incident not found or not ASSIGNED yet.");
                    }
                    break;
                }
                case 5:
                    System.out.println("Purged " + controller.getIncidentManager().purgeResolvedIncidents() + " resolved incident(s).");
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }

    private static void reportIncident(Scanner scanner) {
        String incidentId = InputHelper.getString(scanner, "Incident ID: ");
        String title = InputHelper.getString(scanner, "Title: ");
        String description = InputHelper.getString(scanner, "Description: ");
        Incident.IncidentSeverity[] levels = Incident.IncidentSeverity.values();
        int level = InputHelper.getInt(scanner, "Severity (1-LOW, 2-MEDIUM, 3-HIGH, 4-CRITICAL): ", 1, levels.length);
        Incident incident = new Incident(incidentId, title, description,
                levels[level - 1], Incident.IncidentStatus.OPEN, "");
        if (controller.getIncidentManager().reportIncident(incident)) {
            System.out.println("Incident reported with status OPEN.");
        } else {
            System.out.println("Error: Incident ID '" + incidentId + "' already exists!");
        }
    }
}
