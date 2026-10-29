package view;

/**
 * Prints console navigation using the shared theme.
 *
 * <p>This view contains menu labels only; input handling and application
 * operations belong to controllers and services.
 *
 * <p>Responsibility: Huynh Nguyen Hoang Khang (SE201461) - console navigation.
 */
public final class MenuView {
    private MenuView() {
    }

    public static void showMainMenu() {
        ConsoleTheme.menu(
                "HOME",
                "Personnel",
                "Zones and permissions",
                "Attendance",
                "Safety incidents",
                "CSV data and export"
        );
    }

    public static void showPersonMenu() {
        ConsoleTheme.menu("PERSONNEL", "Add person", "View personnel", "Search and update");
    }

    public static void showZoneMenu() {
        ConsoleTheme.menu("ZONES", "Add zone", "View zones", "Grant access", "Revoke access");
    }

    public static void showAttendanceMenu() {
        ConsoleTheme.menu("ATTENDANCE", "Check in or out", "View history");
    }

    public static void showIncidentMenu() {
        ConsoleTheme.menu(
                "INCIDENTS",
                "Report incident",
                "View incidents",
                "Assign handler",
                "Resolve incident",
                "Purge resolved incidents"
        );
    }

    public static void showDataMenu() {
        ConsoleTheme.menu("CSV DATA AND EXPORT", "Reload CSV files", "Save CSV files", "Export CSV files");
    }
}
