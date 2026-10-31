import controller.SiteManagerController;
import controller.AccountController;
import controller.WorkspaceController;
import model.auth.Permission;
import model.auth.Account;
import service.AuthenticationService;
import util.InputHelper;
import view.ConsoleTheme;
import view.ConsoleScreen;
import view.MenuView;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Path directory = Paths.get(System.getProperty("smartsite.dataDir", "data"));
        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleScreen.attach(scanner);
            SiteManagerController controller = new SiteManagerController(directory);
            String startupFailure = "";
            try {
                controller.loadAllData();
            } catch (java.io.IOException e) {
                startupFailure = e.getMessage();
            }
            AuthenticationService authentication = controller.getAuthentication();
            AccountController accounts = new AccountController(authentication);
            WorkspaceController workspace = new WorkspaceController(controller);
            boolean exit = false;
            while (!exit) {
                try {
                    if (!authentication.isLoggedIn()) {
                        if (!accounts.signIn(scanner)) {
                            break;
                        }
                        if (!controller.isDataReady()) {
                            if (isAdministrator(authentication)) {
                                ConsoleTheme.error(startupFailure);
                                ConsoleTheme.warning("Business actions are blocked. Correct the CSV file, then reload it with menu 5.");
                            } else {
                                ConsoleTheme.warning("Service temporarily unavailable. Contact an administrator.");
                            }
                        }
                    }
                    MenuView.showMainMenu(authentication);
                    int choice = InputHelper.getMenuChoice(scanner, "Choice (0-10): ", 10);
                    ConsoleScreen.begin();
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
                            int action = InputHelper.getMenuChoice(scanner, "Choice (0-3): ", 3);
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
                            workspace.attendanceReport(scanner);
                            break;
                        case 7:
                            workspace.incidentReport(scanner);
                            break;
                        case 8:
                            accounts.handleMenu(scanner);
                            break;
                        case 9:
                            accounts.changeOwnPassword(scanner);
                            break;
                        case 10:
                            if (controller.isDataReady()) {
                                controller.saveAllData();
                            }
                            authentication.logout();
                            ConsoleScreen.identityBoundary();
                            ConsoleTheme.success("Signed out.");
                            break;
                        case 0:
                            if (controller.isDataReady()) {
                                controller.saveAllData();
                                ConsoleTheme.success("CSV data saved. Goodbye.");
                            } else {
                                ConsoleTheme.success("Goodbye.");
                            }
                            exit = true;
                            break;
                    }
                } catch (exception.InputCancelledException e) {
                    ConsoleTheme.warning("Current entry cancelled. Returned to the main menu.");
                } catch (NoSuchElementException e) {
                    if (authentication.isLoggedIn() && controller.isDataReady()) {
                        controller.saveAllData();
                        ConsoleTheme.success("Input closed. CSV data saved.");
                    } else if (!authentication.isLoggedIn()) {
                        ConsoleTheme.success("Input closed before sign-in.");
                    } else {
                        ConsoleTheme.success("Session closed.");
                    }
                    exit = true;
                } catch (Exception e) {
                    if (!isAdministrator(authentication) && (!controller.isDataReady()
                            || e instanceof java.io.IOException
                            || e.getClass() == exception.SiteManagementException.class)) {
                        ConsoleTheme.error("Operation unavailable. Contact an administrator.");
                    } else {
                        ConsoleTheme.error(e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            ConsoleTheme.error("Session unavailable. Contact an administrator.");
            System.exit(1);
        }
    }

    private static boolean isAdministrator(AuthenticationService authentication) {
        return authentication.isLoggedIn() && authentication.currentAccount().getRole() == Account.Role.ADMIN;
    }
}
