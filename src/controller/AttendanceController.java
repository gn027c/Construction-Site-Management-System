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
                            site.attendancePerson(person);
                        } catch (exception.PersonNotFoundException e) {
                            ConsoleTheme.error(e.getMessage());
                            break;
                        }
                    }
                    String zone = InputHelper.getString(scanner, "Zone ID: ");
                    try {
                        site.attendanceZone(zone);
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
                    java.util.List<java.util.List<String>> occRows = new java.util.ArrayList<>();
                    String previousZone = null;
                    int count = 0;
                    for (java.util.List<String> row : site.occupancy(choice == 6)) {
                        if (previousZone != null && !previousZone.equals(row.get(0))) {
                            occRows.add(totalRow(previousZone, count));
                            count = 0;
                        }
                        occRows.add(row);
                        previousZone = row.get(0);
                        count++;
                    }
                    if (previousZone != null) {
                        occRows.add(totalRow(previousZone, count));
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

    private static java.util.List<String> totalRow(String zoneId, int count) {
        return java.util.Arrays.asList(zoneId, "-", "[TOTAL OCCUPANTS: " + count + "]", "-", "-", "-");
    }
}
