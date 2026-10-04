package controller;

import service.ZoneService;
import view.MenuView;
import util.InputHelper;
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
                    // TODO: Member 3 (Le Tan Thien - SE201852) add zone flow
                    break;
                case 2:
                    // TODO: Member 3 (Le Tan Thien - SE201852) display zone list flow
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }
}
