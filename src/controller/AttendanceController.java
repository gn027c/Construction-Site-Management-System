package controller;

import java.util.Comparator;
import java.util.Scanner;
import model.AttendanceRecord;
import model.Permission;
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
            int choice = InputHelper.getInt(scanner, "Enter option (0-6): ", 0, 6);
            view.ConsoleScreen.begin();
            if (choice == 0) {
                return;
            }
            switch (choice) {
                case 1:
                case 3:
                    boolean personal = choice == 3;
                    authentication.require(personal ? Permission.RECORD_OWN_ATTENDANCE : Permission.RECORD_ATTENDANCE);
                    String person = personal ? authentication.currentPersonCode()
                            : InputHelper.getString(scanner, "Person code: ");
                    String zone = InputHelper.getString(scanner, "Zone ID: ");
                    int action = InputHelper.getInt(scanner, "1-Check in, 2-Check out, 0-Cancel: ", 0, 2);
                    if (action == 0) {
                        authentication.audit("ATTENDANCE", "PERSON", person, "CANCELLED", "Cancelled by user.");
                        break;
                    }
                    site.recordAttendance(person, zone, action == 1
                            ? AttendanceRecord.CheckType.CHECK_IN : AttendanceRecord.CheckType.CHECK_OUT, personal);
                    ConsoleTheme.success("Attendance recorded.");
                    break;
                case 2:
                case 4:
                    ListView.show(scanner, "ATTENDANCE", site.attendance(choice == 4),
                            AttendanceRecord::toString, AttendanceRecord::toString,
                            Comparator.comparing(AttendanceRecord::getTimestamp).reversed()
                                    .thenComparing(
                                            AttendanceRecord::getRecordId,
                                            util.IdentifierHelper.NATURAL_ORDER.reversed()
                                    ),
                            event -> site.getAuthentication().audit(event, "LIST", "", "SUCCESS", "Navigated list."));
                    break;
                case 5:
                case 6:
                    ListView.show(scanner, "CURRENT OCCUPANCY", site.occupancy(choice == 6),
                            s -> s, s -> s, null,
                            event -> site.getAuthentication().audit(event, "LIST", "", "SUCCESS", "Navigated list."));
                    break;
            }
        }
    }
}
