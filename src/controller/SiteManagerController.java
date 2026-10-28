package controller;

import service.PersonnelService;
import service.ZoneService;
import service.AttendanceService;
import service.IncidentService;
import java.util.Scanner;

/**
 * BỘ ĐIỀU PHỐI TRUNG TÂM (FACADE CONTROLLER)
 * PHỤ TRÁCH: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
 * MÔ TẢ:
 * - Đóng gói toàn bộ các Service và Sub-Controller của hệ thống.
 * - Cung cấp điểm giao tiếp duy nhất (Single Point of Contact) cho Main.java.
 * - Giúp Main hoàn toàn độc lập, không cần biết chi tiết khởi tạo bên trong.
 */
public class SiteManagerController {

    private final PersonnelService personnelService;
    private final ZoneService zoneService;
    private final AttendanceService attendanceService;
    private final IncidentService incidentService;

    private final PersonnelController personnelController;
    private final ZoneController zoneController;
    private final AttendanceController attendanceController;
    private final IncidentController incidentController;

    public SiteManagerController() {
        // 1. Khởi tạo tầng Service nội bộ
        this.personnelService = new PersonnelService();
        this.zoneService = new ZoneService();
        this.attendanceService = new AttendanceService(personnelService, zoneService);
        this.incidentService = new IncidentService(personnelService);

        // 2. Khởi tạo các Controller nhánh tương ứng
        this.personnelController = new PersonnelController(personnelService);
        this.zoneController = new ZoneController(zoneService, personnelService);
        this.attendanceController = new AttendanceController(attendanceService);
        this.incidentController = new IncidentController(incidentService);
    }

    /**
     * Task 1.2: Nạp tự động dữ liệu từ tệp CSV của cả 4 module/repository khi ứng dụng khởi chạy
     */
    public void loadAllData() {
        personnelService.loadFromCsv();
        zoneService.loadFromCsv();
        attendanceService.loadFromCsv();
        incidentService.loadFromCsv();
    }

    /**
     * Task 1.2: Lưu tự động toàn bộ dữ liệu ra tệp CSV của cả 4 module/repository trước khi thoát hoặc lưu thủ công
     */
    public void saveAllData() {
        personnelService.saveToCsv();
        zoneService.saveToCsv();
        attendanceService.saveToCsv();
        incidentService.saveToCsv();
    }

    public void handlePersonMenu(Scanner scanner) {
        personnelController.handleMenu(scanner);
    }

    public void handleZoneMenu(Scanner scanner) {
        zoneController.handleMenu(scanner);
    }

    public void handleAttendanceMenu(Scanner scanner) {
        attendanceController.handleMenu(scanner);
    }

    public void handleIncidentMenu(Scanner scanner) {
        incidentController.handleMenu(scanner);
    }
}
