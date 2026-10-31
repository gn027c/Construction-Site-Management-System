package controller;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import model.auth.Permission;
import service.AuthenticationService;
import service.ReportService;
import util.DateTimeHelper;
import util.InputHelper;
import view.ConsoleTheme;
import view.ListView;

/**
 * Presents administrator diagnostics and filtered period reports.
 */
public final class WorkspaceController {
    private final SiteManagerController site;
    private final AuthenticationService authentication;

    public WorkspaceController(SiteManagerController site) {
        this.site = site;
        authentication = site.getAuthentication();
    }

    public void review(Scanner scanner) {
        authentication.require(Permission.REVIEW_ATTENDANCE);
        List<String> issues = site.reviewAttendance();
        if (issues.isEmpty()) {
            ConsoleTheme.success("No issues; business data is active.");
            return;
        }
        ListView.show(
                scanner, "CSV VALIDATION ERRORS / READ ONLY", issues, text -> text, text -> text, null
        );
        ConsoleTheme.warning("Source CSV was not changed. Correct the source file, then run validation again.");
    }

    public void attendanceReport(Scanner scanner) throws IOException {
        generateReport(scanner, 1, "ATTENDANCE REPORT");
    }

    public void incidentReport(Scanner scanner) throws IOException {
        generateReport(scanner, 2, "INCIDENT REPORT");
    }

    public void reports(Scanner scanner) throws IOException {
        authentication.require(Permission.EXPORT_REPORTS);
        ConsoleTheme.title("REPORTS");
        int type = InputHelper.getMenuChoice(scanner, "1-Attendance report, 2-Incident report, 0-Back: ", 2);
        if (type == 0) {
            return;
        }
        generateReport(scanner, type, type == 1 ? "ATTENDANCE REPORT" : "INCIDENT REPORT");
    }

    private void generateReport(Scanner scanner, int type, String title) throws IOException {
        authentication.require(Permission.EXPORT_REPORTS);
        LocalDate[] bounds = site.getReportDateBounds(type == 2);
        LocalDate from = bounds[0];
        LocalDate to = bounds[1];
        ReportService.Period period = ReportService.Period.DAY;
        String person = "";
        String zone = "";

        while (true) {
            List<String[]> rows = site.report(from, to, period, type == 2, person, zone);
            ConsoleTheme.breadcrumb("REPORTS / " + title);
            System.out.println("  Active Filter: " + DateTimeHelper.formatDate(from) + " -> "
                    + DateTimeHelper.formatDate(to) + " | Grouping: " + period
                    + (person.isEmpty() ? "" : " | Person: " + person)
                    + (zone.isEmpty() ? "" : " | Zone: " + zone));
            System.out.println();

            if (rows.isEmpty() || rows.size() <= 1) {
                ConsoleTheme.warning("No data found for the current period and filters.");
            } else {
                List<String> headers = Arrays.asList(rows.get(0));
                List<String[]> dataRows = rows.subList(1, rows.size());
                List<List<String>> tableData = new ArrayList<>();
                for (String[] r : dataRows) {
                    tableData.add(Arrays.asList(r));
                }
                ListView.printTable(headers, tableData);
            }

            System.out.println();
            Map<Integer, String> options = new LinkedHashMap<>();
            options.put(1, "Filter by date range, grouping or person");
            options.put(2, "Export report to CSV");
            ConsoleTheme.menuOptions(title, "View, filter or export report data.", options, "Back");

            int choice = InputHelper.getMenuChoice(scanner, "Choice (0-2): ", 2);
            if (choice == 0) {
                return;
            } else if (choice == 1) {
                LocalDate newFrom = DateTimeHelper.parseDate(InputHelper.getDate(scanner, "From date (dd/MM/yyyy): "));
                LocalDate newTo = DateTimeHelper.parseDate(InputHelper.getDate(scanner, "To date (dd/MM/yyyy): "));
                if (newFrom.isAfter(newTo)) {
                    ConsoleTheme.warning("Start date cannot be after end date.");
                    continue;
                }
                int grouping = InputHelper.getInt(scanner, "1-Day, 2-Week (Monday), 3-Month: ", 1, 3);
                ReportService.Period newPeriod = ReportService.Period.values()[grouping - 1];
                String newPerson = "";
                String newZone = "";
                if (type == 1) {
                    newPerson = InputHelper.readLine(scanner, "Person code filter (blank-All): ").trim();
                    newZone = InputHelper.readLine(scanner, "Zone ID filter (blank-All): ").trim();
                }
                from = newFrom;
                to = newTo;
                period = newPeriod;
                person = newPerson;
                zone = newZone;
            } else if (choice == 2) {
                String destination = InputHelper.readLine(scanner, "Export parent directory (Enter/0-Cancel): ").trim();
                if (!destination.isEmpty() && !destination.equals("0")) {
                    ConsoleTheme.success("Report exported: " + site.exportReport(rows, Paths.get(destination)));
                }
            }
        }
    }
}
