package view;

import java.util.LinkedHashMap;
import java.util.Map;
import model.auth.Permission;
import service.AuthenticationService;

/**
 * Prints console navigation using the shared theme.
 *
 * <p>This view contains menu labels only; input handling and application
 * operations belong to controllers and services.
 */
public final class MenuView {
    private MenuView() {
    }

    public static void showMainMenu(AuthenticationService authentication) {
        Map<String, Map<Integer, String>> sections = new LinkedHashMap<>();

        Map<Integer, String> operations = new LinkedHashMap<>();
        add(operations, 1, "Personnel", authentication, Permission.MANAGE_PERSONNEL);
        add(operations, 2, "Zones and permissions", authentication, Permission.MANAGE_ZONES);
        add(operations, 3, "Attendance", authentication, Permission.RECORD_ATTENDANCE, Permission.VIEW_ATTENDANCE,
                Permission.RECORD_OWN_ATTENDANCE, Permission.VIEW_OWN_ATTENDANCE);
        add(operations, 4, "Safety incidents", authentication,
                Permission.REPORT_INCIDENTS, Permission.VIEW_INCIDENTS, Permission.ASSIGN_INCIDENTS,
                Permission.RESOLVE_INCIDENTS, Permission.PURGE_INCIDENTS);
        sections.put("SITE OPERATIONS", operations);

        Map<Integer, String> reports = new LinkedHashMap<>();
        add(reports, 5, "CSV data and export", authentication, Permission.RELOAD_DATA, Permission.EXPORT_REPORTS);
        add(reports, 6, "Attendance report", authentication, Permission.EXPORT_REPORTS);
        add(reports, 7, "Incident report", authentication, Permission.EXPORT_REPORTS);
        sections.put("DATA & REPORTS", reports);

        Map<Integer, String> workspace = new LinkedHashMap<>();
        add(workspace, 8, "Manage accounts", authentication, Permission.MANAGE_ACCOUNTS);
        workspace.put(9, "Change my password");
        workspace.put(10, "Sign out");
        sections.put("SECURITY & WORKSPACE", workspace);

        ConsoleTheme.categorizedMenuOptions(
                "HOME / " + authentication.currentAccount().getUsername() + " / "
                        + authentication.currentAccount().getRole(),
                "Site operations, reporting and workspace administration.",
                sections, "Save and exit"
        );
    }

    public static void showPersonMenu(java.util.List<model.person.Person> people) {
        ConsoleScreen.begin();
        System.out.println();
        ConsoleTheme.breadcrumb("PERSONNEL", "Manage personnel records, roles and site access status.");
        System.out.println(ConsoleTheme.color("90", "  " + String.join("", java.util.Collections.nCopies(70, "─"))));
        System.out.println();
        if (people == null || people.isEmpty()) {
            System.out.println("  No personnel records found.");
        } else {
            java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
            int limit = Math.min(people.size(), 15);
            for (int i = 0; i < limit; i++) {
                rows.add(PersonnelView.tableRow(people.get(i)));
            }
            ListView.printTable(PersonnelView.TABLE_HEADERS, rows);
            if (people.size() > 15) {
                System.out.println(ConsoleTheme.color("90", "  Showing 15 of " + people.size()
                        + " personnel records (select [2] to search and page all)."));
            } else {
                System.out.println(ConsoleTheme.color("90", "  Total: " + people.size() + " personnel record(s)."));
            }
        }
        System.out.println();
        System.out.println("  " + ConsoleTheme.color("36", " [1]") + " Add person");
        System.out.println("  " + ConsoleTheme.color("36", " [2]") + " View and update personnel");
        System.out.println("  " + ConsoleTheme.color("36", " [3]") + " Activate or deactivate");
        System.out.println("  " + ConsoleTheme.color("36", " [4]") + " Delete unreferenced person");
        System.out.println();
        System.out.println("  " + ConsoleTheme.color("33", " [0]") + " Back");
        System.out.println();
    }

    public static void showPersonMenu() {
        showPersonMenu(null);
    }

    public static void showZoneMenu() {
        ConsoleTheme.menuWithDescription(
                "ZONES",
                "Manage site zones, restricted boundaries and entry permissions.",
                "Add zone", "View zones", "Grant access", "Revoke access"
        );
    }

    public static void showAttendanceMenu(AuthenticationService authentication) {
        Map<Integer, String> options = new LinkedHashMap<>();
        add(options, 1, "Check in or out", authentication, Permission.RECORD_ATTENDANCE);
        add(options, 2, "View history", authentication, Permission.VIEW_ATTENDANCE);
        add(options, 3, "My check in or out", authentication, Permission.RECORD_OWN_ATTENDANCE);
        add(options, 4, "My attendance history", authentication, Permission.VIEW_OWN_ATTENDANCE);
        add(options, 5, "People currently in each zone", authentication, Permission.VIEW_OCCUPANCY);
        add(options, 6, "My current zone", authentication, Permission.VIEW_OWN_ATTENDANCE);
        ConsoleTheme.menuOptions("ATTENDANCE", "Track zone check-ins, check-outs and live site occupancy.", options, "Back");
    }

    public static void showIncidentMenu(AuthenticationService authentication) {
        Map<Integer, String> options = new LinkedHashMap<>();
        add(options, 1, "Report incident", authentication, Permission.REPORT_INCIDENTS);
        add(options, 2, "View incidents", authentication, Permission.VIEW_INCIDENTS);
        add(options, 3, "Assign handler", authentication, Permission.ASSIGN_INCIDENTS);
        add(options, 4, "Resolve incident", authentication, Permission.RESOLVE_INCIDENTS);
        add(options, 5, "Archive resolved incidents", authentication, Permission.PURGE_INCIDENTS);
        add(options, 6, "Include archived incidents", authentication, Permission.VIEW_INCIDENTS);
        ConsoleTheme.menuOptions("INCIDENTS", "Log, assign and track safety incidents and resolutions.", options, "Back");
    }

    public static void showDataMenu(AuthenticationService authentication) {
        Map<Integer, String> options = new LinkedHashMap<>();
        add(options, 1, "Reload CSV files", authentication, Permission.RELOAD_DATA);
        add(options, 2, "Save CSV files", authentication, Permission.RELOAD_DATA);
        add(options, 3, "Export CSV files", authentication, Permission.EXPORT_REPORTS);
        ConsoleTheme.menuOptions("CSV DATA AND EXPORT", "Synchronize, persist and export system CSV records.", options, "Back");
    }

    private static void add(
            Map<Integer, String> options, int number, String label,
            AuthenticationService authentication, Permission... permissions
    ) {
        for (Permission permission : permissions) {
            if (authentication.allows(permission)) {
                options.put(number, label);
                return;
            }
        }
    }
}
