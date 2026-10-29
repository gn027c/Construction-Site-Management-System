package controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import model.ActivityEvent;
import model.AttendanceRecord;
import model.Notification;
import model.Permission;
import service.AuthenticationService;
import service.ReportService;
import util.DateTimeHelper;
import util.InputHelper;
import view.ConsoleTheme;
import view.AttendanceReviewView;
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

    public void review(Scanner scanner) throws IOException {
        authentication.require(Permission.REVIEW_ATTENDANCE);
        while (true) {
            ConsoleTheme.menu(
                    "DATA REVIEW", "Scan and list CSV issues", "Correct attendance with evidence",
                    "View a person's attendance timeline"
            );
            AttendanceReviewView.showGuide();
            int choice = InputHelper.getMenuChoice(scanner, "Choice (0-3): ", 3);
            view.ConsoleScreen.begin();
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
            } else if (choice == 3) {
                String personCode = InputHelper.getString(scanner, "Person code from the issue (e.g., W1): ");
                List<AttendanceRecord> timeline = site.attendanceForReview(personCode);
                ListView.show(
                        scanner, "ATTENDANCE TIMELINE / CSV ORDER / " + util.IdentifierHelper.normalize(personCode),
                        timeline, AttendanceReviewView::formatRecord, AttendanceReviewView::formatRecord, null,
                        event -> authentication.audit(event, "REVIEW", "", "SUCCESS", "Navigated timeline.")
                );
            } else {
                String id = InputHelper.getString(scanner, "Attendance ID: ");
                String normalizedId = util.IdentifierHelper.normalize(id);
                AttendanceRecord original = site.attendanceForReview("").stream()
                        .filter(record -> record.getRecordId().equals(normalizedId))
                        .findFirst()
                        .orElseThrow(() -> new exception.DataValidationException("recordId", "Record does not exist."));
                AttendanceReviewView.showOriginal(original);
                String zone = replacement(scanner, "Verified zone ID", original.getZoneId());
                String timestamp = replacement(
                        scanner, "Verified timestamp (yyyy-MM-dd HH:mm:ss)", original.getTimestamp()
                );
                int type = InputHelper.getInt(scanner, "1-Check in, 2-Check out, 0-Keep original: ", 0, 2);
                String checkType = type == 0 ? original.getCheckType().name() : type == 1 ? "CHECK_IN" : "CHECK_OUT";
                String reason = InputHelper.getString(scanner, "Correction reason: ");
                String evidence = InputHelper.getString(scanner, "Evidence/reference: ");
                System.out.println("Proposed: " + normalizedId + " | " + original.getPersonCode()
                        + " | " + timestamp + " | " + checkType + " | " + zone);
                String confirm = InputHelper.readLine(
                        scanner, "Save this verified correction? (yes to save, Enter/no to cancel): "
                ).trim();
                if (confirm.equalsIgnoreCase("yes")) {
                    site.correctAttendance(id, zone, timestamp, checkType, reason, evidence);
                    ConsoleTheme.success("Correction saved.");
                    if (site.isDataReady()) {
                        ConsoleTheme.success("All checks passed. Business data is active.");
                    } else {
                        ConsoleTheme.warning("Other inconsistencies remain. Choose option 1 to review them.");
                    }
                } else {
                    authentication.audit("CORRECT_ATTENDANCE", "ATTENDANCE", id, "CANCELLED", "Review cancelled.");
                    ConsoleTheme.warning("Correction cancelled. Original record retained.");
                }
            }
        }
    }

    private static String replacement(Scanner scanner, String label, String current) {
        String value = InputHelper.readLine(scanner, label + " [" + current + "]: ").trim();
        return value.isEmpty() ? current : value;
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
