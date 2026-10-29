package view;

import java.util.LinkedHashMap;
import java.util.Map;
import model.Permission;
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
        Map<Integer, String> options = new LinkedHashMap<>();
        add(options, 1, "Personnel", authentication, Permission.MANAGE_PERSONNEL);
        add(options, 2, "Zones and permissions", authentication, Permission.MANAGE_ZONES);
        add(options, 3, "Attendance", authentication, Permission.RECORD_ATTENDANCE, Permission.VIEW_ATTENDANCE,
                Permission.RECORD_OWN_ATTENDANCE, Permission.VIEW_OWN_ATTENDANCE);
        add(
                options, 4, "Safety incidents", authentication,
                Permission.REPORT_INCIDENTS, Permission.VIEW_INCIDENTS, Permission.ASSIGN_INCIDENTS,
                Permission.RESOLVE_INCIDENTS, Permission.PURGE_INCIDENTS
        );
        add(options, 5, "CSV data and export", authentication, Permission.RELOAD_DATA, Permission.EXPORT_REPORTS);
        add(options, 6, "Account administration", authentication, Permission.MANAGE_ACCOUNTS);
        options.put(7, "Change my password");
        options.put(8, "Sign out");
        add(options, 9, "Activity log", authentication, Permission.VIEW_ACTIVITY_LOG);
        options.put(10, "My notifications");
        add(options, 11, "Data review", authentication, Permission.REVIEW_ATTENDANCE);
        add(options, 12, "Period reports", authentication, Permission.EXPORT_REPORTS);
        ConsoleTheme.menuOptions(
                "HOME / " + authentication.currentAccount().getUsername() + " / "
                        + authentication.currentAccount().getRole(),
                options, "Save and exit"
        );
    }

    public static void showPersonMenu() {
        ConsoleTheme.menu("PERSONNEL", "Add person", "View personnel", "Search and update",
                "Activate or deactivate", "Delete unreferenced person");
    }

    public static void showZoneMenu() {
        ConsoleTheme.menu("ZONES", "Add zone", "View zones", "Grant access", "Revoke access");
    }

    public static void showAttendanceMenu(AuthenticationService authentication) {
        Map<Integer, String> options = new LinkedHashMap<>();
        add(options, 1, "Check in or out", authentication, Permission.RECORD_ATTENDANCE);
        add(options, 2, "View history", authentication, Permission.VIEW_ATTENDANCE);
        add(options, 3, "My check in or out", authentication, Permission.RECORD_OWN_ATTENDANCE);
        add(options, 4, "My attendance history", authentication, Permission.VIEW_OWN_ATTENDANCE);
        add(options, 5, "People currently in each zone", authentication, Permission.VIEW_OCCUPANCY);
        add(options, 6, "My current zone", authentication, Permission.VIEW_OWN_ATTENDANCE);
        ConsoleTheme.menuOptions("ATTENDANCE", options, "Back");
    }

    public static void showIncidentMenu(AuthenticationService authentication) {
        Map<Integer, String> options = new LinkedHashMap<>();
        add(options, 1, "Report incident", authentication, Permission.REPORT_INCIDENTS);
        add(options, 2, "View incidents", authentication, Permission.VIEW_INCIDENTS);
        add(options, 3, "Assign handler", authentication, Permission.ASSIGN_INCIDENTS);
        add(options, 4, "Resolve incident", authentication, Permission.RESOLVE_INCIDENTS);
        add(options, 5, "Archive resolved incidents", authentication, Permission.PURGE_INCIDENTS);
        add(options, 6, "Include archived incidents", authentication, Permission.VIEW_INCIDENTS);
        ConsoleTheme.menuOptions("INCIDENTS", options, "Back");
    }

    public static void showDataMenu(AuthenticationService authentication) {
        Map<Integer, String> options = new LinkedHashMap<>();
        add(options, 1, "Reload CSV files", authentication, Permission.RELOAD_DATA);
        add(options, 2, "Save CSV files", authentication, Permission.RELOAD_DATA);
        add(options, 3, "Export CSV files", authentication, Permission.EXPORT_REPORTS);
        ConsoleTheme.menuOptions("CSV DATA AND EXPORT", options, "Back");
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
