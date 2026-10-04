package controller;

import model.RestrictedZone;
import model.Zone;
import service.ZoneService;
import view.MenuView;
import util.InputHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 * TẦNG ĐIỀU KHIỂN (CONTROLLER LAYER)
 * PHỤ TRÁCH: Thành viên 3 (Lê Tấn Thiên - SE201852)
 * MÔ TẢ:
 * - Điều hướng luồng tương tác Console cho Menu Quản lý Phân vùng.
 * - Thu thập input, gọi ZoneService và hiển thị kết quả ra giao diện.
 */
public class ZoneController {
    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    public void handleMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showZoneMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-2): ", 0, 2);
            switch (choice) {
                case 1:
                    addZoneFlow(scanner);
                    break;
                case 2:
                    displayZoneListFlow();
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }

    private void addZoneFlow(Scanner scanner) {
        System.out.println("\n--- ADD NEW ZONE ---");
        int type = InputHelper.getInt(scanner, "Select zone type: 1. Normal Zone | 2. Restricted Zone (0-Cancel): ", 0, 2);
        if (type == 0) return;

        String zoneId = InputHelper.getString(scanner, "Enter Zone ID (e.g., Z01): ");
        String zoneName = InputHelper.getString(scanner, "Enter Zone Name: ");

        if (type == 1) {
            Zone zone = new Zone(zoneId, zoneName);
            zoneService.addZone(zone);
        } else {
            System.out.println("Select Safety Level: 1. LOW | 2. MEDIUM | 3. HIGH | 4. CRITICAL");
            int levelOpt = InputHelper.getInt(scanner, "Choice (1-4): ", 1, 4);
            
            RestrictedZone.SafetyLevel level;
            switch (levelOpt) {
                case 1: level = RestrictedZone.SafetyLevel.LOW; break;
                case 3: level = RestrictedZone.SafetyLevel.HIGH; break;
                case 4: level = RestrictedZone.SafetyLevel.CRITICAL; break;
                default: level = RestrictedZone.SafetyLevel.MEDIUM; break;
            }

            RestrictedZone restrictedZone = new RestrictedZone(zoneId, zoneName, level, new HashSet<>());
            zoneService.addZone(restrictedZone);
        }
    }

    private void displayZoneListFlow() {
        System.out.println("\n--- ZONE LIST ---");
        List<Zone> zones = zoneService.getAllZones();
        if (zones.isEmpty()) {
            System.out.println("No zones available.");
        } else {
            for (Zone z : zones) {
                System.out.println(z.toString());
            }
        }
    }
}
