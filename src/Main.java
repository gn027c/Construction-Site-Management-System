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
        System.out.println("   WELCOME TO SMARTSITE MANAGEMENT SYSTEM        ");
        System.out.println("=================================================");

        while (!exit) {
            try {
                
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
                        System.out.println("\nThank you for using SmartSite System! Goodbye.");
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
}
