package repository;

import model.AttendanceRecord;
import java.util.ArrayList;
import java.util.List;

public class AttendanceRepository {
    private static final String HEADER = "recordId,personCode,zoneId,timestamp,checkType";
    private static final int COLUMN_COUNT = 5;
    private static final java.time.format.DateTimeFormatter TIMESTAMP = util.DateHelper.TIMESTAMP_FORMATTER;

    private final List<AttendanceRecord> attendanceList = new ArrayList<>();
    private final java.util.Map<String, AttendanceRecord> latestByPerson = new java.util.HashMap<>();

    public boolean save(AttendanceRecord record) {
        if (record == null) {
            return false;
        }
        return attendanceList.add(record);
    }

    public AttendanceRecord findLastByPersonCode(String personCode) {
        if (personCode == null || personCode.trim().isEmpty()) {
            return null;
        }
        String cleanCode = personCode.trim();
        for (int i = attendanceList.size() - 1; i >= 0; i--) {
            AttendanceRecord rec = attendanceList.get(i);
            if (util.IdHelper.isSame(rec.getPersonCode(), cleanCode)) {
                return rec;
            }
        }
        return null;
    }

    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(attendanceList);
    }

    static String normalizeTimestamp(String raw) {
        if (raw == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(\\d{2})/(\\d{2})/(\\d{4}) (\\d{2}):(\\d{2})").matcher(raw);
        if (m.matches()) {
            return m.group(3) + "-" + m.group(2) + "-" + m.group(1) + " " + m.group(4) + ":" + m.group(5) + ":00";
        }
        return raw;
    }

    public boolean loadFromCsv(String filePath) {
        latestByPerson.clear();
        List<AttendanceRecord> records = util.CsvHelper.readTable(
                filePath, HEADER, COLUMN_COUNT, this::parse, AttendanceRecord::getRecordId);
        if (records == null) {
            return false;
        }
        attendanceList.clear();
        attendanceList.addAll(records);
        return true;
    }

    private AttendanceRecord parse(List<String> columns) {
        String timestamp = normalizeTimestamp(columns.get(3).trim());
        java.time.LocalDateTime.parse(timestamp, TIMESTAMP);
        AttendanceRecord record = new AttendanceRecord(
                columns.get(0).trim(), columns.get(1).trim(), columns.get(2).trim(), timestamp,
                AttendanceRecord.CheckType.valueOf(columns.get(4).trim()));
        String person = record.getPersonCode().toUpperCase();
        record.assertCanFollow(latestByPerson.get(person));
        latestByPerson.put(person, record);
        return record;
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (AttendanceRecord rec : attendanceList) {
            lines.add(rec.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }

}
