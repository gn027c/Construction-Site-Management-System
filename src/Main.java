import controller.SiteManagerController;
import exception.DataValidationException;
import exception.SiteManagementException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import model.Account;
import model.Permission;
import service.AuthenticationService;
import util.InputHelper;
import view.ConsoleUi;
import view.MenuView;

public class Main {
    private static final int SIGN_IN_ATTEMPTS = 3;

    public static void main(String[] args) {
        SiteManagerController site = new SiteManagerController();
        Scanner scanner = new Scanner(System.in);

        ConsoleUi.banner("WELCOME TO SMARTSITE MANAGEMENT SYSTEM", null);

        String loadProblem = load(site);
        if (!site.isAccountsReady()) {
            System.err.println("Sign-in is unavailable: " + loadProblem);
            System.err.println("Fix accounts.csv and start again.");
            scanner.close();
            return;
        }

        try {
            boolean running = true;
            while (running && signIn(site.authentication(), scanner)) {
                if (site.authentication().currentAccount().mustChangePassword()) {
                    forcePasswordChange(site.authentication(), scanner);
                }
                if (!site.isDataReady()) {
                    System.err.println("[WARN] Data is not loaded: " + site.loadProblem());
                }
                running = homeMenu(site, scanner);
            }
            System.out.println("Thank you for using SmartSite System! Goodbye.");
        } catch (NoSuchElementException e) {
            System.err.println("Input ended. Unsaved changes were not written.");
        }
        scanner.close();
    }

    private static String load(SiteManagerController site) {
        try {
            site.loadAllData();
            System.out.println("[OK] [SYSTEM] All CSV data loaded successfully!");
            return null;
        } catch (SiteManagementException e) {
            return e.getMessage();
        }
    }

    private static boolean signIn(AuthenticationService authentication, Scanner scanner) {
        for (int attempt = 1; attempt <= SIGN_IN_ATTEMPTS; attempt++) {
            String username = InputHelper.getString(scanner, "Username (0 to exit): ");
            if (username.equals("0")) {
                return false;
            }
            String password = InputHelper.getString(scanner, "Password: ");
            try {
                Account account = authentication.login(username, password);
                System.out.println("Signed in as " + account.getUsername() + " (" + account.getRole() + ").");
                return true;
            } catch (SiteManagementException e) {
                System.err.println("[ERROR] " + e.getMessage());
            }
        }
        System.err.println("Too many failed attempts.");
        return false;
    }

    private static void forcePasswordChange(AuthenticationService authentication, Scanner scanner) {
        System.out.println("You must change your password before continuing.");
        while (authentication.currentAccount().mustChangePassword()) {
            changePassword(authentication, scanner);
        }
    }

    private static void changePassword(AuthenticationService authentication, Scanner scanner) {
        String currentPassword = InputHelper.getString(scanner, "Current password: ");
        String newPassword = InputHelper.getString(scanner, "New password: ");
        try {
            authentication.changeOwnPassword(currentPassword, newPassword);
            System.out.println("[OK] Password changed.");
        } catch (SiteManagementException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }

    private static boolean homeMenu(SiteManagerController site, Scanner scanner) {
        while (true) {
            try {
                MenuView.showMainMenu(site.authentication().currentAccount());
                int choice = InputHelper.getInt(scanner, "Enter your choice (0-9): ", 0, 9);
                switch (choice) {
                    case 1:
                        site.handlePersonMenu(scanner);
                        break;
                    case 2:
                        site.handleZoneMenu(scanner);
                        break;
                    case 3:
                        site.handleAttendanceMenu(scanner);
                        break;
                    case 4:
                        site.handleIncidentMenu(scanner);
                        break;
                    case 5:
                        handleDataManagementMenu(site, scanner);
                        break;
                    case 6:
                        site.handleAccountMenu(scanner);
                        break;
                    case 7:
                        changePassword(site.authentication(), scanner);
                        break;
                    case 8:
                        saveIfReady(site);
                        site.authentication().logout();
                        return true;
                    case 9:
                        site.handleReportMenu(scanner);
                        break;
                    default:
                        saveIfReady(site);
                        site.authentication().logout();
                        return false;
                }
            } catch (DataValidationException e) {
                System.err.println("[WARN] [DATA VALIDATION ERROR] " + e.getMessage());
            } catch (SiteManagementException e) {
                System.err.println("[ERROR] [SITE MANAGEMENT ERROR] " + e.getMessage());
            }
            System.out.println();
        }
    }

    private static void saveIfReady(SiteManagerController site) {
        if (site.isDataReady()) {
            site.saveAllData();
            System.out.println("\n[OK] Data saved successfully!");
        } else {
            System.out.println("\nData files were left unchanged because they could not be loaded.");
        }
    }

    private static void handleDataManagementMenu(SiteManagerController site, Scanner scanner) {
        boolean back = false;
        while (!back) {
            try {
                MenuView.showDataMenu();
                int choice = InputHelper.getInt(scanner, "Enter your choice (0-2): ", 0, 2);
                switch (choice) {
                    case 1:
                        site.authentication().require(Permission.RELOAD_DATA);
                        site.loadAllData();
                        System.out.println("[OK] Data reloaded successfully from CSV files.");
                        break;
                    case 2:
                        site.authentication().require(Permission.RELOAD_DATA);
                        site.saveAllData();
                        System.out.println("[OK] Data saved manually to CSV files successfully.");
                        break;
                    default:
                        back = true;
                        break;
                }
            } catch (SiteManagementException e) {
                System.err.println("[WARN] [DATA MANAGEMENT ERROR] " + e.getMessage());
            }
            System.out.println();
        }
    }
}
