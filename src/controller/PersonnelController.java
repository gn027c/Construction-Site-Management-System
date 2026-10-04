package controller;

import service.PersonnelService;
import view.MenuView;
import util.InputHelper;
import java.util.Scanner;

/**
 * TẦNG ĐIỀU KHIỂN (CONTROLLER LAYER)
 * PHỤ TRÁCH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Điều hướng luồng tương tác Console cho Menu Quản lý Nhân sự.
 * - Thu thập input, gọi PersonnelService và hiển thị kết quả ra giao diện.
 */
public class PersonnelController {
    private final PersonnelService personnelService;

    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    public void handleMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showPersonMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-3): ", 0, 3);
            switch (choice) {
                case 1:
                    // TODO: Member 2 (Tran Ngoc Anh Tuan - SE201513) add person flow
                    break;
                case 2:
                    // TODO: Member 2 (Tran Ngoc Anh Tuan - SE201513) display person list flow
                    break;
                case 3:
                    // TODO: Member 2 (Tran Ngoc Anh Tuan - SE201513) search & update person flow
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }
}
