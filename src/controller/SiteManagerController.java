package controller;

import model.AttendanceRecord;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
 * MÔ TẢ:
 * - Lớp Controller điều phối trung tâm quản trị toàn bộ hệ thống SmartSite (Facade Pattern / Service Layer).
 * - Kết hợp và điều phối giữa 4 module quản lý chuyên biệt:
 *   + personnelManager: Do Trần Ngọc Anh Tuấn (SE201513) phụ trách.
 *   + zoneManager: Do Lê Tấn Thiên (SE201852) phụ trách.
 *   + attendanceManager, incidentManager: Do Nguyễn Tấn Lợi (SE211059) phụ trách.
 * - Cung cấp các phương thức nghiệp vụ liên module (Cross-module Orchestration):
 *   + recordAttendance: Xác thực nhân sự -> Xác thực quyền phân vùng an toàn -> Ghi nhận quẹt thẻ FSM.
 *   + assignIncident: Xác thực nhân sự phụ trách -> Phân công xử lý sự cố.
 */
public class SiteManagerController {
    private final PersonnelManager personnelManager;
    private final ZoneManager zoneManager;
    private final AttendanceManager attendanceManager;
    private final IncidentManager incidentManager;

    public SiteManagerController() {
        this.personnelManager = new PersonnelManager();
        this.zoneManager = new ZoneManager();
        this.attendanceManager = new AttendanceManager();
        this.incidentManager = new IncidentManager();
    }

    public PersonnelManager getPersonnelManager() {
        return personnelManager;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public AttendanceManager getAttendanceManager() {
        return attendanceManager;
    }

    public IncidentManager getIncidentManager() {
        return incidentManager;
    }

    // --- Cross-module Business Workflows (Facade Delegation) ---

    /**
     * Nghiệp vụ điểm danh tổng hợp (ủy nhiệm cho AttendanceManager):
     * Xác thực nhân sự -> Xác thực quyền phân vùng an toàn -> Xử lý FSM -> Hiển thị đa hình.
     */
    public boolean recordAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type) {
        return attendanceManager.recordAttendance(personnelManager, zoneManager, personCode, zoneId, type);
    }

    /**
     * Nghiệp vụ phân công sự cố an toàn (ủy nhiệm cho IncidentManager):
     * Xác thực nhân sự phụ trách -> Gán người xử lý và cập nhật trạng thái.
     */
    public boolean assignIncident(String incidentId, String assigneeCode) {
        return incidentManager.assignIncident(personnelManager, incidentId, assigneeCode);
    }
}

