package service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.AttendanceRecord;
import model.Incident;
import util.CsvHelper;

public class ReportService {
    private static final java.time.format.DateTimeFormatter TIMESTAMP = util.DateHelper.TIMESTAMP_FORMATTER;

    private final AttendanceService attendanceService;
    private final IncidentService incidentService;

    public ReportService(AttendanceService attendanceService, IncidentService incidentService) {
        if (attendanceService == null || incidentService == null) {
            throw new IllegalArgumentException("Report services are required.");
        }
        this.attendanceService = attendanceService;
        this.incidentService = incidentService;
    }

    public ReportPeriod currentWeek() {
        LocalDate today = LocalDate.now();
        return new ReportPeriod(
                today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)));
    }

    public ReportPeriod currentMonth() {
        LocalDate today = LocalDate.now();
        return new ReportPeriod(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()));
    }

    public List<AttendanceRecord> attendanceBetween(ReportPeriod period) {
        List<AttendanceRecord> result = new ArrayList<>();
        for (AttendanceRecord record : attendanceService.getAllAttendanceRecords()) {
            LocalDate date = LocalDateTime.parse(record.getTimestamp(), TIMESTAMP).toLocalDate();
            if (period.includes(date)) {
                result.add(record);
            }
        }
        return result;
    }

    public List<Incident> incidentsBetween(ReportPeriod period) {
        List<Incident> result = new ArrayList<>();
        for (Incident incident : incidentService.getAllIncidents()) {
            if (incident.getReportedAt() != null && period.includes(incident.getReportedAt().toLocalDate())) {
                result.add(incident);
            }
        }
        return result;
    }

    public List<Incident> incidentsWithoutReportedAt() {
        List<Incident> result = new ArrayList<>();
        for (Incident incident : incidentService.getAllIncidents()) {
            if (incident.getReportedAt() == null) {
                result.add(incident);
            }
        }
        return result;
    }

    public Map<AttendanceRecord.CheckType, Integer> attendanceSummary(ReportPeriod period) {
        Map<AttendanceRecord.CheckType, Integer> summary = new EnumMap<>(AttendanceRecord.CheckType.class);
        for (AttendanceRecord.CheckType type : AttendanceRecord.CheckType.values()) {
            summary.put(type, 0);
        }
        for (AttendanceRecord record : attendanceBetween(period)) {
            summary.put(record.getCheckType(), summary.get(record.getCheckType()) + 1);
        }
        return summary;
    }

    public Map<Incident.IncidentSeverity, Integer> incidentSeveritySummary(ReportPeriod period) {
        Map<Incident.IncidentSeverity, Integer> summary = new EnumMap<>(Incident.IncidentSeverity.class);
        for (Incident.IncidentSeverity severity : Incident.IncidentSeverity.values()) {
            summary.put(severity, 0);
        }
        for (Incident incident : incidentsBetween(period)) {
            summary.put(incident.getSeverity(), summary.get(incident.getSeverity()) + 1);
        }
        return summary;
    }

    public Map<Incident.IncidentStatus, Integer> incidentStatusSummary(ReportPeriod period) {
        Map<Incident.IncidentStatus, Integer> summary = new EnumMap<>(Incident.IncidentStatus.class);
        for (Incident.IncidentStatus status : Incident.IncidentStatus.values()) {
            summary.put(status, 0);
        }
        for (Incident incident : incidentsBetween(period)) {
            summary.put(incident.getStatus(), summary.get(incident.getStatus()) + 1);
        }
        return summary;
    }

    public Map<String, Integer> currentOccupancy() {
        Map<String, AttendanceRecord> latest = new LinkedHashMap<>();
        for (AttendanceRecord record : attendanceService.getAllAttendanceRecords()) {
            String personCode = record.getPersonCode().toUpperCase();
            AttendanceRecord current = latest.get(personCode);
            if (current == null || LocalDateTime.parse(record.getTimestamp(), TIMESTAMP)
                    .isAfter(LocalDateTime.parse(current.getTimestamp(), TIMESTAMP))) {
                latest.put(personCode, record);
            }
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (AttendanceRecord record : latest.values()) {
            if (record.getCheckType() == AttendanceRecord.CheckType.CHECK_IN) {
                counts.put(record.getZoneId(), counts.containsKey(record.getZoneId())
                        ? counts.get(record.getZoneId()) + 1 : 1);
            }
        }
        return counts;
    }

    public String attendanceCsv(ReportPeriod period) {
        List<String> lines = new ArrayList<>();
        lines.add("recordId,personCode,zoneId,timestamp,checkType");
        for (AttendanceRecord record : attendanceBetween(period)) {
            lines.add(record.toCsvLine());
        }
        return String.join(System.lineSeparator(), lines);
    }

    public String incidentCsv(ReportPeriod period) {
        List<String> lines = new ArrayList<>();
        lines.add("incidentId,title,description,severity,status,assignedTo,reportedAt");
        for (Incident incident : incidentsBetween(period)) {
            lines.add(incident.toCsvLine());
        }
        return String.join(System.lineSeparator(), lines);
    }

    public boolean export(ReportPeriod period, String directory) {
        java.nio.file.Path output = java.nio.file.Paths.get(directory);
        try {
            java.nio.file.Files.createDirectories(output);
        } catch (java.io.IOException e) {
            return false;
        }
        List<String> attendance = new ArrayList<>();
        attendance.add("recordId,personCode,zoneId,timestamp,checkType");
        for (AttendanceRecord record : attendanceBetween(period)) {
            attendance.add(record.toCsvLine());
        }
        List<String> incidents = new ArrayList<>();
        incidents.add("incidentId,title,description,severity,status,assignedTo,reportedAt");
        for (Incident incident : incidentsBetween(period)) {
            incidents.add(incident.toCsvLine());
        }
        List<String> undated = new ArrayList<>();
        undated.add("incidentId,title,description,severity,status,assignedTo,reportedAt");
        for (Incident incident : incidentsWithoutReportedAt()) {
            undated.add(incident.toCsvLine());
        }
        return CsvHelper.writeAllLines(output.resolve("attendance-report.csv").toString(), attendance)
                && CsvHelper.writeAllLines(output.resolve("incident-report.csv").toString(), incidents)
                && CsvHelper.writeAllLines(output.resolve("incident-undated.csv").toString(), undated);
    }

    public static final class ReportPeriod {
        private final LocalDate from;
        private final LocalDate to;

        public ReportPeriod(LocalDate from, LocalDate to) {
            if (from == null || to == null || to.isBefore(from)) {
                throw new IllegalArgumentException("Invalid report period.");
            }
            this.from = from;
            this.to = to;
        }

        public LocalDate getFrom() {
            return from;
        }

        public LocalDate getTo() {
            return to;
        }

        public boolean includes(LocalDate date) {
            return date != null && !date.isBefore(from) && !date.isAfter(to);
        }

        @Override
        public String toString() {
            return from + " to " + to;
        }
    }
}
