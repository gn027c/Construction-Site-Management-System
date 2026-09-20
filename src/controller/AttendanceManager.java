package controller;

import model.AttendanceRecord;
import model.Incident;
import java.util.ArrayList;
import java.util.List;

/**
 * NHIỆM VỤ: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Module quản lý chu kỳ quẹt thẻ điểm danh và vòng đời hồ sơ sự cố an toàn (Attendance & Incident Lifecycle).
 * - Chịu trách nhiệm thực hiện các thuật toán: Quẹt thẻ FSM (chặn check-in kép), Quản lý xử lý sự cố.
 */
public class AttendanceManager {
    private final List<AttendanceRecord> attendanceList = new ArrayList<>();
    private final List<Incident> incidentList = new ArrayList<>();

    /**
     * Quẹt thẻ điểm danh với thuật toán kiểm tra chu kỳ vào/ra hợp lệ (FSM).
     * YÊU CẦU LOGIC (Lợi tự viết):
     * - Chặn CHECK_IN liên tiếp 2 lần khi chưa CHECK_OUT.
     * - Chặn CHECK_OUT khi chưa từng CHECK_IN.
     * - Nếu hợp lệ, tự sinh mã recordId, gắn thời gian hiện tại và thêm vào attendanceList.
     */
    public boolean processAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type) {
        // TODO: [Lợi] Cài đặt thuật toán kiểm tra chu kỳ FSM và quẹt thẻ điểm danh tại đây
        return false;
    }

    /**
     * Lấy toàn bộ lịch sử quẹt thẻ điểm danh.
     */
    public List<AttendanceRecord> getAllAttendanceRecords() {
        return new ArrayList<>(attendanceList);
    }

    /**
     * Báo cáo ghi nhận một sự cố mới phát sinh tại công trường.
     * YÊU CẦU LOGIC (Lợi tự viết):
     * - Kiểm tra incident != null, thêm vào incidentList.
     */
    public boolean reportIncident(Incident incident) {
        // TODO: [Lợi] Cài đặt thuật toán lưu trữ sự cố tại đây
        return false;
    }

    /**
     * Phân công người chịu trách nhiệm xử lý sự cố.
     * YÊU CẦU LOGIC (Lợi tự viết):
     * - Tìm sự cố theo incidentId.
     * - Cập nhật assignedTo và đổi trạng thái sự cố sang ASSIGNED.
     */
    public boolean assignIncident(String incidentId, String assigneeCode) {
        // TODO: [Lợi] Cài đặt thuật toán phân công sự cố tại đây
        return false;
    }

    /**
     * Đóng và hoàn tất giải quyết sự cố.
     * YÊU CẦU LOGIC (Lợi tự viết):
     * - Tìm sự cố theo incidentId.
     * - Đổi trạng thái sự cố sang RESOLVED.
     */
    public boolean resolveIncident(String incidentId) {
        // TODO: [Lợi] Cài đặt thuật toán hoàn tất sự cố tại đây
        return false;
    }

    /**
     * Lấy danh sách tất cả các sự cố.
     */
    public List<Incident> getAllIncidents() {
        return new ArrayList<>(incidentList);
    }
}
