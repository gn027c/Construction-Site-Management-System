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
}
