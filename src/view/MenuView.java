package view;

import java.util.ArrayList;
import java.util.List;
import model.Account;
import model.Permission;

public final class MenuView {
    private MenuView() {
    }

    public static void showMainMenu() {
        showMainMenu(null);
    }

    public static void showMainMenu(Account account) {
        List<String> items = new ArrayList<>();
        if (account == null || account.allows(Permission.MANAGE_PERSONNEL)) {
            items.add("[1] Person Management");
        }
        if (account == null || account.allows(Permission.MANAGE_ZONES)
                || account.allows(Permission.VIEW_OCCUPANCY)) {
            items.add("[2] Zone Management");
        }
        if (account == null || account.allows(Permission.RECORD_ATTENDANCE)
                || account.allows(Permission.RECORD_OWN_ATTENDANCE)
                || account.allows(Permission.VIEW_ATTENDANCE)
                || account.allows(Permission.VIEW_OWN_ATTENDANCE)) {
            items.add("[3] Attendance Management");
        }
        if (account == null || account.allows(Permission.REPORT_INCIDENTS)
                || account.allows(Permission.VIEW_INCIDENTS)
                || account.allows(Permission.ASSIGN_INCIDENTS)
                || account.allows(Permission.RESOLVE_INCIDENTS)
                || account.allows(Permission.PURGE_INCIDENTS)) {
            items.add("[4] Incident Management");
        }
        if (account == null || account.allows(Permission.RELOAD_DATA)) {
            items.add("[5] Data Management / Persistence");
        }
        if (account == null || account.allows(Permission.MANAGE_ACCOUNTS)) {
            items.add("[6] Account Management");
        }
        items.add("[7] Change My Password");
        items.add("[8] Sign Out");
        if (account == null || account.allows(Permission.EXPORT_REPORTS)) {
            items.add("[9] Reports and Export");
        }
        items.add("[0] Exit Program");
        String context = account == null ? "No active session" :
                "Signed in: " + account.getUsername() + " | Role: " + account.getRole();
        ConsoleUi.menuWithContext(
                "SMARTSITE CONSTRUCTION MANAGEMENT SYSTEM",
                context,
                items.toArray(new String[0]));
    }

    public static void showPersonMenu() {
        ConsoleUi.menu(
                "PERSON MANAGEMENT",
                "1. Add New Person",
                "2. Display Person List",
                "3. Search / Update Person",
                "4. Activate / Deactivate Person",
                "5. Set Safety Level",
                "6. Change Visitor Date",
                "7. Change Position",
                "8. Delete Person",
                "9. Update Profile",
                "0. Back to Main Menu");
    }

    public static void showZoneMenu() {
        showZoneMenu(null);
    }

    public static void showZoneMenu(Account account) {
        boolean manage = account == null || account.allows(Permission.MANAGE_ZONES);
        boolean view = account == null || account.allows(Permission.VIEW_OCCUPANCY);
        ConsoleUi.menu("ZONE MANAGEMENT",
                accessItem("1. Add New Zone", manage),
                accessItem("2. Display Zone List", manage || view),
                accessItem("3. Grant Restricted Access", manage),
                accessItem("4. Revoke Restricted Access", manage),
                accessItem("5. Update Zone", manage),
                accessItem("6. Delete Zone", manage),
                "0. Back to Main Menu");
    }

    public static void showAttendanceMenu() {
        showAttendanceMenu(null);
    }

    public static void showAttendanceMenu(Account account) {
        boolean record = account == null || account.allows(Permission.RECORD_ATTENDANCE)
                || account.allows(Permission.RECORD_OWN_ATTENDANCE);
        boolean view = account == null || account.allows(Permission.VIEW_ATTENDANCE)
                || account.allows(Permission.VIEW_OWN_ATTENDANCE);
        ConsoleUi.menu("ATTENDANCE MANAGEMENT",
                accessItem("1. Record Check-In / Check-Out", record),
                accessItem("2. View Attendance History", view),
                "0. Back to Main Menu");
    }

    public static void showIncidentMenu() {
        showIncidentMenu(null);
    }

    public static void showIncidentMenu(Account account) {
        ConsoleUi.menu("INCIDENT MANAGEMENT",
                accessItem("1. Report New Incident", account == null || account.allows(Permission.REPORT_INCIDENTS)),
                accessItem("2. Display Incident List", account == null || account.allows(Permission.VIEW_INCIDENTS)),
                accessItem("3. Assign Incident Handler", account == null || account.allows(Permission.ASSIGN_INCIDENTS)),
                accessItem("4. Resolve Incident", account == null || account.allows(Permission.RESOLVE_INCIDENTS)),
                accessItem("5. Purge Resolved Incidents", account == null || account.allows(Permission.PURGE_INCIDENTS)),
                "0. Back to Main Menu");
    }

    public static void showAccountMenu() {
        ConsoleUi.menu(
                "ACCOUNT MANAGEMENT",
                "1. List Accounts",
                "2. Create Account",
                "3. Change Role",
                "4. Enable / Disable Account",
                "5. Reset Password",
                "6. Link Person",
                "7. Grant Permission",
                "8. Revoke Permission",
                "0. Back to Main Menu");
    }

    public static void showDataMenu() {
        ConsoleUi.menu(
                "DATA MANAGEMENT / PERSISTENCE",
                "1. Reload Data From CSV Files",
                "2. Save Data Manually To CSV Files",
                "0. Back to Main Menu");
    }

    public static void showReportMenu() {
        ConsoleUi.menu(
                "REPORTS & EXPORT",
                "1. Current Week Report",
                "2. Current Month Report",
                "3. Custom Period Report",
                "4. Export Report CSV Files",
                "0. Back to Main Menu");
    }

    private static String accessItem(String label, boolean allowed) {
        return allowed ? label : label + "  [locked]";
    }
}
