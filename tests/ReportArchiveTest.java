import static support.Checks.*;

import exception.DataValidationException;
import exception.InvalidIncidentStateException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import model.attendance.AttendanceRecord;
import model.incident.Incident;
import repository.IncidentRepository;
import service.IncidentService;
import service.ReportService;
import util.CsvHelper;
import util.DateTimeHelper;

public class ReportArchiveTest {
    private static AttendanceRecord swipe(String id, String person, String zone, String time, boolean entering) {
        return new AttendanceRecord(
                id, person, zone, time,
                entering ? AttendanceRecord.CheckType.CHECK_IN : AttendanceRecord.CheckType.CHECK_OUT
        );
    }

    private static Incident saved(
            String id, Incident.IncidentStatus status, String reportedAt, String resolvedAt, boolean archived
    ) {
        return new Incident(
                id, "Hazard", "Description", Incident.IncidentSeverity.HIGH,
                status, status == Incident.IncidentStatus.OPEN ? "" : "W1",
                reportedAt, resolvedAt, archived
        );
    }

    public static void main(String[] args) throws Exception {
        test("Reporting and resolving capture strict timestamps without losing the assignee", () -> {
            Incident incident = Incident.report("inc-001", "Hazard", "Description", Incident.IncidentSeverity.HIGH);
            equal("INC1", incident.getIncidentId());
            DateTimeHelper.parseTimestamp(incident.getReportedAt());
            equal("", incident.getResolvedAt());
            incident.assignTo("w01");
            incident.resolve();
            DateTimeHelper.parseTimestamp(incident.getResolvedAt());
            require(!DateTimeHelper.parseTimestamp(incident.getResolvedAt()).isBefore(
                    DateTimeHelper.parseTimestamp(incident.getReportedAt())
            ));
            equal("W1", incident.getAssignedTo());
        });
        test("Historical reconstruction preserves unknown dates rather than inventing dates", () -> {
            Incident incident = new Incident(
                    "INC1", "Hazard", "Description", Incident.IncidentSeverity.LOW,
                    Incident.IncidentStatus.RESOLVED, "W1"
            );
            equal("", incident.getReportedAt());
            equal("", incident.getResolvedAt());
            require(!incident.isArchived());
        });
        test("Only resolved incidents can be archived and repeated archive is idempotent", () -> {
            Incident open = Incident.report("INC1", "Hazard", "Description", Incident.IncidentSeverity.LOW);
            throwsType(InvalidIncidentStateException.class, open::archive);
            require(!open.isArchived());
            open.assignTo("W1");
            throwsType(InvalidIncidentStateException.class, open::archive);
            open.resolve();
            require(open.archive());
            require(!open.archive());
            equal(Incident.IncidentStatus.RESOLVED, open.getStatus());
            equal("W1", open.getAssignedTo());
        });
        test("Persisted incident dates and archive flags respect lifecycle invariants", () -> {
            throwsType(DataValidationException.class, () -> saved(
                    "INC1", Incident.IncidentStatus.OPEN, "2026-10-12 07:00:00", "2026-10-12 08:00:00", false
            ));
            throwsType(DataValidationException.class, () -> saved(
                    "INC1", Incident.IncidentStatus.ASSIGNED, "", "", true
            ));
            throwsType(DataValidationException.class, () -> saved(
                    "INC1", Incident.IncidentStatus.RESOLVED, "2026-10-12 08:00:00", "2026-10-12 07:00:00", true
            ));
        });
        test("Archiving through the service retains all records and hides only archived records from active lists", () -> {
            IncidentRepository repository = new IncidentRepository();
            repository.save(saved("INC1", Incident.IncidentStatus.RESOLVED, "", "", false));
            repository.save(saved("INC2", Incident.IncidentStatus.ASSIGNED, "", "", false));
            IncidentService service = new IncidentService(repository, null);
            equal(1, service.archiveResolvedIncidents());
            equal(0, service.archiveResolvedIncidents());
            equal(2, service.getAllIncidents().size());
            equal(1, service.getActiveIncidents().size());
            require(service.findIncidentById("INC1").isArchived());
        });
        test("Incident serialization keeps all nine columns including unknown historical dates", () -> {
            Incident incident = saved("INC1", Incident.IncidentStatus.RESOLVED, "", "", true);
            String[] fields = CsvHelper.parseLine(incident.toCsvLine());
            equal(9, fields.length);
            equal("", fields[6]);
            equal("", fields[7]);
            equal("true", fields[8]);
        });
        test("Attendance filtering includes both date endpoints and normalized person and zone codes", () -> {
            List<AttendanceRecord> records = Arrays.asList(
                    swipe("REC1", "W1", "Z1", "2026-10-12 00:00:00", true),
                    swipe("REC2", "W1", "Z1", "2026-10-13 23:59:59", false),
                    swipe("REC3", "W2", "Z1", "2026-10-13 12:00:00", true),
                    swipe("REC4", "W1", "Z2", "2026-10-14 00:00:00", true)
            );
            List<AttendanceRecord> matching = ReportService.filterAttendance(
                    records, LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 13), "w01", "z-001"
            );
            equal(2, matching.size());
            equal("REC1", matching.get(0).getRecordId());
            equal("REC2", matching.get(1).getRecordId());
        });
        test("Daily attendance summaries count check-ins and check-outs separately", () -> {
            List<String[]> rows = ReportService.summarizeAttendance(
                    Arrays.asList(
                            swipe("REC1", "W1", "Z1", "2026-10-12 07:00:00", true),
                            swipe("REC2", "W1", "Z1", "2026-10-12 08:00:00", false),
                            swipe("REC3", "W2", "Z1", "2026-10-13 09:00:00", true)
                    ),
                    LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 13), "", "", ReportService.Period.DAY
            );
            equal(3, rows.size());
            equal("2026-10-12,1,1", String.join(",", rows.get(1)));
            equal("2026-10-13,1,0", String.join(",", rows.get(2)));
        });
        test("Weeks start on Monday and monthly summaries use calendar months", () -> {
            equal(LocalDate.of(2026, 10, 12), ReportService.weekStart(LocalDate.of(2026, 10, 18)));
            List<AttendanceRecord> records = Arrays.asList(
                    swipe("REC1", "W1", "Z1", "2026-10-18 23:59:59", true),
                    swipe("REC2", "W1", "Z1", "2026-10-19 00:00:00", false)
            );
            List<String[]> weeks = ReportService.summarizeAttendance(
                    records, LocalDate.of(2026, 10, 18), LocalDate.of(2026, 10, 19), "", "", ReportService.Period.WEEK
            );
            equal("2026-10-12", weeks.get(1)[0]);
            equal("2026-10-19", weeks.get(2)[0]);
            List<String[]> months = ReportService.summarizeAttendance(
                    records, LocalDate.of(2026, 10, 18), LocalDate.of(2026, 10, 19), "", "", ReportService.Period.MONTH
            );
            equal("2026-10-01,1,1", String.join(",", months.get(1)));
        });
        test("Incident reports keep archived incidents and group resolution by its own date", () -> {
            List<String[]> rows = ReportService.summarizeIncidents(
                    Arrays.asList(saved(
                            "INC1", Incident.IncidentStatus.RESOLVED,
                            "2026-01-31 09:00:00", "2026-02-01 09:00:00", true
                    )),
                    LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 1), ReportService.Period.DAY
            );
            equal("2026-01-31,1,0,0,0,1,1,0,0", String.join(",", rows.get(1)));
            equal("2026-02-01,0,1,0,0,0,0,0,0", String.join(",", rows.get(2)));
        });
        test("Unknown incident timestamps are reported separately from dated totals", () -> {
            List<String[]> rows = ReportService.summarizeIncidents(
                    Arrays.asList(saved("INC1", Incident.IncidentStatus.RESOLVED, "", "", true)),
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), ReportService.Period.MONTH
            );
            equal(2, rows.size());
            equal("UNKNOWN,0,0,0,0,1,1,1,1", String.join(",", rows.get(1)));
        });
        test("Report ranges reject reversed dates and accept an empty result", () -> {
            throwsType(DataValidationException.class, () -> ReportService.filterAttendance(
                    Arrays.asList(), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1), "", ""
            ));
            List<String[]> rows = ReportService.summarizeAttendance(
                    Arrays.asList(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1), "", "",
                    ReportService.Period.DAY
            );
            equal(1, rows.size());
        });
        finish();
    }
}
