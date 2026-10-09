package controller;

import model.AttendanceRecord;
import service.AttendanceService;
import view.MenuView;
import util.InputHelper;
import java.util.Scanner;

/**
 * TẦNG ĐIỀU KHIỂN (CONTROLLER LAYER)
 * PHỤ TRÁCH: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Điều hướng luồng tương tác Console cho Menu Điểm danh & Kiểm soát vào/ra.
 * - Thu thập input, gọi AttendanceService và hiển thị kết quả ra giao diện.
 */
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    public void handleMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showAttendanceMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    recordAttendanceFlow(scanner);
                    break;
                case 2:
                    attendanceService.displayAllAttendanceRecords();
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }

    private void recordAttendanceFlow(Scanner scanner) {
        System.out.println("\n--- RECORD ATTENDANCE / ACCESS CONTROL ---");
        String personCode = InputHelper.getString(scanner, "Enter Person Code (e.g., EMP001): ");
        String zoneId = InputHelper.getString(scanner, "Enter Zone ID (e.g., Z01): ");

        System.out.println("Select Action:");
        System.out.println("1. CHECK-IN (Enter Zone)");
        System.out.println("2. CHECK-OUT (Leave Zone)");
        int typeChoice = InputHelper.getInt(scanner, "Choose action (1-2, 0-Cancel): ", 0, 2);
        if (typeChoice == 0) return;

        AttendanceRecord.CheckType type = (typeChoice == 1) 
                ? AttendanceRecord.CheckType.CHECK_IN 
                : AttendanceRecord.CheckType.CHECK_OUT;

        try {
            attendanceService.recordAttendance(personCode, zoneId, type);
        } catch (exception.AttendanceOrderException e) {
            System.out.println("Attendance Order Violation: " + e.getMessage());
        }
    }
}
