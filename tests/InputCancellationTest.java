import static support.Checks.*;

import exception.InputCancelledException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import service.PersonnelService;
import util.InputHelper;

public class InputCancellationTest {
    private static PersonnelService people(Path root) {
        PersonnelService people = new PersonnelService();
        require(people.loadFromCsv(root.resolve("persons.csv").toString()));
        return people;
    }

    public static void main(String[] args) throws Exception {
        test("All field readers accept cancellation independently of validation errors", () -> {
            throwsType(InputCancelledException.class, () -> InputHelper.getString(new Scanner("/cancel\n"), "Name: "));
            throwsType(InputCancelledException.class, () -> InputHelper.getInt(new Scanner("x\n/cancel\n"), "Type: ", 1, 4));
            throwsType(InputCancelledException.class, () -> InputHelper.getDate(new Scanner("/cancel\n"), "Date: "));
            throwsType(InputCancelledException.class, () -> InputHelper.getPassword(new Scanner("/cancel\n"), "Password: "));
            throwsType(InputCancelledException.class, () -> InputHelper.readLine(new Scanner("/cancel\n"), "Optional: "));
            throwsType(InputCancelledException.class, () -> InputHelper.getInt(new Scanner("\n"), "Role: ", 1, 4));
        });
        test("Menu Enter, zero and cancel return Back without trapping the reader", () -> {
            for (String input : new String[] { "\n", "0\n", "/cancel\n", "bad\n\n" }) {
                equal(0, InputHelper.getMenuChoice(new Scanner(input), "Choice: ", 3));
            }
        });
        test("Meaningful zero values and optional empty strings retain their semantics", () -> {
            equal(0, InputHelper.getInt(new Scanner("0\n"), "Deactivate: ", 0, 1));
            equal("0", InputHelper.getString(new Scanner("0\n"), "No permissions: "));
            equal("", InputHelper.readLine(new Scanner("\n"), "Keep original: "));
        });
        test("Literal cancel text can be escaped without losing password whitespace", () -> {
            equal("/cancel", InputHelper.getString(new Scanner("//cancel\n"), "Text: "));
            equal("  /cancel  ", InputHelper.getPassword(new Scanner("  //cancel  \n"), "Password: "));
        });
        test("Cancelling a partially entered worker returns Home without saving a person", () -> {
            Path root = Files.createTempDirectory("smartsite-cancel-worker-");
            String output = ConsoleTest.launch(
                    root, "1\n1\n1\nP1\nW1\nWorker A\n0901234567\n/cancel\n0\n", 0, false
            );
            require(output.contains("Current entry cancelled. Returned to the main menu."));
            equal(0, people(root).getAllPersons().size());
            String log = new String(Files.readAllBytes(root.resolve("activity_log.csv")), StandardCharsets.UTF_8);
            require(log.contains("CANCELLED,User cancelled input."));
        });
        test("Cancelling account role selection cannot create an account", () -> {
            Path root = Files.createTempDirectory("smartsite-cancel-account-");
            String output = ConsoleTest.launch(root, "6\n2\nnewuser\n/cancel\n0\n", 0, false);
            require(output.contains("Current entry cancelled"));
            service.AuthenticationService auth = new service.AuthenticationService(root);
            auth.login("admin", "admin");
            equal(1, auth.listAccounts().size());
        });
        test("Cancelling visitor date entry cannot save partially entered personnel", () -> {
            Path root = Files.createTempDirectory("smartsite-cancel-date-");
            ConsoleTest.launch(
                    root, "1\n1\n3\nP1\nV1\nVisitor A\n0901234567\nMeeting\nHost A\n/cancel\n0\n", 0, false
            );
            equal(0, people(root).getAllPersons().size());
        });
        test("Cancelling a contact edit retains both existing contact values", () -> {
            Path root = Files.createTempDirectory("smartsite-cancel-contact-");
            ConsoleTest.launch(
                    root, "1\n1\n1\nP1\nW1\nOriginal\n0901234567\nWelder\nX_SAFE\nTeam A\n0\n0\n", 0, false
            );
            ConsoleTest.launch(root, "1\n3\nW1\nChanged\n/cancel\n0\n", 0, false);
            equal("Original", people(root).findPersonByCode("W1").getName());
            equal("0901234567", people(root).findPersonByCode("W1").getPhoneNumber());
        });
        test("Blank submenu selection returns Home and blank Home selection exits", () -> {
            String output = ConsoleTest.launch(Files.createTempDirectory("smartsite-cancel-menu-"), "1\n\n\n", 0, false);
            require(output.contains("Goodbye."));
            require(!output.contains("Cannot be empty"));
        });
        test("Cancelling password entry returns to username without spending a login attempt", () -> {
            String output = ConsoleTest.launchRaw(
                    Files.createTempDirectory("smartsite-cancel-login-"), "admin\n/cancel\n\n", 0, false
            );
            require(output.contains("Sign-in cancelled"));
            require(!output.contains("Sign-in attempts exhausted"));
            require(!output.contains("HOME /"));
        });
        finish();
    }
}
