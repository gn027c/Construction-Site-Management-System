package repository;

import model.AttendanceRecord;
import java.util.ArrayList;
import java.util.List;

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
        return attendanceList.add(record);
    }

    public AttendanceRecord findLastByPersonCode(String personCode) {
        if (personCode == null || personCode.trim().isEmpty()) {
            return null;
        }
        String cleanCode = personCode.trim();
        for (int i = attendanceList.size() - 1; i >= 0; i--) {
            AttendanceRecord rec = attendanceList.get(i);
            if (rec.getPersonCode().equalsIgnoreCase(cleanCode)) {
                return rec;
            }
        }
        return null;
    }

    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(attendanceList);
    }

    /**
     * Chuẩn hóa thời gian điểm danh về yyyy-MM-dd HH:mm:ss (cùng định dạng code đang ghi).
     * Chấp nhận dữ liệu cũ dd/MM/yyyy HH:mm (dữ liệu mẫu). Giá trị không nhận ra thì giữ nguyên.
     */
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
        List<String> lines = util.CsvHelper.readAllLines(filePath);
        if (lines.isEmpty()) {
            return false;
        }
        attendanceList.clear();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (i == 0 && line.toLowerCase().startsWith("recordid")) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length >= 5) {
                String recordId = parts[0].trim();
                String personCode = parts[1].trim();
                String zoneId = parts[2].trim();
                String timestamp = normalizeTimestamp(parts[3].trim());
                AttendanceRecord.CheckType checkType;
                try {
                    checkType = AttendanceRecord.CheckType.valueOf(parts[4].trim().toUpperCase());
                } catch (Exception e) {
                    checkType = AttendanceRecord.CheckType.CHECK_IN;
                }
                attendanceList.add(new AttendanceRecord(recordId, personCode, zoneId, timestamp, checkType));
            }
        }
        return true;
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("recordId,personCode,zoneId,timestamp,checkType");
        for (AttendanceRecord rec : attendanceList) {
            lines.add(rec.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }
}
