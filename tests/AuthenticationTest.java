import static support.Checks.*;

import controller.SiteManagerController;
import exception.AccessDeniedException;
import exception.AuthenticationException;
import exception.DataValidationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.Set;
import model.Account;
import model.Incident;
import model.Permission;
import repository.IncidentRepository;
import service.AuthenticationService;
import util.PasswordHelper;

public class AuthenticationTest {
    private static Path root() throws IOException {
        return Files.createTempDirectory("smartsite-auth-");
    }

    private static AuthenticationService admin(Path root) throws IOException {
        AuthenticationService authentication = new AuthenticationService(root);
        authentication.login("admin", "admin");
        return authentication;
    }

    private static void fixture(Path root) throws IOException {
        for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv")) {
            Files.copy(Paths.get("data", file), root.resolve(file));
        }
        Files.write(root.resolve("attendance.csv"), Arrays.asList("recordId,personCode,zoneId,timestamp,checkType"));
    }

    public static void main(String[] args) throws Exception {
        test("First start creates a hashed admin account with all permissions", () -> {
            Path root = root();
            AuthenticationService authentication = admin(root);
            Account account = authentication.currentAccount();
            equal(Account.Role.ADMIN, account.getRole());
            equal(EnumSet.allOf(Permission.class), account.getPermissions());
            require(PasswordHelper.isValidHash(account.getPasswordHash()));
            require(!account.toString().contains(account.getPasswordHash()));
            String csv = new String(Files.readAllBytes(root.resolve("accounts.csv")), StandardCharsets.UTF_8);
            require(!csv.contains(",admin,"));
            equal(1, authentication.listAccounts().size());
        });
        test("Passwords use unique salts and preserve whitespace and Unicode", () -> {
            String first = PasswordHelper.hash("  Pa\u00dfword  ");
            String second = PasswordHelper.hash("  Pa\u00dfword  ");
            require(!first.equals(second));
            require(PasswordHelper.verify("  Pa\u00dfword  ", first));
            require(!PasswordHelper.verify("Pa\u00dfword", first));
            require(!PasswordHelper.verify("anything", "PBKDF2-SHA256$999999999$bad$bad"));
        });
        test("Failed login and logout revoke the previous authentication", () -> {
            AuthenticationService authentication = admin(root());
            throwsType(AuthenticationException.class, () -> authentication.login("admin", "wrong"));
            require(!authentication.isLoggedIn());
            throwsType(AccessDeniedException.class, authentication::listAccounts);
            throwsType(AuthenticationException.class, () -> authentication.login("unknown", "admin"));
            authentication.login(" ADMIN ", "admin");
            authentication.logout();
            require(!authentication.isLoggedIn());
        });
        test("Account administration requires an authenticated administrator", () -> {
            AuthenticationService authentication = new AuthenticationService(root());
            throwsType(AccessDeniedException.class, () -> authentication.createAccount(
                    "officer", "secret", Account.Role.SAFETY_OFFICER
            ));
            throwsType(AccessDeniedException.class, () -> authentication.changeRole("admin", Account.Role.SITE_MANAGER));
        });
        test("Both staff roles receive distinct default permissions", () -> {
            AuthenticationService authentication = admin(root());
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            authentication.createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            authentication.login("officer", "secret");
            require(authentication.allows(Permission.REPORT_INCIDENTS));
            require(authentication.allows(Permission.ASSIGN_INCIDENTS));
            require(!authentication.allows(Permission.MANAGE_PERSONNEL));
            require(!authentication.allows(Permission.PURGE_INCIDENTS));
            authentication.login("manager", "secret");
            require(authentication.allows(Permission.MANAGE_PERSONNEL));
            require(authentication.allows(Permission.MANAGE_ZONES));
            require(authentication.allows(Permission.EXPORT_REPORTS));
            require(!authentication.allows(Permission.REPORT_INCIDENTS));
        });
        test("Staff cannot promote themselves, edit permissions or reset other accounts", () -> {
            AuthenticationService authentication = admin(root());
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            authentication.login("officer", "secret");
            throwsType(AccessDeniedException.class, () -> authentication.changeRole("officer", Account.Role.ADMIN));
            throwsType(AccessDeniedException.class, () -> authentication.changePermissions(
                    "officer", EnumSet.allOf(Permission.class)
            ));
            throwsType(AccessDeniedException.class, () -> authentication.resetPassword("admin", "changed"));
            throwsType(AccessDeniedException.class, () -> authentication.setEnabled("admin", false));
            throwsType(AccessDeniedException.class, authentication::listAccounts);
        });
        test("Custom permissions persist and role reassignment restores role defaults", () -> {
            Path root = root();
            AuthenticationService authentication = admin(root);
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            authentication.changePermissions("officer", EnumSet.of(Permission.VIEW_ATTENDANCE));
            AuthenticationService restarted = new AuthenticationService(root);
            restarted.login("officer", "secret");
            require(restarted.allows(Permission.VIEW_ATTENDANCE));
            require(!restarted.allows(Permission.REPORT_INCIDENTS));
            authentication.changeRole("officer", Account.Role.SITE_MANAGER);
            restarted = new AuthenticationService(root);
            restarted.login("officer", "secret");
            equal(Account.Role.SITE_MANAGER.defaultPermissions(), restarted.currentAccount().getPermissions());
        });
        test("Account management cannot be granted to a non-admin role", () -> {
            AuthenticationService authentication = admin(root());
            authentication.createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            throwsType(DataValidationException.class, () -> authentication.changePermissions(
                    "manager", EnumSet.of(Permission.MANAGE_ACCOUNTS)
            ));
            throwsType(DataValidationException.class, () -> authentication.changePermissions(
                    "admin", EnumSet.of(Permission.VIEW_ATTENDANCE)
            ));
        });
        test("The last enabled admin cannot be demoted and failed changes preserve CSV", () -> {
            Path root = root();
            AuthenticationService authentication = admin(root);
            byte[] before = Files.readAllBytes(root.resolve("accounts.csv"));
            throwsType(IOException.class, () -> authentication.changeRole("admin", Account.Role.SITE_MANAGER));
            equal(Account.Role.ADMIN, authentication.currentAccount().getRole());
            require(Arrays.equals(before, Files.readAllBytes(root.resolve("accounts.csv"))));
            throwsType(DataValidationException.class, () -> authentication.setEnabled("admin", false));
        });
        test("An administrator can be demoted once a second enabled admin exists", () -> {
            AuthenticationService authentication = admin(root());
            authentication.createAccount("second-admin", "secret", Account.Role.ADMIN);
            authentication.changeRole("admin", Account.Role.SITE_MANAGER);
            require(!authentication.allows(Permission.MANAGE_ACCOUNTS));
            throwsType(AccessDeniedException.class, authentication::listAccounts);
            authentication.login("second-admin", "secret");
            equal(2, authentication.listAccounts().size());
        });
        test("Disabled accounts cannot sign in and can be re-enabled by admin", () -> {
            Path root = root();
            AuthenticationService authentication = admin(root);
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            authentication.setEnabled("officer", false);
            AuthenticationService restarted = new AuthenticationService(root);
            throwsType(AuthenticationException.class, () -> restarted.login("officer", "secret"));
            authentication.setEnabled("officer", true);
            new AuthenticationService(root).login("officer", "secret");
        });
        test("Password changes require the current password and survive restart", () -> {
            Path root = root();
            AuthenticationService authentication = admin(root);
            throwsType(AuthenticationException.class, () -> authentication.changePassword("wrong", "changed"));
            authentication.changePassword("admin", "new-secret");
            AuthenticationService restarted = new AuthenticationService(root);
            throwsType(AuthenticationException.class, () -> restarted.login("admin", "admin"));
            restarted.login("admin", "new-secret");
            equal(1, restarted.listAccounts().size());
        });
        test("Admin password resets persist without changing account permissions", () -> {
            Path root = root();
            AuthenticationService authentication = admin(root);
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            authentication.resetPassword("officer", "replacement");
            AuthenticationService restarted = new AuthenticationService(root);
            throwsType(AuthenticationException.class, () -> restarted.login("officer", "secret"));
            restarted.login("officer", "replacement");
            equal(Account.Role.SAFETY_OFFICER.defaultPermissions(), restarted.currentAccount().getPermissions());
        });
        test("Duplicate usernames are rejected case-insensitively without changing storage", () -> {
            Path root = root();
            AuthenticationService authentication = admin(root);
            byte[] before = Files.readAllBytes(root.resolve("accounts.csv"));
            throwsType(DataValidationException.class, () -> authentication.createAccount(
                    " ADMIN ", "secret", Account.Role.SITE_MANAGER
            ));
            require(Arrays.equals(before, Files.readAllBytes(root.resolve("accounts.csv"))));
        });
        test("Corrupted accounts are rejected without restoring admin/admin", () -> {
            Path root = root();
            Files.write(root.resolve("accounts.csv"), Arrays.asList("invalid"), StandardCharsets.UTF_8);
            byte[] before = Files.readAllBytes(root.resolve("accounts.csv"));
            throwsType(IOException.class, () -> new AuthenticationService(root));
            require(Arrays.equals(before, Files.readAllBytes(root.resolve("accounts.csv"))));
        });
        test("An account file without an enabled administrator is rejected", () -> {
            Path root = root();
            admin(root);
            Path file = root.resolve("accounts.csv");
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            Files.write(file, text.replace(",ADMIN,true,", ",ADMIN,false,").getBytes(StandardCharsets.UTF_8));
            throwsType(IOException.class, () -> new AuthenticationService(root));
        });
        test("Unknown CSV permissions are rejected rather than silently granted", () -> {
            Path root = root();
            admin(root);
            Path file = root.resolve("accounts.csv");
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            Files.write(file, text.replace("MANAGE_PERSONNEL", "UNKNOWN_PERMISSION").getBytes(StandardCharsets.UTF_8));
            throwsType(IOException.class, () -> new AuthenticationService(root));
        });
        test("A failed account write retains the previous in-memory credentials", () -> {
            Path root = root();
            AuthenticationService authentication = admin(root);
            Path file = root.resolve("accounts.csv");
            Files.move(file, root.resolve("saved-accounts.csv"));
            Files.createDirectory(file);
            try {
                throwsType(IOException.class, () -> authentication.createAccount(
                        "officer", "secret", Account.Role.SAFETY_OFFICER
                ));
                equal(1, authentication.listAccounts().size());
                authentication.login("admin", "admin");
            } finally {
                Files.delete(file);
                Files.move(root.resolve("saved-accounts.csv"), file);
            }
        });
        test("Permission collections cannot mutate an account through an external reference", () -> {
            AuthenticationService authentication = admin(root());
            Set<Permission> permissions = authentication.currentAccount().getPermissions();
            permissions.clear();
            require(authentication.allows(Permission.MANAGE_ACCOUNTS));
        });
        test("Protected facade operations reject an unauthenticated caller", () -> {
            Path root = root();
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            throwsType(AccessDeniedException.class, () -> controller.handlePersonMenu(new Scanner("0\n")));
            throwsType(AccessDeniedException.class, () -> controller.handleZoneMenu(new Scanner("0\n")));
            throwsType(AccessDeniedException.class, () -> controller.handleAttendanceMenu(new Scanner("0\n")));
            throwsType(AccessDeniedException.class, () -> controller.handleIncidentMenu(new Scanner("0\n")));
            throwsType(AccessDeniedException.class, () -> controller.exportCsv(root.resolve("exports")));
            throwsType(AccessDeniedException.class, controller::reloadData);
            require(!Files.exists(root.resolve("exports")));
        });
        test("Safety officers can report, assign and resolve incidents through the application", () -> {
            Path root = root();
            fixture(root);
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            AuthenticationService authentication = controller.getAuthentication();
            authentication.login("admin", "admin");
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            authentication.login("officer", "secret");
            controller.handleIncidentMenu(new Scanner(
                    "1\nINC201\nSafety hazard\nLoose handrail\n3\n3\nINC201\nW01\n4\nINC201\n0\n"
            ));
            IncidentRepository incidents = new IncidentRepository();
            require(incidents.loadFromCsv(root.resolve("incidents.csv").toString()));
            equal(Incident.IncidentStatus.RESOLVED, incidents.findById("INC201").getStatus());
            throwsType(AccessDeniedException.class, () -> controller.handlePersonMenu(new Scanner("0\n")));
            throwsType(AccessDeniedException.class, () -> controller.handleIncidentMenu(new Scanner("5\n")));
        });
        test("Manager read permissions cannot be bypassed by entering a hidden modifying option", () -> {
            Path root = root();
            fixture(root);
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            AuthenticationService authentication = controller.getAuthentication();
            authentication.login("admin", "admin");
            authentication.createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            authentication.login("manager", "secret");
            byte[] before = Files.readAllBytes(root.resolve("incidents.csv"));
            controller.handleIncidentMenu(new Scanner("2\n0\n0\n"));
            throwsType(AccessDeniedException.class, () -> controller.handleIncidentMenu(new Scanner("1\n")));
            throwsType(AccessDeniedException.class, () -> controller.handleAttendanceMenu(new Scanner("1\n")));
            require(Arrays.equals(before, Files.readAllBytes(root.resolve("incidents.csv"))));
        });
        test("Business exports exclude account records and password hashes", () -> {
            Path root = root();
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            controller.getAuthentication().login("admin", "admin");
            Path exported = controller.exportCsv(root.resolve("exports"));
            require(!Files.exists(exported.resolve("accounts.csv")));
            for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv")) {
                String text = new String(Files.readAllBytes(exported.resolve(file)), StandardCharsets.UTF_8);
                require(!text.contains("PBKDF2"));
            }
        });
        test("Manager can update personnel, create a zone and export business reports", () -> {
            Path root = root();
            fixture(root);
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            AuthenticationService authentication = controller.getAuthentication();
            authentication.login("admin", "admin");
            authentication.createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            authentication.login("manager", "secret");
            controller.handlePersonMenu(new Scanner("3\nW01\nUpdated Worker\n\n0\n"));
            controller.handleZoneMenu(new Scanner("1\n1\nZ100\nStaff zone\n0\n"));
            service.PersonnelService personnel = new service.PersonnelService();
            service.ZoneService zones = new service.ZoneService();
            require(personnel.loadFromCsv(root.resolve("persons.csv").toString()));
            require(zones.loadFromCsv(root.resolve("zones.csv").toString()));
            equal("Updated Worker", personnel.findPersonByCode("W01").getName());
            equal("Staff zone", zones.findZoneById("Z100").getZoneName());
            require(Files.isDirectory(controller.exportCsv(root.resolve("exports"))));
        });
        test("Revoking all staff permissions takes effect on login and persists", () -> {
            Path root = root();
            AuthenticationService authentication = admin(root);
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            authentication.changePermissions("officer", EnumSet.noneOf(Permission.class));
            AuthenticationService restarted = new AuthenticationService(root);
            restarted.login("officer", "secret");
            require(restarted.isLoggedIn());
            for (Permission permission : Permission.values()) {
                require(!restarted.allows(permission));
            }
        });
        finish();
    }
}
