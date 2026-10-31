import static support.Checks.*;

import controller.SiteManagerController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import model.person.Person;
import model.person.Staff;
import service.AuthenticationService;
import util.InputHelper;

public class StaffBusinessValidationTest {
    private static Path root() throws IOException {
        Path root = Files.createTempDirectory("smartsite-staff-val-");
        for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv")) {
            Files.copy(Paths.get("data", file), root.resolve(file));
        }
        return root;
    }

    public static void main(String[] args) throws Exception {
        test("InputHelper capitalizes person names into Title Case", () -> {
            equal("Nguyen Van An", InputHelper.capitalizeName("nguyen van an"));
            equal("Tran Thi Mai", InputHelper.capitalizeName("TRAN THI MAI"));
            equal("Le Van Cuong", InputHelper.capitalizeName("  le   van   cuong  "));
            equal("", InputHelper.capitalizeName("   "));
        });

        test("InputHelper getPersonName rejects numbers and formats valid names", () -> {
            // "1\n  \nnguyen van phuc\n" -> 1 rejected, blank rejected, "nguyen van phuc" accepted as "Nguyen Van Phuc"
            Scanner scanner = new Scanner("1\n  \nnguyen van phuc\n");
            String name = InputHelper.getPersonName(scanner, "Name: ");
            equal("Nguyen Van Phuc", name);
        });

        test("InputHelper getPhoneNumber rejects invalid numbers and accepts 10 digits starting with 0", () -> {
            // "1\n1234567890\n09012345678\n0987654321\n"
            Scanner scanner = new Scanner("1\n1234567890\n09012345678\n0987654321\n");
            String phone = InputHelper.getPhoneNumber(scanner, "Phone: ");
            equal("0987654321", phone);
        });

        test("Staff creation with menu choices for position and auto-generated safe certificate", () -> {
            Path root = root();
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            AuthenticationService auth = controller.getAuthentication();
            auth.login("admin", "admin");

            // Input sequence:
            // 1: Add person
            // 4: Staff
            // (ID and Code are auto-generated)
            // tran van sang: Name (will be auto-capitalized to Tran Van Sang)
            // 0987654321: Phone
            // 2: Position (Site Engineer)
            // 1: Safety Certification (CERT_S1_SAFE)
            // 3: Safety level HIGH
            // 0: Exit personnel menu
            String input = "1\n4\ntran van sang\n0987654321\n2\n1\n3\n0\n";
            controller.handlePersonMenu(new Scanner(input));

            Person created = controller.person("S1");
            require(created instanceof Staff);
            Staff staff = (Staff) created;
            require(staff.getId().matches("^P\\d{5}$"));
            equal("S1", staff.getCode());
            equal("Tran Van Sang", staff.getName());
            equal("0987654321", staff.getPhoneNumber());
            equal("Site Engineer", staff.getPosition());
            equal(model.common.SafetyLevel.HIGH, staff.getSafetyLevel());
            equal("CERT_S1_SAFE", staff.getSafetyCertId());
            require(staff.hasValidSafetyCredential());
        });

        test("Staff creation with uncertified option sets UNSAFE certificate", () -> {
            Path root = root();
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            AuthenticationService auth = controller.getAuthentication();
            auth.login("admin", "admin");

            // Input sequence:
            // 1: Add person
            // 4: Staff
            // (ID and Code are auto-generated)
            // le thi dao: Name -> Le Thi Dao
            // 0912345678: Phone
            // 3: Position (Safety Officer)
            // 2: Safety Certification (CERT_UNSAFE)
            // 0: Exit
            String input = "1\n4\nle thi dao\n0912345678\n3\n2\n0\n";
            controller.handlePersonMenu(new Scanner(input));

            Person created = controller.person("S1");
            require(created instanceof Staff);
            Staff staff = (Staff) created;
            require(staff.getId().matches("^P\\d{5}$"));
            equal("Safety Officer", staff.getPosition());
            equal("CERT_UNSAFE", staff.getSafetyCertId());
            require(!staff.hasValidSafetyCredential());
        });

        test("Auto-generated ID and Code guarantee role prefix and unique numbers", () -> {
            Path root = root();
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            AuthenticationService auth = controller.getAuthentication();
            auth.login("admin", "admin");

            String input = "1\n1\nNguyen Van K\n0987654321\n1\n1\nTeam 1\n\n0\n";
            controller.handlePersonMenu(new Scanner(input));

            int maxW = 0;
            for (Person p : controller.people()) {
                if (p.getCode().startsWith("W")) {
                    try {
                        int num = Integer.parseInt(p.getCode().substring(1));
                        maxW = Math.max(maxW, num);
                    } catch (NumberFormatException ignored) {}
                }
            }
            Person created = controller.person("W" + maxW);
            require(created != null);
            require(created.getId().matches("^P\\d{5}$"));
            equal("Nguyen Van K", created.getName());
        });

        test("Person ID is randomly generated without relying on index or sequential order", () -> {
            Path root = root();
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            controller.getAuthentication().login("admin", "admin");

            String id1 = controller.suggestNextPersonId();
            String id2 = controller.suggestNextPersonId();
            require(id1.matches("^P\\d{5}$"));
            require(id2.matches("^P\\d{5}$"));
            // Random IDs must not equal sequential index "P11"
            require(!id1.equals("P11"));
            require(!id2.equals("P11"));
            // Existing personnel must not already have this ID
            for (Person p : controller.people()) {
                require(!p.getId().equalsIgnoreCase(id1));
                require(!p.getId().equalsIgnoreCase(id2));
            }
        });

        test("shouldProvisionAccount returns true for staff, worker, contractor and false for visitor", () -> {
            Person worker = new model.person.Worker("P10001", "W99", "Worker A", "0901234567", "Welder", "CERT_SAFE", "Team A");
            Person contractor = new model.person.Contractor("P10002", "C99", "Contractor B", "0901234568", "Company X", "CTR-01");
            Person staff = new model.person.Staff("P10003", "S99", "Staff C", "0901234569", "Safety Officer", "CERT_SAFE");
            Person visitor = new model.person.Visitor("P10004", "V99", "Visitor D", "0901234570", "Audit", "Host X", "01/01/2026");

            require(worker.shouldProvisionAccount());
            require(contractor.shouldProvisionAccount());
            require(staff.shouldProvisionAccount());
            require(!visitor.shouldProvisionAccount());
        });

        test("Account mustChangePassword defaults to true on creation and resets on password change", () -> {
            model.auth.Account acc = new model.auth.Account(
                    "user_test",
                    util.PasswordHelper.validate("initialPass"),
                    model.auth.Account.Role.SITE_USER,
                    true,
                    model.auth.Account.Role.SITE_USER.defaultPermissions(),
                    "W99"
            );
            require(acc.isMustChangePassword());
            require(acc.mustChangePassword());

            // When password is changed by user, mustChangePassword becomes false
            model.auth.Account updated = acc.withPassword("newSecretPassword123");
            require(!updated.isMustChangePassword());

            // When admin resets password, mustChangePassword becomes true
            model.auth.Account reset = updated.withResetPassword("tempResetPass123");
            require(reset.isMustChangePassword());
        });

        test("Username generation formats name abbreviation and code, e.g. Nguyen Tan Loi -> LoiNTW1", () -> {
            equal("LoiNTW1", SiteManagerController.buildUsernameBase("Nguyễn Tấn Lợi", "W1"));
            equal("TuanTNAW2", SiteManagerController.buildUsernameBase("Trần Ngọc Anh Tuấn", "W2"));
            equal("ThienLTW3", SiteManagerController.buildUsernameBase("Lê Tấn Thiên", "W3"));
            equal("KhangHNHS1", SiteManagerController.buildUsernameBase("Huỳnh Nguyễn Hoàng Khang", "S1"));
            equal("DoeJW2", SiteManagerController.buildUsernameBase("John Doe", "W2"));
            equal("LoiW1", SiteManagerController.buildUsernameBase("Lợi", "W1"));
        });

        test("SiteManagerController auto-provisions account with LoiNT format, default password 0 and mustChangePassword true", () -> {
            Path root = root();
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            controller.getAuthentication().login("admin", "admin");

            Person worker = new model.person.Worker("P99999", "W99", "Nguyễn Tấn Lợi", "0911223344", "Welder", "CERT_SAFE", "Team X");
            controller.addPerson(worker);
            require(controller.shouldProvisionAccount(worker));

            model.auth.Account provisioned = controller.provisionAccount(worker);
            require(provisioned != null);
            equal("LoiNTW99", provisioned.getUsername());
            require(provisioned.isMustChangePassword());
            equal("W99", provisioned.getPersonCode());
            require(util.PasswordHelper.matches("0", provisioned.getPassword()));

            // Verify authentication works with default password "0"
            controller.getAuthentication().logout();
            model.auth.Account loggedIn = controller.getAuthentication().login("LoiNTW99", "0");
            require(loggedIn != null);
            equal("LoiNTW99", loggedIn.getUsername());
            require(loggedIn.isMustChangePassword());

            // Case-insensitive login also succeeds
            controller.getAuthentication().logout();
            model.auth.Account lowerLoggedIn = controller.getAuthentication().login("lointw99", "0");
            require(lowerLoggedIn != null);
            equal("LoiNTW99", lowerLoggedIn.getUsername());
        });

        finish();
    }
}

