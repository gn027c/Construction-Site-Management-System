import view.MenuView;
import util.InputHelper;
import controller.SiteManagerController;
import java.util.Scanner;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
 * MÔ TẢ:
 * - Application Bootstrap & Console View Entry Point.
 * - Chỉ giao tiếp duy nhất với SiteManagerController (Facade Pattern).
 * - Quản lý vòng lặp Menu chính và bắt sự kiện lựa chọn từ người dùng.
 */
public class Main {

    public static void main(String[] args) {
        // Giao diện chỉ khởi tạo duy nhất Facade Controller trung tâm
        SiteManagerController siteController = new SiteManagerController();
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            MenuView.showMainMenu();
            int choice = InputHelper.getInt(scanner, "Enter your choice (0-4): ", 0, 4);

            switch (choice) {
                case 1:
                    siteController.handlePersonMenu(scanner);
                    break;
                case 2:
                    siteController.handleZoneMenu(scanner);
                    break;
                case 3:
                    siteController.handleAttendanceMenu(scanner);
                    break;
                case 4:
                    siteController.handleIncidentMenu(scanner);
                    break;
                case 0:
                    exit = true;
                    System.out.println("Thank you for using SmartSite System! Goodbye.");
                    break;
                default:
                    System.out.println("Invalid selection!");
            }
        }
        scanner.close();
    }
}
