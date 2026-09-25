package view;

/**
 * TASK: Member 1 (Huynh Nguyen Hoang Khang - SE201461)
 * DESCRIPTION:
 * - Responsible for rendering Menu interfaces on the Console.
 * - Displays welcome banner and feature options for SmartSite.
 */
public class MenuView {

    // Main Menu Layout
    public static void showMainMenu() {
        System.out.println("\n==================================================");
        System.out.println("   SMARTSITE CONSTRUCTION MANAGEMENT SYSTEM");
        System.out.println("==================================================");
        System.out.println("1. Person Management");
        System.out.println("2. Zone Management");
        System.out.println("3. Attendance / Access Control");
        System.out.println("4. Incident Management");
        System.out.println("0. Exit Program");
        System.out.println("==================================================");
    }

    // Sub-menu: Person Management
    public static void showPersonMenu() {
        System.out.println("\n--- PERSON MANAGEMENT ---");
        System.out.println("1. Add New Person");
        System.out.println("2. Display Person List");
        System.out.println("3. Search / Update Person");
        System.out.println("0. Back to Main Menu");
    }

    // Sub-menu: Zone Management
    public static void showZoneMenu() {
        System.out.println("\n--- ZONE MANAGEMENT ---");
        System.out.println("1. Add New Zone");
        System.out.println("2. Display Zone List");
        System.out.println("0. Back to Main Menu");
    }

    // Sub-menu: Attendance
    public static void showAttendanceMenu() {
        System.out.println("\n--- ATTENDANCE & ACCESS CONTROL ---");
        System.out.println("1. Record Check-In/Out");
        System.out.println("2. View Attendance History");
        System.out.println("0. Back to Main Menu");
    }

    // Sub-menu: Incident
    public static void showIncidentMenu() {
        System.out.println("\n--- INCIDENT MANAGEMENT ---");
        System.out.println("1. Report New Incident");
        System.out.println("2. Display Incident List");
        System.out.println("0. Back to Main Menu");
    }
}
