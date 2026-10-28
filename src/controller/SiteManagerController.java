package controller;

import service.PersonnelService;
import service.ZoneService;
import service.AttendanceService;
import service.IncidentService;
import java.util.Scanner;

/**
 * BỘ ĐIỀU PHỐI TRUNG TÂM (FACADE CONTROLLER)
 * PHỤ TRÁCH: Khang SE201461 - tích hợp; Tuấn SE201513 - nhân sự;
 * Thiên SE201852 - khu vực; Lợi SE211059 - điểm danh và sự cố.
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

    private final Runnable checkpoint;
    public SiteManagerController() { this(new state.AppState(), () -> { }); }
    public SiteManagerController(state.AppState state) { this(state, () -> { }); }

    public SiteManagerController(state.AppState state, Runnable checkpoint) {
        this.checkpoint = checkpoint;
        // 1. Khởi tạo tầng Service nội bộ
        this.personnelService = state.personnel;
        this.zoneService = state.zones;
        this.attendanceService = state.attendance;
        this.incidentService = state.incidents;

        // 2. Khởi tạo các Controller nhánh tương ứng
        this.personnelController = new PersonnelController(personnelService);
        this.zoneController = new ZoneController(zoneService, personnelService);
        this.attendanceController = new AttendanceController(attendanceService);
        this.incidentController = new IncidentController(incidentService);
    }

    public void handlePersonMenu(Scanner scanner) {
        personnelController.handleMenu(scanner, checkpoint);
    }

    public void handleZoneMenu(Scanner scanner) {
        zoneController.handleMenu(scanner, checkpoint);
    }

    public void handleAttendanceMenu(Scanner scanner) {
        attendanceController.handleMenu(scanner, checkpoint);
    }

    public void handleIncidentMenu(Scanner scanner) {
        incidentController.handleMenu(scanner, checkpoint);
    }
}

