package controller;

import java.util.Comparator;
import java.util.Scanner;
import model.attendance.AttendanceRecord;
import model.auth.Permission;
import service.AuthenticationService;
import util.InputHelper;
import view.ConsoleTheme;
import view.ListView;
import view.MenuView;

/**
 * Presents personal and authorized staff attendance operations through the secured facade.
 */
public class AttendanceController {
    private final SiteManagerController site;
    private final AuthenticationService authentication;

    public AttendanceController(SiteManagerController site) {
        this.site = site;
        authentication = site.getAuthentication();
    }

    public void handleMenu(Scanner scanner) {
        while (true) {
            MenuView.showAttendanceMenu(authentication);
            int choice = InputHelper.getMenuChoice(scanner, "Enter option (0-6): ", 6);
            view.ConsoleScreen.begin();
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1:
                case 3:
                    boolean personal = choice == 3;
                    authentication.require(personal ? Permission.RECORD_OWN_ATTENDANCE : Permission.RECORD_ATTENDANCE);
                    view.ConsoleTheme.breadcrumb("ATTENDANCE / " + (personal ? "MY CHECK IN / OUT" : "RECORD ATTENDANCE"));
                    String person = personal ? authentication.currentPersonCode()
                            : InputHelper.getString(scanner, "Person code: ");
                    if (!personal) {
                        try {
                            site.person(person);
                        } catch (exception.PersonNotFoundException e) {
                            ConsoleTheme.error(e.getMessage());
                            break;
                        }
                    }
                    String zone = InputHelper.getString(scanner, "Zone ID: ");
                    try {
                        site.zones().stream()
                                .filter(z -> z.getZoneId().equalsIgnoreCase(zone))
                                .findFirst()
                                .orElseThrow(() -> new exception.ZoneNotFoundException("Zone '" + zone + "' not found."));
                    } catch (exception.ZoneNotFoundException e) {
                        ConsoleTheme.error(e.getMessage());
                        break;
                    }
                    int action = InputHelper.getMenuChoice(scanner, "1-Check in, 2-Check out, 0-Cancel: ", 2);
                    if (action == 0) {
                        break;
                    }
                    site.recordAttendance(person, zone, action == 1
                            ? AttendanceRecord.CheckType.CHECK_IN : AttendanceRecord.CheckType.CHECK_OUT, personal);
                    ConsoleTheme.success("Attendance recorded.");
                    break;
                case 2:
                case 4:
                    ListView.showTable(
                            scanner,
                            "ATTENDANCE",
                            site.attendance(choice == 4),
                            view.AttendanceView.TABLE_HEADERS,
                            view.AttendanceView::tableRow,
                            view.AttendanceView::formatSummary,
                            Comparator.comparing(AttendanceRecord::getTimestamp).reversed()
                                    .thenComparing(
                                            AttendanceRecord::getRecordId,
                                            util.IdentifierHelper.NATURAL_ORDER.reversed()
                                    )
                    );
                    break;
                case 5:
                case 6:
                    java.util.List<String> rawOccupancy = site.occupancy(choice == 6);
                    java.util.List<java.util.List<String>> occRows = new java.util.ArrayList<>();
                    for (String line : rawOccupancy) {
                        if (line.startsWith("TOTAL ")) {
                            String[] parts = line.split(" = ");
                            String zoneName = parts[0].substring(6).trim();
                            String count = parts.length > 1 ? parts[1].trim() : "";
                            occRows.add(java.util.Arrays.asList(zoneName, "-", "[TOTAL OCCUPANTS: " + count + "]", "-", "-", "-"));
                        } else {
                            String[] parts = line.split(" \\| ");
                            String zoneName = parts.length > 0 ? parts[0].trim() : "";
                            String pCode = parts.length > 1 ? parts[1].trim() : "";
                            String pName = parts.length > 2 ? parts[2].trim() : "";
                            String pRole = parts.length > 3 ? parts[3].trim() : "";
                            String pTime = parts.length > 4 ? parts[4].trim() : "";
                            String pStatus = parts.length > 5 ? parts[5].trim() : "Active";
                            occRows.add(java.util.Arrays.asList(zoneName, pCode, pName, pRole, pTime, pStatus));
                        }
                    }
                    ListView.showTable(
                            scanner,
                            "CURRENT OCCUPANCY",
                            occRows,
                            java.util.Arrays.asList("Zone", "Person", "Name", "Role", "Check-in Time", "Status"),
                            row -> row,
                            row -> String.join(" ", row),
                            null
                    );
                    break;
            }
        }
    }
}
