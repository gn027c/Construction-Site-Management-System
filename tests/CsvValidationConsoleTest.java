import static support.Checks.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import model.Account;
import service.AuthenticationService;

public class CsvValidationConsoleTest {
    private static Path invalidFixture() throws Exception {
        Path root = Files.createTempDirectory("smartsite-read-only-validation-");
        for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv")) {
            Files.copy(Paths.get("data", file), root.resolve(file));
        }
        return root;
    }

    public static void main(String[] args) throws Exception {
        test("Admin sees validation details without a CSV editor and cannot change rejected source", () -> {
            Path root = invalidFixture();
            byte[] original = Files.readAllBytes(root.resolve("attendance.csv"));
            String output = ConsoleTest.launch(root, "11\n0\n0\n", 0, false);
            require(output.contains("CSV validation (read only)"));
            require(output.contains("CSV VALIDATION ERRORS / READ ONLY"));
            require(output.contains("ATT6"));
            require(!output.contains("Correct attendance with evidence"));
            require(!output.contains("ORIGINAL ATTENDANCE RECORD"));
            require(!output.contains("Verified zone ID"));
            require(Arrays.equals(original, Files.readAllBytes(root.resolve("attendance.csv"))));
        });
        test("Healthy data validation returns directly without repair steps", () -> {
            Path root = Files.createTempDirectory("smartsite-valid-ledger-");
            String output = ConsoleTest.launch(root, "11\n0\n", 0, false);
            require(output.contains("No issues; business data is active."));
            require(!output.contains("REVIEW STEPS"));
            require(!output.contains("Correction reason"));
        });
        test("A non-admin cannot obtain CSV validation details by entering a hidden menu number", () -> {
            Path root = invalidFixture();
            AuthenticationService auth = new AuthenticationService(root);
            auth.login("admin", "admin");
            auth.createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            String output = ConsoleTest.launchRaw(root, "manager\nsecret\n11\n0\n", 0, false);
            require(!output.contains("CSV VALIDATION ERRORS"));
            require(!output.contains("Invalid attendance.csv"));
            require(!output.contains("ATT6"));
            require(!output.contains("[11] CSV validation"));
        });
        test("EOF in read-only validation exits without rewriting rejected history", () -> {
            Path root = invalidFixture();
            byte[] original = Files.readAllBytes(root.resolve("attendance.csv"));
            String output = ConsoleTest.launch(root, "11\n", 0, false);
            require(output.contains("Session closed."));
            require(Arrays.equals(original, Files.readAllBytes(root.resolve("attendance.csv"))));
        });
        finish();
    }
}
