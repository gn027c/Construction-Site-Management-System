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
        String id = InputHelper.readLine(scanner, "Notification ID to mark read (blank-Back): ").trim();
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

    public void review(Scanner scanner) {
        authentication.require(Permission.REVIEW_ATTENDANCE);
        List<String> issues = site.reviewAttendance();
        if (issues.isEmpty()) {
            ConsoleTheme.success("No issues; business data is active.");
            return;
        }
        ListView.show(
                scanner, "CSV VALIDATION ERRORS / READ ONLY", issues, text -> text, text -> text, null,
                event -> authentication.audit(event, "REVIEW", "", "SUCCESS", "Navigated validation errors.")
        );
        ConsoleTheme.warning("Source CSV was not changed. Correct the source file, then run validation again.");
    }

    public void reports(Scanner scanner) throws IOException {
        authentication.require(Permission.EXPORT_REPORTS);
        ConsoleTheme.title("PERIOD REPORTS");
        int type = InputHelper.getMenuChoice(scanner, "1-Attendance report, 2-Incident report, 0-Back: ", 2);
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
            person = InputHelper.readLine(scanner, "Person code filter (blank-All): ").trim();
            zone = InputHelper.readLine(scanner, "Zone ID filter (blank-All): ").trim();
        }
        List<String[]> rows = site.report(from, to, period, type == 2, person, zone);
        ListView.show(scanner, "REPORT", rows, Arrays::toString, Arrays::toString, null,
                event -> authentication.audit(event, "REPORT", "", "SUCCESS", "Navigated report."));
        String destination = InputHelper.readLine(scanner, "Export parent directory (blank-Back): ").trim();
        if (!destination.isEmpty()) {
            ConsoleTheme.success("Report exported: " + site.exportReport(rows, Paths.get(destination)));
        }
    }
}
