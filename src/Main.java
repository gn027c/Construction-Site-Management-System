import controller.SiteManagerController;
import controller.AccountController;
import model.Permission;
import service.AuthenticationService;
import util.DataDirectoryLock;
import util.InputHelper;
import view.ConsoleTheme;
import view.MenuView;
import java.nio.file.*;
import java.util.*;

/**
 * Starts the console application and delegates operations to the facade.
 *
 * <p>The {@code smartsite.dataDir} property selects the data directory and defaults
 * to {@code data}. A directory lock is held for the session lifetime. Signed-in
 * sessions save current data on normal exit or end of input.
 */
public class Main {
    public static void main(String[] args) {
        Path directory = Paths.get(System.getProperty("smartsite.dataDir", "data"));
        try (DataDirectoryLock lock = new DataDirectoryLock(directory);
            Scanner scanner = new Scanner(System.in)) {
            SiteManagerController controller = new SiteManagerController(directory);
            controller.loadAllData();
            AuthenticationService authentication = controller.getAuthentication();
            AccountController accounts = new AccountController(authentication);
            ConsoleTheme.success("CSV data loaded.");
            boolean exit = false;
            while (!exit) {
                try {
                    if (!authentication.isLoggedIn() && !accounts.signIn(scanner)) {
                        break;
                    }
                    MenuView.showMainMenu(authentication);
                    int choice = InputHelper.getInt(scanner, "Choice (0-8): ", 0, 8);
                    switch (choice) {
                        case 1:
                            controller.handlePersonMenu(scanner);
                            break;
                        case 2:
                            controller.handleZoneMenu(scanner);
                            break;
                        case 3:
                            controller.handleAttendanceMenu(scanner);
                            break;
                        case 4:
                            controller.handleIncidentMenu(scanner);
                            break;
                        case 5:
                            authentication.requireAny(Permission.RELOAD_DATA, Permission.EXPORT_REPORTS);
                            MenuView.showDataMenu(authentication);
                            int action = InputHelper.getInt(scanner, "Choice (0-3): ", 0, 3);
                            if (action == 1) {
                                authentication.require(Permission.RELOAD_DATA);
                                String answer = InputHelper.getString(
                                        scanner,
                                        "Discard unsaved changes and reload CSV files? (yes/no): "
                                );
                                if (answer.equalsIgnoreCase("yes")) {
                                    controller.reloadData();
                                    ConsoleTheme.success("CSV data reloaded.");
                                }
                            } else if (action == 2) {
                                authentication.require(Permission.RELOAD_DATA);
                                controller.saveAllData();
                                ConsoleTheme.success("CSV data saved.");
                            } else if (action == 3) {
                                authentication.require(Permission.EXPORT_REPORTS);
                                String path = InputHelper.getString(scanner, "Export parent directory: ");
                                ConsoleTheme.success("Export completed: " + controller.exportCsv(Paths.get(path)));
                            }
                            break;
                        case 6:
                            accounts.handleMenu(scanner);
                            break;
                        case 7:
                            accounts.changeOwnPassword(scanner);
                            break;
                        case 8:
                            controller.saveAllData();
                            authentication.logout();
                            ConsoleTheme.success("Signed out.");
                            break;
                        case 0:
                            controller.saveAllData();
                            ConsoleTheme.success("CSV data saved. Goodbye.");
                            exit = true;
                            break;
                    }
                } catch (NoSuchElementException e) {
                    if (authentication.isLoggedIn()) {
                        controller.saveAllData();
                        ConsoleTheme.success("Input closed. CSV data saved.");
                    } else {
                        ConsoleTheme.success("Input closed before sign-in.");
                    }
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
