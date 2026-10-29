import static support.Checks.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import model.Account;
import service.AuthenticationService;

public class AuthenticationConsoleTest {
    private static Path root() throws IOException {
        return Files.createTempDirectory("smartsite-login-ui-");
    }

    private static void fixture(Path root) throws IOException {
        for (String file : Arrays.asList("persons.csv", "zones.csv", "incidents.csv")) {
            Files.copy(java.nio.file.Paths.get("data", file), root.resolve(file));
        }
        Files.write(root.resolve("attendance.csv"), Arrays.asList("recordId,personCode,zoneId,timestamp,checkType"));
    }

    public static void main(String[] args) throws Exception {
        test("Admin can create a safety account, sign out and sign in with the new account", () -> {
            Path root = root();
            fixture(root);
            String output = ConsoleTest.launchRaw(
                    root,
                    "admin\nadmin\n6\n2\nofficer\n3\nw01\nsecret\nsecret\n0\n8\nofficer\nsecret\n1\n6\n0\n",
                    0, false
            );
            require(output.contains("Account saved."));
            require(output.contains("Signed in as officer (SAFETY_OFFICER)."));
            String staffOutput = output.substring(output.indexOf("Signed in as officer"));
            require(!staffOutput.contains("[1] Personnel"));
            require(!staffOutput.contains("[6] Account administration"));
            require(staffOutput.contains("Access denied: MANAGE_PERSONNEL"));
            require(staffOutput.contains("Access denied: MANAGE_ACCOUNTS"));
            AuthenticationService restarted = new AuthenticationService(root);
            equal(Account.Role.SAFETY_OFFICER, restarted.login("officer", "secret").getRole());
        });
        test("Wrong passwords never display a protected menu or save business files", () -> {
            Path root = root();
            String output = ConsoleTest.launchRaw(
                    root, "admin\nwrong\nadmin\nwrong\nunknown\nwrong\n", 0, false
            );
            require(output.contains("Sign-in attempts exhausted"));
            require(!output.contains("[6] Account administration"));
            require(!Files.exists(root.resolve("persons.csv")));
        });
        test("EOF before sign-in exits cleanly without saving business data", () -> {
            Path root = root();
            String output = ConsoleTest.launchRaw(root, "", 0, false);
            require(output.contains("Input closed before sign-in."));
            require(!Files.exists(root.resolve("persons.csv")));
        });
        test("Changed admin credentials are used after logout and process restart", () -> {
            Path root = root();
            String output = ConsoleTest.launchRaw(
                    root, "admin\nadmin\n7\nadmin\nnew-secret\nnew-secret\n8\nadmin\nnew-secret\n0\n", 0, false
            );
            require(output.contains("Password changed."));
            require(output.contains("Signed out."));
            String restarted = ConsoleTest.launchRaw(root, "admin\nnew-secret\n0\n", 0, false);
            require(restarted.contains("Signed in as admin (ADMIN)."));
        });
        test("Manager cannot use data reload or account administration through hidden menu numbers", () -> {
            Path root = root();
            AuthenticationService authentication = new AuthenticationService(root);
            authentication.login("admin", "admin");
            authentication.createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            String output = ConsoleTest.launchRaw(root, "manager\nsecret\n5\n1\n6\n0\n", 0, false);
            require(output.contains("Access denied: RELOAD_DATA"));
            require(output.contains("Access denied: MANAGE_ACCOUNTS"));
            require(!output.contains("[1] Reload CSV files"));
        });
        test("Corrupted credentials stop startup without resetting the file", () -> {
            Path root = root();
            Path file = root.resolve("accounts.csv");
            Files.write(file, Arrays.asList("bad"), StandardCharsets.UTF_8);
            byte[] before = Files.readAllBytes(file);
            String output = ConsoleTest.launchRaw(root, "admin\nadmin\n0\n", 1, false);
            require(output.contains("credentials were not reset"));
            require(Arrays.equals(before, Files.readAllBytes(file)));
        });
        test("Account lists show account details without password hashes", () -> {
            String output = ConsoleTest.launchRaw(root(), "admin\nadmin\n6\n1\n0\n0\n0\n", 0, false);
            require(output.contains("admin | ADMIN | Enabled"));
            require(!output.contains("PBKDF2"));
        });
        test("Admin can replace permissions and disable an account from the console", () -> {
            Path root = root();
            AuthenticationService authentication = new AuthenticationService(root);
            authentication.login("admin", "admin");
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            String output = ConsoleTest.launchRaw(
                    root,
                    "admin\nadmin\n6\n4\nofficer\n4\n5\nofficer\n0\n0\n8\nofficer\nsecret\n0\n",
                    0, false
            );
            require(output.contains("Account saved."));
            require(output.contains("Invalid username or password, or account disabled."));
            AuthenticationService restarted = new AuthenticationService(root);
            restarted.login("admin", "admin");
            for (Account account : restarted.listAccounts()) {
                if (account.getUsername().equals("officer")) {
                    require(!account.isEnabled());
                    equal(java.util.EnumSet.of(model.Permission.VIEW_ATTENDANCE), account.getPermissions());
                }
            }
        });
        test("Mismatched password confirmation does not create an account", () -> {
            Path root = root();
            fixture(root);
            String output = ConsoleTest.launchRaw(
                    root, "admin\nadmin\n6\n2\nofficer\n3\nW1\nsecret\nwrong\n0\n0\n", 0, false
            );
            require(output.contains("Confirmation does not match."));
            AuthenticationService restarted = new AuthenticationService(root);
            restarted.login("admin", "admin");
            equal(1, restarted.listAccounts().size());
        });
        finish();
    }
}
