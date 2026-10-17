import view.MenuView;
import util.InputHelper;
import controller.SiteManagerController;
import exception.DataValidationException;
import exception.SiteManagementException;
import java.util.Scanner;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461) - Nhóm trưởng
 * MÔ TẢ:
 * - Application Bootstrap & Console View Entry Point.
 * - Giao tiếp duy nhất với SiteManagerController (Facade Pattern).
 * - Quản lý vòng lặp Menu chính, bắt sự kiện lựa chọn và xử lý Exception toàn cục.
 */
public class Main {

    public static void main(String[] args) {
        
        SiteManagerController siteController = new SiteManagerController();
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        System.out.println("=================================================");
        System.out.println("    WELCOME TO SMARTSITE MANAGEMENT SYSTEM        ");
        System.out.println("=================================================");

        // Task 1.2: Tự động nạp dữ liệu khi bắt đầu chương trình
        try {
            siteController.loadAllData();
            System.out.println("✅ [SYSTEM] All CSV data loaded successfully!");
        } catch (Exception e) {
            System.err.println("⚠️ [SYSTEM WARNING] Failed to auto-load initial data: " + e.getMessage());
        }

        while (!exit) {
            try {
                
                MenuView.showMainMenu();

                // Nâng giới hạn lựa chọn từ (0-4) lên (0-5)
                int choice = InputHelper.getInt(scanner, "Enter your choice (0-5): ", 0, 5);

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
                    case 5:
                        // Task 1.3: Quản lý sao lưu / nạp lại dữ liệu thủ công
                        handleDataManagementMenu(siteController, scanner);
                        break;
                    case 0:
                        // Task 1.2: Tự động lưu dữ liệu trước khi thoát[cite: 1]
                        siteController.saveAllData();
                        exit = true;
                        System.out.println("\n✅ Data saved successfully!");
                        System.out.println("Thank you for using SmartSite System! Goodbye.");
                        break;
                    default:
                        System.out.println("Invalid selection!");
                }
            } catch (DataValidationException e) {
                System.err.println("⚠️ [DATA VALIDATION ERROR] " + e.getMessage());
            } catch (SiteManagementException e) {
                System.err.println("❌ [SITE MANAGEMENT ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.err.println("💥 [UNEXPECTED ERROR] " + e.getMessage());
            }

            System.out.println();
        }

        scanner.close();
    }

    /**
     * Sub-routine xử lý Menu quản lý sao lưu / nạp lại dữ liệu thủ công (Task 1.3)[cite: 1]
     */
    private static void handleDataManagementMenu(SiteManagerController siteController, Scanner scanner) {
        boolean back = false;
        while (!back) {
            try {
                MenuView.showDataMenu();
                int choice = InputHelper.getInt(scanner, "Enter your choice (0-2): ", 0, 2);

                switch (choice) {
                    case 1:
                        siteController.loadAllData();
                        System.out.println("✅ Data reloaded successfully from CSV files.");
                        break;
                    case 2:
                        siteController.saveAllData();
                        System.out.println("✅ Data saved manually to CSV files successfully.");
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid selection!");
                }
            } catch (Exception e) {
                System.err.println("⚠️ [DATA MANAGEMENT ERROR] " + e.getMessage());
            }
            System.out.println();
        }
    }
}
