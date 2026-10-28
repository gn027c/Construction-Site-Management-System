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
        AttendanceRepository staged = new AttendanceRepository();
        try {
            List<String[]> rows = util.CsvHelper.readRows(filePath);
            if (rows.isEmpty() || !rows.get(0)[0].equals("recordId")) return false;
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                if (util.CsvHelper.isBlankRow(p)) continue;
                if (p.length != 5 || p[0].trim().isEmpty() || p[1].trim().isEmpty() || p[2].trim().isEmpty()) return false;
                String stamp = normalizeTimestamp(p[3]);
                java.time.LocalDateTime.parse(stamp, java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withResolverStyle(java.time.format.ResolverStyle.STRICT));
                if (!staged.save(new AttendanceRecord(p[0], p[1], p[2], stamp, AttendanceRecord.CheckType.valueOf(p[4])))) return false;
            }
        } catch (RuntimeException e) { return false; }
        attendanceList.clear(); attendanceList.addAll(staged.attendanceList);
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
