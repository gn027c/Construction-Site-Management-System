package controller;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
 * MÔ TẢ:
 * - Lớp Controller điều phối trung tâm quản trị toàn bộ hệ thống SmartSite (Facade Pattern).
 * - Kết hợp 4 module quản lý chuyên biệt:
 *   + personnelManager: Do Trần Ngọc Anh Tuấn (SE201513) phụ trách.
 *   + zoneManager: Do Lê Tấn Thiên (SE201852) phụ trách.
 *   + attendanceManager, incidentManager: Do Nguyễn Tấn Lợi (SE211059) phụ trách.
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
}
