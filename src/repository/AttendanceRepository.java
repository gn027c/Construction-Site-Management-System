package repository;

import model.AttendanceRecord;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeParseException;
import util.DateTimeHelper;

/**
 * Stores attendance records in insertion order and handles attendance CSV.
 *
 * <p>Record identifiers must be unique. CSV loading builds a separate collection
 * and replaces active data only after all rows have been accepted.
 */
public class AttendanceRepository {
    private final List<AttendanceRecord> attendanceList = new ArrayList<>();

    /**
     * Stores a record after checking its timestamp and unique record identifier.
     */
    public boolean save(AttendanceRecord record) {
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
        return attendanceList.add(record);
    }

    /**
     * Finds the most recently inserted swipe for a normalized personnel code.
     *
     * <p>Insertion order defines the preceding swipe. Records are not sorted by timestamp.
     */
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

    /**
     * Returns a separate list in storage order containing the stored model objects.
     *
     * <p>Changing the returned list does not change this repository's collection.
     * The model objects themselves are shared and retain their validated operations.
     */
    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(attendanceList);
    }

    /**
     * Loads CSV into a temporary repository before replacing active data.
     * A read or validation failure returns false and retains the current collection.
     */
    public boolean loadFromCsv(String filePath) {
        AttendanceRepository staged = new AttendanceRepository();
        try {
            List<String[]> rows = util.CsvHelper.readRows(filePath);
            if (rows.isEmpty() || !java.util.Arrays.equals(rows.get(0), new String[] {
                    "recordId", "personCode", "zoneId", "timestamp", "checkType"
            })) {
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                if (util.CsvHelper.isBlankRow(p)) {
                    continue;
                }
                if (p.length != 5 || p[0].trim().isEmpty() || p[1].trim().isEmpty() || p[2].trim().isEmpty()) {
                    return false;
                }
                if (
                        !staged.save(
                                new AttendanceRecord(p[0], p[1], p[2], p[3], AttendanceRecord.CheckType.valueOf(p[4]))
                        )
                ) {
                    return false;
                }
            }
        } catch (RuntimeException e) {
            return false;
        }
        attendanceList.clear();
        attendanceList.addAll(staged.attendanceList);
        return true;
    }

    /**
     * Serializes current data and requests an atomic replacement of one CSV file.
     */
    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("recordId,personCode,zoneId,timestamp,checkType");
        for (AttendanceRecord rec : attendanceList) {
            try {
                DateTimeHelper.parseTimestamp(rec.getTimestamp());
            } catch (DateTimeParseException e) {
                return false;
            }
            lines.add(rec.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }
}
