package controller;

import model.AttendanceRecord;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * NHIỆM VỤ: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Module quản lý chu kỳ quẹt thẻ điểm danh ra/vào (Attendance Lifecycle).
 * - Chịu trách nhiệm thực hiện thuật toán: Quẹt thẻ FSM (chặn check-in kép,
 * check-out sai khu vực).
 * - Nghiệp vụ sự cố được tách sang IncidentManager.
 */
public class AttendanceManager {
    private final List<AttendanceRecord> attendanceList = new ArrayList<>();
    private int recordCounter = 1;

    // Flow: validate đầu vào -> tìm bản ghi gần nhất của nhân sự -> áp dụng FSM
    // vào/ra -> sinh recordId + timestamp -> lưu vào attendanceList
    public boolean processAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type) {
        if (personCode == null || personCode.trim().isEmpty() || zoneId == null || zoneId.trim().isEmpty()
                || type == null) {
            return false;
        }

        String cleanPersonCode = personCode.trim();
        String cleanZoneId = zoneId.trim();

        // Duyệt ngược tìm bản ghi điểm danh gần nhất của nhân sự (O(N))
        AttendanceRecord lastRecord = null;
        for (int i = attendanceList.size() - 1; i >= 0; i--) {
            AttendanceRecord rec = attendanceList.get(i);
            if (rec.getPersonCode().equalsIgnoreCase(cleanPersonCode)) {
                lastRecord = rec;
                break;
            }
        }

        // FSM: không CHECK_IN khi đang ở trong; CHECK_OUT chỉ hợp lệ khi đang ở trong
        // đúng khu vực đã CHECK_IN
        if (type == AttendanceRecord.CheckType.CHECK_IN) {
            if (lastRecord != null && lastRecord.getCheckType() == AttendanceRecord.CheckType.CHECK_IN) {
                return false;
            }
        } else if (type == AttendanceRecord.CheckType.CHECK_OUT) {
            if (lastRecord == null || lastRecord.getCheckType() == AttendanceRecord.CheckType.CHECK_OUT
                    || !lastRecord.getZoneId().equalsIgnoreCase(cleanZoneId)) {
                return false;
            }
        }

        // Mã record bắt đầu bằng prefix: REC
        String recordId = String.format("REC%04d", recordCounter++);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        AttendanceRecord newRecord = new AttendanceRecord(recordId, cleanPersonCode, cleanZoneId, timestamp, type);
        attendanceList.add(newRecord);
        return true;
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return new ArrayList<>(attendanceList);
    }

    public void displayAllAttendanceRecords() {
        if (attendanceList.isEmpty()) {
            System.out.println("No attendance records yet.");
            return;
        }
        for (AttendanceRecord r : attendanceList) {
            System.out.println(r);
        }
    }

    /**
     * Nghiệp vụ điểm danh kết nối liên module:
     * 1. Xác thực nhân sự tồn tại qua PersonnelManager.
     * 2. Nếu là CHECK_IN, xác thực quyền an toàn qua ZoneManager (đa hình Zone.checkAccess).
     * 3. Xử lý FSM quẹt thẻ qua processAttendance.
     * 4. Gọi đa hình person.displayDetails() khi thành công.
     */
    public boolean recordAttendance(PersonnelManager personnelManager, ZoneManager zoneManager,
                                   String personCode, String zoneId, AttendanceRecord.CheckType type) {
        if (personCode == null || zoneId == null || type == null) {
            System.out.println("Error: Invalid attendance parameters!");
            return false;
        }
        model.Person person = personnelManager.findPersonByCode(personCode);
        if (person == null) {
            System.out.println("Error: Person '" + personCode + "' not found!");
            return false;
        }

        if (type == AttendanceRecord.CheckType.CHECK_IN && !zoneManager.verifyAccess(zoneId, person)) {
            return false;
        }

        if (processAttendance(person.getCode(), zoneId, type)) {
            System.out.println(type + " recorded successfully for:");
            person.displayDetails();
            return true;
        } else {
            System.out.println("Error: " + type + " rejected (already checked in, or checking out of a different zone).");
            return false;
        }
    }
}
