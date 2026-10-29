package repository;

import model.AttendanceRecord;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeParseException;
import util.DateTimeHelper;

/**
 * TẦNG LƯU TRỮ (REPOSITORY LAYER)
 * PHỤ TRÁCH: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ: Quản lý lưu trữ và truy xuất tập hợp bản ghi điểm danh (AttendanceRecord).
 */
public class AttendanceRepository {
    private final List<AttendanceRecord> attendanceList = new ArrayList<>();

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
            if (existing.getRecordId().equals(record.getRecordId())) return false;
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
            if (rec.getPersonCode().equals(cleanCode)) {
                return rec;
            }
        }
        return null;
    }

    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(attendanceList);
    }

    public boolean loadFromCsv(String filePath) {
        AttendanceRepository staged = new AttendanceRepository();
        try {
            List<String[]> rows = util.CsvHelper.readRows(filePath);
            if (rows.isEmpty() || !rows.get(0)[0].equals("recordId")) return false;
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                if (util.CsvHelper.isBlankRow(p)) continue;
                if (p.length != 5 || p[0].trim().isEmpty() || p[1].trim().isEmpty() || p[2].trim().isEmpty()) return false;
                if (!staged.save(new AttendanceRecord(p[0], p[1], p[2], p[3], AttendanceRecord.CheckType.valueOf(p[4])))) return false;
            }
        } catch (RuntimeException e) { return false; }
        attendanceList.clear(); attendanceList.addAll(staged.attendanceList);
        return true;
    }

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
