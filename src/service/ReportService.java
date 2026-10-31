package service;

import exception.DataValidationException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import model.attendance.AttendanceRecord;
import model.incident.Incident;
import util.DateTimeHelper;
import util.IdentifierHelper;

/**
 * Filters business records and builds calendar-based summaries without file or console access.
 * Date ranges include both endpoints; archived incidents remain part of the reporting history.
 */
public final class ReportService {
    public enum Period {
        DAY,
        WEEK,
        MONTH
    }

    private ReportService() {
    }

    public static List<AttendanceRecord> filterAttendance(
            List<AttendanceRecord> records,
            LocalDate from,
            LocalDate to,
            String personCode,
            String zoneId
    ) {
        checkRange(from, to);
        String person = optionalCode(personCode);
        String zone = optionalCode(zoneId);
        List<AttendanceRecord> matching = new ArrayList<>();
        for (AttendanceRecord record : records) {
            LocalDate date = DateTimeHelper.parseTimestamp(record.getTimestamp()).toLocalDate();
            if (within(date, from, to)
                    && (person.isEmpty() || person.equals(record.getPersonCode()))
                    && (zone.isEmpty() || zone.equals(record.getZoneId()))) {
                matching.add(record);
            }
        }
        matching.sort(
                Comparator.comparing(AttendanceRecord::getTimestamp)
                        .thenComparing(AttendanceRecord::getRecordId, IdentifierHelper.NATURAL_ORDER)
        );
        return matching;
    }

    private static String optionalCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return IdentifierHelper.normalize(value);
    }

    /**
     * Produces CSV-ready rows grouped by calendar day, Monday-based week or month.
     */
    public static List<String[]> summarizeAttendance(
            List<AttendanceRecord> records,
            LocalDate from,
            LocalDate to,
            String personCode,
            String zoneId,
            Period period
    ) {
        checkPeriod(period);
        Map<LocalDate, int[]> counts = new TreeMap<>();
        for (AttendanceRecord record : filterAttendance(records, from, to, personCode, zoneId)) {
            LocalDate date = DateTimeHelper.parseTimestamp(record.getTimestamp()).toLocalDate();
            int[] values = counts.computeIfAbsent(bucket(date, period), ignored -> new int[2]);
            int column = record.getCheckType() == AttendanceRecord.CheckType.CHECK_IN ? 0 : 1;
            values[column]++;
        }
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "period", "checkIns", "checkOuts" });
        for (Map.Entry<LocalDate, int[]> entry : counts.entrySet()) {
            rows.add(new String[] {
                    entry.getKey().toString(),
                    Integer.toString(entry.getValue()[0]),
                    Integer.toString(entry.getValue()[1])
            });
        }
        return rows;
    }

    /**
     * Groups reported and resolved events by their actual dates.
     * Status and archive counts describe incidents reported in each bucket.
     * The UNKNOWN row separately counts historical dates that cannot be placed in the requested period.
     */
    public static List<String[]> summarizeIncidents(
            List<Incident> incidents,
            LocalDate from,
            LocalDate to,
            Period period
    ) {
        checkRange(from, to);
        checkPeriod(period);
        Map<LocalDate, int[]> counts = new TreeMap<>();
        int[] unknown = new int[8];
        for (Incident incident : incidents) {
            if (incident.getReportedAt().isEmpty()) {
                unknown[6]++;
                addStatus(unknown, incident);
            } else {
                LocalDate reported = DateTimeHelper.parseTimestamp(incident.getReportedAt()).toLocalDate();
                if (within(reported, from, to)) {
                    int[] values = counts.computeIfAbsent(bucket(reported, period), ignored -> new int[8]);
                    values[0]++;
                    addStatus(values, incident);
                }
            }
            if (incident.isResolved()) {
                if (incident.getResolvedAt().isEmpty()) {
                    unknown[7]++;
                } else {
                    LocalDate resolved = DateTimeHelper.parseTimestamp(incident.getResolvedAt()).toLocalDate();
                    if (within(resolved, from, to)) {
                        int[] values = counts.computeIfAbsent(bucket(resolved, period), ignored -> new int[8]);
                        values[1]++;
                    }
                }
            }
        }
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] {
                "period", "reported", "resolved", "open", "assigned", "resolvedStatus",
                "archived", "unknownReportedAt", "unknownResolvedAt"
        });
        for (Map.Entry<LocalDate, int[]> entry : counts.entrySet()) {
            rows.add(summaryRow(entry.getKey().toString(), entry.getValue()));
        }
        rows.add(summaryRow("UNKNOWN", unknown));
        return rows;
    }

    private static void addStatus(int[] values, Incident incident) {
        switch (incident.getStatus()) {
            case OPEN:
                values[2]++;
                break;
            case ASSIGNED:
                values[3]++;
                break;
            case RESOLVED:
                values[4]++;
                break;
            default:
                throw new IllegalStateException("Unsupported incident status.");
        }
        if (incident.isArchived()) {
            values[5]++;
        }
    }

    private static String[] summaryRow(String label, int[] values) {
        String[] row = new String[values.length + 1];
        row[0] = label;
        for (int i = 0; i < values.length; i++) {
            row[i + 1] = Integer.toString(values[i]);
        }
        return row;
    }

    public static LocalDate weekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private static LocalDate bucket(LocalDate date, Period period) {
        switch (period) {
            case DAY:
                return date;
            case WEEK:
                return weekStart(date);
            case MONTH:
                return date.withDayOfMonth(1);
            default:
                throw new IllegalArgumentException("Unsupported reporting period.");
        }
    }

    private static boolean within(LocalDate date, LocalDate from, LocalDate to) {
        return !date.isBefore(from) && !date.isAfter(to);
    }

    private static void checkRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new DataValidationException("dateRange", "Provide a start date that does not follow the end date.");
        }
    }

    private static void checkPeriod(Period period) {
        if (period == null) {
            throw new DataValidationException("period", "Must not be null.");
        }
    }
}
