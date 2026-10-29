package controller;

import model.AttendanceRecord;
import service.AttendanceService;
import view.MenuView;
import util.InputHelper;
import java.util.Scanner;

/**
 * Handles console input and result messages for attendance recording and history.
 *
 * <p>The service performs lookups and delegates swipe ordering to the model.
 * The controller presents recoverable access and attendance errors.
 *
 * <p>Responsibility: Nguyen Tan Loi (SE211059) - attendance console flows.
 */
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    public void handleMenu(Scanner scanner) {
        handleMenu(scanner, () -> {
        });
    }

    /**
     * Runs the submenu and calls the supplied action after modifying menu choices.
     *
     * <p>The callback runs after the flow returns, including cancellation or handled
     * errors. Exceptions escaping the flow prevent the callback from running.
     *
     * @param scanner the shared console input reader
     * @param saveChanges the action used to persist current data
     */
    public void handleMenu(Scanner scanner, Runnable saveChanges) {
        boolean back = false;
        while (!back) {
            MenuView.showAttendanceMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    recordAttendanceFlow(scanner);
                    break;
                case 2:
                    java.util.List<AttendanceRecord> records = attendanceService.getAllAttendanceRecords();
                    if (records.isEmpty()) {
                        view.ConsoleTheme.warning("No attendance records.");
                    }
                    for (AttendanceRecord record : records) {
                        System.out.println(record);
                    }
                    break;
                case 0:
                    back = true;
                    break;
            }
            if (choice == 1) {
                saveChanges.run();
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
        if (typeChoice == 0) {
            return;
        }
        AttendanceRecord.CheckType type = (typeChoice == 1)
                ? AttendanceRecord.CheckType.CHECK_IN
                : AttendanceRecord.CheckType.CHECK_OUT;
        try {
            if (attendanceService.recordAttendance(personCode, zoneId, type)) {
                view.ConsoleTheme.success("Attendance recorded: " + type + " at " + zoneId);
            } else {
                System.out.println("Invalid attendance parameters.");
            }
        } catch (exception.AttendanceOrderException e) {
            view.ConsoleTheme.warning("Attendance Order Violation: " + e.getMessage());
        } catch (exception.RestrictedZoneViolationException e) {
            view.ConsoleTheme.warning("Restricted Zone Error: " + e.getMessage());
        } catch (exception.ZoneNotFoundException e) {
            view.ConsoleTheme.error("Zone Error: " + e.getMessage());
        }
    }
}
