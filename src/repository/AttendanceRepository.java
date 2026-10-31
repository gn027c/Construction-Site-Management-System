package repository;

import model.attendance.AttendanceRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.format.DateTimeParseException;
import util.DateTimeHelper;

public class AttendanceRepository {
    private final List<AttendanceRecord> attendanceList = new ArrayList<>();
    private String lastLoadError = "";

    public boolean save(AttendanceRecord record) {
        if (!canAppend(record)) {
            return false;
        }
        return attendanceList.add(record);
    }

    private boolean canAppend(AttendanceRecord record) {
        if (record == null) {
            return false;
        }
        try {
            DateTimeHelper.parseTimestamp(record.getTimestamp());
        } catch (DateTimeParseException e) {
            return false;
        }
        for (AttendanceRecord existing : attendanceList) {
            if (existing.getRecordId().equals(record.getRecordId())) {
                return false;
            }
        }
        record.assertCanFollow(findLastByPersonCode(record.getPersonCode()));
        return true;
    }

    public boolean saveAndPersist(AttendanceRecord record, String filePath) {
        if (!canAppend(record)) {
            return false;
        }
        List<AttendanceRecord> candidate = new ArrayList<>(attendanceList);
        candidate.add(record);
        if (!writeRecords(filePath, candidate)) {
            return false;
        }
        attendanceList.add(record);
        return true;
    }

    public AttendanceRecord findLastByPersonCode(String personCode) {
        String cleanCode = util.IdentifierHelper.normalizeForLookup(personCode);
        if (cleanCode == null) {
            return null;
        }
        for (int i = attendanceList.size() - 1; i >= 0; i--) {
            AttendanceRecord rec = attendanceList.get(i);
            if (rec.getPersonCode().equals(cleanCode)) {
                return rec;
            }
        }
        return null;
    }

    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(attendanceList);
    }

    public String getLastLoadError() {
        return lastLoadError;
    }

    public boolean loadFromCsv(String filePath) {
        AttendanceRepository staged = new AttendanceRepository();
        lastLoadError = "";
        String location = "header";
        try {
            List<String[]> rows = util.CsvHelper.readRows(filePath);
            if (rows.isEmpty() || !java.util.Arrays.equals(rows.get(0), new String[] {
                    "recordId", "personCode", "zoneId", "timestamp", "checkType"
            })) {
                lastLoadError = "Expected the attendance CSV header with five columns.";
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                location = "row " + (i + 1);
                if (util.CsvHelper.isBlankRow(p)) {
                    continue;
                }
                if (p.length != 5 || p[0].trim().isEmpty() || p[1].trim().isEmpty() || p[2].trim().isEmpty()) {
                    lastLoadError = location + ": expected five columns with record, person and zone identifiers.";
                    return false;
                }
                location += " / " + p[0];
                if (
                        !staged.save(
                                new AttendanceRecord(p[0], p[1], p[2], p[3], AttendanceRecord.CheckType.valueOf(p[4]))
                        )
                ) {
                    lastLoadError = location + ": invalid or duplicate attendance record.";
                    return false;
                }
            }
        } catch (RuntimeException e) {
            lastLoadError = location + ": " + e.getMessage();
            return false;
        }
        attendanceList.clear();
        attendanceList.addAll(staged.attendanceList);
        return true;
    }

    public boolean saveToCsv(String filePath) {
        return writeRecords(filePath, attendanceList);
    }

    private boolean writeRecords(String filePath, List<AttendanceRecord> records) {
        List<String> lines = new ArrayList<>();
        Map<String, AttendanceRecord> previous = new HashMap<>();
        lines.add("recordId,personCode,zoneId,timestamp,checkType");
        for (AttendanceRecord rec : records) {
            try {
                DateTimeHelper.parseTimestamp(rec.getTimestamp());
                rec.assertCanFollow(previous.get(rec.getPersonCode()));
            } catch (RuntimeException e) {
                return false;
            }
            previous.put(rec.getPersonCode(), rec);
            lines.add(rec.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }
}
