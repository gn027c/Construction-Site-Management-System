package controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import model.ActivityEvent;
import model.Notification;
import model.Permission;
import service.AuthenticationService;
import service.ReportService;
import util.DateTimeHelper;
import util.InputHelper;
import view.ConsoleTheme;
import view.ListView;

/**
 * Presents administrator diagnostics, private notifications and filtered period reports.
 */
public final class WorkspaceController {
    private final SiteManagerController site;
    private final AuthenticationService authentication;

    public WorkspaceController(SiteManagerController site) {
        this.site = site;
        authentication = site.getAuthentication();
    }

    public void logs(Scanner scanner) {
        authentication.require(Permission.VIEW_ACTIVITY_LOG);
        List<ActivityEvent> events = authentication.getActivityLog().list(authentication);
        ListView.show(scanner, "ACTIVITY LOG", events, ActivityEvent::toString, ActivityEvent::toString,
                java.util.Comparator.comparing(ActivityEvent::getTimestamp).reversed()
                        .thenComparing(ActivityEvent::getLogId, util.IdentifierHelper.NATURAL_ORDER.reversed()),
                event -> authentication.audit(event, "ACTIVITY_LOG", "", "SUCCESS", "Navigated log list."));
        authentication.audit("VIEW_LOG", "ACTIVITY_LOG", "", "SUCCESS", "Viewed activity history.");
    }

    public void notifications(Scanner scanner) {
        List<Notification> notifications = authentication.getNotifications().listMine(authentication);
        ListView.show(scanner, "MY NOTIFICATIONS", notifications, Notification::toString, Notification::toString,
                java.util.Comparator.comparing(Notification::getTimestamp).reversed(),
                event -> authentication.audit(event, "NOTIFICATION", "", "SUCCESS", "Navigated own inbox."));
        System.out.print("Notification ID to mark read (blank-Back): ");
        String id = scanner.nextLine().trim();
        if (!id.isEmpty()) {
            boolean marked = authentication.getNotifications().markRead(authentication, id);
            authentication.audit("READ_NOTIFICATION", "NOTIFICATION", id, marked ? "SUCCESS" : "FAILED",
                    marked ? "Marked own notification read." : "Notification unavailable or could not be saved.");
            if (!marked) {
                ConsoleTheme.warning("Notification was not found in your inbox or could not be saved.");
            }
        }
        authentication.audit("VIEW_NOTIFICATIONS", "NOTIFICATION", "", "SUCCESS", "Viewed own inbox.");
    }

    public void review(Scanner scanner) throws IOException {
        authentication.require(Permission.REVIEW_ATTENDANCE);
        while (true) {
            ConsoleTheme.menu("DATA REVIEW", "Scan and list CSV issues", "Correct attendance with evidence");
            int choice = InputHelper.getInt(scanner, "Choice (0-2): ", 0, 2);
            if (choice == 0) {
                return;
            }
            if (choice == 1) {
                List<String> issues = site.reviewAttendance();
                if (issues.isEmpty()) {
                    ConsoleTheme.success("No issues; business data is active.");
                } else {
                    ListView.show(scanner, "CSV ISSUES", issues, s -> s, s -> s, null,
                            event -> authentication.audit(event, "REVIEW", "", "SUCCESS", "Navigated issues."));
                }
            } else {
                String id = InputHelper.getString(scanner, "Attendance ID: ");
                String zone = InputHelper.getString(scanner, "Verified zone ID: ");
                String timestamp = InputHelper.getString(scanner, "Verified timestamp (yyyy-MM-dd HH:mm:ss): ");
                int type = InputHelper.getInt(scanner, "1-Check in, 2-Check out: ", 1, 2);
                String reason = InputHelper.getString(scanner, "Correction reason: ");
                String evidence = InputHelper.getString(scanner, "Evidence/reference: ");
                String confirm = InputHelper.getString(scanner, "Save this verified correction? (yes/no): ");
                if (confirm.equalsIgnoreCase("yes")) {
                    site.correctAttendance(id, zone, timestamp, type == 1 ? "CHECK_IN" : "CHECK_OUT", reason, evidence);
                    ConsoleTheme.success("Correction saved. Data active: " + site.isDataReady());
                } else {
                    authentication.audit("CORRECT_ATTENDANCE", "ATTENDANCE", id, "CANCELLED", "Review cancelled.");
                }
            }
        }
    }

    public void reports(Scanner scanner) throws IOException {
        authentication.require(Permission.EXPORT_REPORTS);
        int type = InputHelper.getInt(scanner, "1-Attendance report, 2-Incident report, 0-Back: ", 0, 2);
        if (type == 0) {
            return;
        }
        LocalDate from = DateTimeHelper.parseDate(InputHelper.getDate(scanner, "From date (dd/MM/yyyy): "));
        LocalDate to = DateTimeHelper.parseDate(InputHelper.getDate(scanner, "To date (dd/MM/yyyy): "));
        int grouping = InputHelper.getInt(scanner, "1-Day, 2-Week (Monday), 3-Month: ", 1, 3);
        ReportService.Period period = ReportService.Period.values()[grouping - 1];
        String person = "";
        String zone = "";
        if (type == 1) {
            System.out.print("Person code filter (blank-All): ");
            person = scanner.nextLine().trim();
            System.out.print("Zone ID filter (blank-All): ");
            zone = scanner.nextLine().trim();
        }
        List<String[]> rows = site.report(from, to, period, type == 2, person, zone);
        ListView.show(scanner, "REPORT", rows, Arrays::toString, Arrays::toString, null,
                event -> authentication.audit(event, "REPORT", "", "SUCCESS", "Navigated report."));
        System.out.print("Export parent directory (blank-Back): ");
        String destination = scanner.nextLine().trim();
        if (!destination.isEmpty()) {
            ConsoleTheme.success("Report exported: " + site.exportReport(rows, Paths.get(destination)));
        }
    }
}
