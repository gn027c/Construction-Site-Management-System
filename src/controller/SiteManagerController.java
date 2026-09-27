package controller;

import model.AttendanceRecord;
import model.Incident;
import model.Person;

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

    // --- Cross-module Business Workflows (Facade Methods) ---

    /**
     * Nghiệp vụ điểm danh tổng hợp:
     * 1. Xác thực nhân sự tồn tại trong hệ thống.
     * 2. Nếu là CHECK_IN, xác thực quyền vào phân vùng thi công (đa hình Zone.checkAccess).
     * 3. Kiểm tra chu kỳ vào/ra qua máy trạng thái FSM (AttendanceManager.processAttendance).
     * 4. Hiển thị thông tin nhân sự đa hình (Person.displayDetails) khi thành công.
     */
    public boolean recordAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type) {
        if (personCode == null || zoneId == null || type == null) {
            System.out.println("Error: Invalid attendance parameters!");
            return false;
        }
        Person person = personnelManager.findPersonByCode(personCode);
        if (person == null) {
            System.out.println("Error: Person '" + personCode + "' not found!");
            return false;
        }

        // Chỉ khi vào khu vực mới cần kiểm tra quyền an toàn; ra khỏi khu vực do FSM kiểm soát
        if (type == AttendanceRecord.CheckType.CHECK_IN && !zoneManager.verifyAccess(zoneId, person)) {
            return false;
        }

        if (attendanceManager.processAttendance(person.getCode(), zoneId, type)) {
            System.out.println(type + " recorded successfully for:");
            person.displayDetails();
            return true;
        } else {
            System.out.println("Error: " + type + " rejected (already checked in, or checking out of a different zone).");
            return false;
        }
    }

    /**
     * Nghiệp vụ phân công sự cố an toàn:
     * 1. Xác thực nhân sự được phân công có tồn tại trong hệ thống.
     * 2. Gán người xử lý và chuyển trạng thái sang ASSIGNED (IncidentManager.assignIncident).
     */
    public boolean assignIncident(String incidentId, String assigneeCode) {
        if (personnelManager.findPersonByCode(assigneeCode) == null) {
            System.out.println("Error: Person '" + assigneeCode + "' not found!");
            return false;
        }
        if (incidentManager.assignIncident(incidentId, assigneeCode)) {
            System.out.println("Incident assigned successfully.");
            return true;
        } else {
            System.out.println("Error: Incident not found or already RESOLVED.");
            return false;
        }
    }
}

