import controller.SiteManagerController;
import util.DataDirectoryLock;
import util.InputHelper;
import view.ConsoleTheme;
import view.MenuView;
import java.nio.file.*;
import java.util.*;

/** Console bootstrap. CSV data is managed through the Facade and services. */
public class Main {
    public static void main(String[] args) {
        Path directory = Paths.get(System.getProperty("smartsite.dataDir", "data"));
        try (DataDirectoryLock lock = new DataDirectoryLock(directory); Scanner scanner = new Scanner(System.in)) {
            SiteManagerController controller = new SiteManagerController(directory);
            controller.loadAllData();
            ConsoleTheme.success("CSV data loaded.");
            boolean exit = false;
            while (!exit) {
                try {
                    MenuView.showMainMenu();
                    int choice = InputHelper.getInt(scanner, "Choice (0-5): ", 0, 5);
                    switch (choice) {
                        case 1: controller.handlePersonMenu(scanner); break;
                        case 2: controller.handleZoneMenu(scanner); break;
                        case 3: controller.handleAttendanceMenu(scanner); break;
                        case 4: controller.handleIncidentMenu(scanner); break;
                        case 5:
                            MenuView.showDataMenu();
                            int action = InputHelper.getInt(scanner, "Choice (0-3): ", 0, 3);
                            if (action == 1) {
                                String answer = InputHelper.getString(scanner, "Discard unsaved changes and reload CSV files? (yes/no): ");
                                if (answer.equalsIgnoreCase("yes")) {
                                    controller.loadAllData();
                                    ConsoleTheme.success("CSV data reloaded.");
                                }
                            } else if (action == 2) {
                                controller.saveAllData(); ConsoleTheme.success("CSV data saved.");
                            } else if (action == 3) {
                                String path = InputHelper.getString(scanner, "Export parent directory: ");
                                ConsoleTheme.success("Export completed: " + controller.exportCsv(Paths.get(path)));
                            }
                            break;
                        case 0:
                            controller.saveAllData();
                            ConsoleTheme.success("CSV data saved. Goodbye.");
                            exit = true;
                            break;
                    }
                } catch (NoSuchElementException e) {
                    controller.saveAllData();
                    ConsoleTheme.success("Input closed. CSV data saved.");
                    exit = true;
                } catch (Exception e) {
                    ConsoleTheme.error(e.getMessage());
                }
            }
        } catch (Exception e) {
            ConsoleTheme.error("Session stopped: " + e.getMessage());
            System.exit(1);
        }
    }
}
