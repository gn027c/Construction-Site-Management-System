package view;

/** Console navigation. Maintainer: Khang SE201461. */
public final class MenuView {
    private MenuView() { }
    public static void showMainMenu() { ConsoleTheme.menu("HOME", "Personnel", "Zones and permissions", "Attendance", "Safety incidents", "State and export"); }
    public static void showPersonMenu() { ConsoleTheme.menu("PERSONNEL", "Add person", "View personnel", "Search and update"); }
    public static void showZoneMenu() { ConsoleTheme.menu("ZONES", "Add zone", "View zones", "Grant access", "Revoke access"); }
    public static void showAttendanceMenu() { ConsoleTheme.menu("ATTENDANCE", "Check in or out", "View history"); }
    public static void showIncidentMenu() { ConsoleTheme.menu("INCIDENTS", "Report incident", "View incidents", "Assign handler", "Resolve incident", "Purge resolved incidents"); }
    public static void showDataMenu() { ConsoleTheme.menu("STATE AND EXPORT", "Reload committed state", "Save checkpoint", "Export CSV files"); }
}
