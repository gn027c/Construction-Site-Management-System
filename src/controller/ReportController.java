package controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import model.AttendanceRecord;
import model.Incident;
import model.Permission;
import service.AuthenticationService;
import service.ReportService;
import util.InputHelper;
import view.MenuView;
import view.ConsoleUi;

public class ReportController {
    private static final DateTimeFormatter DATE = util.DateHelper.DATE_FORMATTER;

    private final ReportService reports;
    private final AuthenticationService authentication;

    public ReportController(ReportService reports, AuthenticationService authentication) {
        this.reports = reports;
        this.authentication = authentication;
    }

    public void handleMenu(Scanner scanner) {
        authentication.require(Permission.EXPORT_REPORTS);
        boolean back = false;
        while (!back) {
            MenuView.showReportMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-4): ", 0, 4);
            switch (choice) {
                case 1:
                    displayReport(reports.currentWeek());
                    break;
                case 2:
                    displayReport(reports.currentMonth());
                    break;
                case 3:
                    displayReport(customPeriod(scanner));
                    break;
                case 4:
                    exportReport(scanner, customOrCurrentPeriod(scanner));
                    break;
                default:
                    back = true;
                    break;
            }
        }
    }

    private ReportService.ReportPeriod customOrCurrentPeriod(Scanner scanner) {
        System.out.println("1. Current week  2. Current month  3. Custom period");
        int choice = InputHelper.getInt(scanner, "Choose period (1-3): ", 1, 3);
        if (choice == 1) {
            return reports.currentWeek();
        }
        if (choice == 2) {
            return reports.currentMonth();
        }
        return customPeriod(scanner);
    }

    private ReportService.ReportPeriod customPeriod(Scanner scanner) {
        String from = InputHelper.getString(scanner, "From date (dd/MM/yyyy): ");
        String to = InputHelper.getString(scanner, "To date (dd/MM/yyyy): ");
        try {
            return new ReportService.ReportPeriod(LocalDate.parse(from, DATE), LocalDate.parse(to, DATE));
        } catch (java.time.DateTimeException e) {
            throw new exception.DataValidationException("Report dates must use dd/MM/yyyy.");
        }
    }

    private void displayReport(ReportService.ReportPeriod period) {
        List<AttendanceRecord> attendance = reports.attendanceBetween(period);
        List<Incident> incidents = reports.incidentsBetween(period);
        List<Incident> undated = reports.incidentsWithoutReportedAt();
        ConsoleUi.section("REPORT PERIOD: " + period);
        System.out.println("Attendance records: " + attendance.size());
        System.out.println("Attendance summary: " + reports.attendanceSummary(period));
        if (!attendance.isEmpty()) {
            String[] headers = { "ID", "PERSON", "ZONE", "TIMESTAMP", "ACTION" };
            List<String[]> rows = new java.util.ArrayList<>();
            for (AttendanceRecord record : attendance) {
                rows.add(new String[] {
                        record.getRecordId(), record.getPersonCode(), record.getZoneId(),
                        record.getTimestamp(), record.getCheckType().name()
                });
            }
            ConsoleUi.table(headers, rows);
        }
        ConsoleUi.section("INCIDENTS");
        System.out.println("Incidents: " + incidents.size());
        System.out.println("By severity: " + reports.incidentSeveritySummary(period));
        System.out.println("By status: " + reports.incidentStatusSummary(period));
        if (!incidents.isEmpty()) {
            String[] headers = { "ID", "SEVERITY", "STATUS", "ASSIGNED", "TITLE" };
            List<String[]> rows = new java.util.ArrayList<>();
            for (Incident incident : incidents) {
                rows.add(new String[] {
                        incident.getIncidentId(), incident.getSeverity().name(),
                        incident.getStatus().name(), incident.getAssignedTo().isEmpty() ? "-" : incident.getAssignedTo(),
                        incident.getTitle()
                });
            }
            ConsoleUi.table(headers, rows);
        }
        if (!undated.isEmpty()) {
            ConsoleUi.section("UNDATED INCIDENTS FROM LEGACY CSV");
            String[] headers = { "ID", "SEVERITY", "STATUS", "ASSIGNED", "TITLE" };
            List<String[]> rows = new java.util.ArrayList<>();
            for (Incident incident : undated) {
                rows.add(new String[] {
                        incident.getIncidentId(), incident.getSeverity().name(),
                        incident.getStatus().name(), incident.getAssignedTo().isEmpty() ? "-" : incident.getAssignedTo(),
                        incident.getTitle()
                });
            }
            ConsoleUi.table(headers, rows);
        }
        ConsoleUi.section("CURRENT OCCUPANCY BY ZONE");
        for (Map.Entry<String, Integer> entry : reports.currentOccupancy().entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
    }

    private void exportReport(Scanner scanner, ReportService.ReportPeriod period) {
        String directory = InputHelper.getString(scanner, "Output directory: ");
        if (!reports.export(period, directory)) {
            throw new exception.AppError("Could not write report files.");
        }
        System.out.println("Reports exported to " + directory + ".");
    }
}
