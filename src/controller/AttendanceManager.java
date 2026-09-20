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

    public boolean processAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type) {
        return false;
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return new ArrayList<>(attendanceList);
    }

    public boolean reportIncident(Incident incident) {
        return false;
    }

    public boolean assignIncident(String incidentId, String assigneeCode) {
        return false;
    }

    public boolean resolveIncident(String incidentId) {
        return false;
    }

    public List<Incident> getAllIncidents() {
        return new ArrayList<>(incidentList);
    }
}
