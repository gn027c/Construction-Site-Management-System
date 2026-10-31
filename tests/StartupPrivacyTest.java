import static support.Checks.*;

import exception.AccessDeniedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import model.auth.Account;
import service.AuthenticationService;

public class StartupPrivacyTest {
    private static Path invalidHistory() throws Exception {
        Path root = Files.createTempDirectory("smartsite-startup-privacy-");
        for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv")) {
            Files.copy(Paths.get("data", file), root.resolve(file));
        }
        util.CsvHelper.writeAllLines(root.resolve("attendance.csv").toString(), Arrays.asList(
                "recordId,personCode,zoneId,timestamp,checkType",
                "ATT1,W1,Z1,2026-10-12 07:30:00,CHECK_IN",
                "ATT6,W1,Z3,2026-10-12 08:15:00,CHECK_IN"
        ));
        return root;
    }

    private static void assertPrivate(String output) {
        for (String secret : Arrays.asList(
                "Invalid attendance history", "Invalid attendance.csv", "Business data rejected", "CSV issues", "ATT6", "ATT8",
                "previous="
        )) {
            require(!output.contains(secret));
        }
    }

    private static AuthenticationService createWorker(Path root) throws Exception {
        AuthenticationService auth = new AuthenticationService(root);
        service.PersonnelService people = new service.PersonnelService();
        require(people.loadFromCsv(Paths.get("data", "persons.csv").toString()));
        auth.bindPersonnel(people);
        auth.login("admin", "admin");
        auth.createAccount("worker", "secret", Account.Role.SITE_USER, "W2");
        return auth;
    }

    public static void main(String[] args) throws Exception {
        test("Anonymous startup reveals no business data diagnostics", () -> {
            String invalid = ConsoleTest.launchRaw(invalidHistory(), "0\n", 0, false);
            String empty = ConsoleTest.launchRaw(
                    Files.createTempDirectory("smartsite-private-empty-"), "0\n", 0, false
            );
            equal(empty, invalid);
            assertPrivate(invalid);
        });
        test("Failed login reveals no startup diagnostics", () -> {
            String output = ConsoleTest.launchRaw(invalidHistory(), "admin\nwrong\n0\n", 0, false);
            assertPrivate(output);
        });
        test("Admin receives diagnostics only after successful sign-in and can review records", () -> {
            Path root = invalidHistory();
            byte[] original = Files.readAllBytes(root.resolve("attendance.csv"));
            String output = ConsoleTest.launchRaw(root, "admin\nadmin\n6\n1\n0\n0\n0\n", 0, false);
            int signedIn = output.indexOf("Signed in as admin");
            require(signedIn >= 0);
            assertPrivate(output.substring(0, signedIn));
            require(output.indexOf("Invalid attendance.csv") > signedIn);
            require(output.contains("ATT6"));
            require(Arrays.equals(original, Files.readAllBytes(root.resolve("attendance.csv"))));
        });
        test("Personal account cannot reveal diagnostics through operations, inbox or hidden menus", () -> {
            Path root = invalidHistory();
            createWorker(root);
            String output = ConsoleTest.launchRaw(
                    root, "worker\nsecret\nchosen\nchosen\n3\n4\n6\n11\n12\n0\n\n0\n", 0, false
            );
            assertPrivate(output);
            require(output.contains("Service temporarily unavailable"));
            require(!output.contains("[6] CSV validation"));
        });
        test("Logout does not repeat administrator diagnostics to the next personal session", () -> {
            Path root = invalidHistory();
            createWorker(root);
            String output = ConsoleTest.launchRaw(root, "admin\nadmin\n13\nworker\nsecret\nchosen\nchosen\n0\n", 0, false);
            assertPrivate(output.substring(output.indexOf("Signed out.")));
        });
        test("Anonymous fatal startup errors do not disclose credential filenames or paths", () -> {
            Path root = Files.createTempDirectory("smartsite-private-fatal-");
            Files.write(root.resolve("accounts.csv"), Arrays.asList("bad"), StandardCharsets.UTF_8);
            String output = ConsoleTest.launchRaw(root, "", 1, false);
            require(output.contains("Session unavailable. Contact an administrator."));
            require(!output.contains("accounts.csv"));
            require(!output.contains(root.toString()));
        });
        finish();
    }
}
