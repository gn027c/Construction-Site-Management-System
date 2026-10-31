import static support.Checks.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import view.ConsoleScreen;
import view.ConsoleTheme;

public class ConsoleScreenTest {
    private static String capture(String mode, Scanner input, Runnable action) throws Exception {
        String previousMode = System.getProperty("smartsite.screen");
        PrintStream previousOutput = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(bytes, true, "UTF-8")) {
            System.setProperty("smartsite.screen", mode);
            ConsoleScreen.attach(input);
            System.setOut(output);
            action.run();
        } finally {
            System.setOut(previousOutput);
            System.setProperty("smartsite.screen", "never");
            ConsoleScreen.attach(new Scanner(""));
            if (previousMode == null) {
                System.clearProperty("smartsite.screen");
            } else {
                System.setProperty("smartsite.screen", previousMode);
            }
        }
        return bytes.toString("UTF-8");
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("smartsite.color", "never");
        test("One and two digit options align with the exit option", () -> {
            Map<Integer, String> options = new LinkedHashMap<>();
            options.put(1, "Personnel");
            options.put(9, "Audit settings");
            options.put(10, "Sign out");
            options.put(12, "Period reports");
            String output = capture("never", new Scanner(""), () -> {
                ConsoleTheme.menuOptions("HOME", options, "Save and exit");
            });
            for (String label : new String[] {
                    "Personnel", "Audit settings", "Sign out", "Period reports", "Save and exit"
            }) {
                String line = java.util.Arrays.stream(output.split("\\R"))
                        .filter(value -> value.endsWith(label)).findFirst().get();
                equal(7, line.indexOf(label));
            }
        });
        test("Breadcrumb description renders directly beneath breadcrumb and above divider", () -> {
            Map<Integer, String> options = new LinkedHashMap<>();
            options.put(1, "Personnel");
            String output = capture("never", new Scanner(""), () -> {
                ConsoleTheme.menuOptions("TEST PATH", "Test description text", options, "Back");
            });
            require(output.contains("SMARTSITE / TEST PATH"));
            String smallDesc = ConsoleTheme.toSmallCaps("Test description text");
            require(output.contains("  › " + smallDesc));
            int breadcrumbIdx = output.indexOf("SMARTSITE / TEST PATH");
            int descIdx = output.indexOf(smallDesc);
            int dividerIdx = output.indexOf("──────");
            if (dividerIdx < 0) {
                dividerIdx = output.indexOf("------");
            }
            require(breadcrumbIdx < descIdx);
            require(descIdx < dividerIdx);
        });
        test("Results remain visible until Enter before the next screen is cleared", () -> {
            Scanner input = new Scanner("\nnext\n");
            String output = capture("always", input, () -> {
                ConsoleTheme.success("Saved.");
                ConsoleScreen.begin();
            });
            require(output.indexOf("Saved.") < output.indexOf("Press Enter"));
            require(output.indexOf("Press Enter") < output.indexOf("\u001b[2J\u001b[H"));
            equal("next", input.nextLine());
        });
        test("A follow-up prompt acknowledges feedback without consuming another input line", () -> {
            Scanner input = new Scanner("menu-choice\n");
            String output = capture("always", input, () -> {
                ConsoleTheme.warning("Invalid input.");
                ConsoleTheme.prompt("Try again: ");
                ConsoleScreen.begin();
            });
            require(!output.contains("Press Enter"));
            equal("menu-choice", input.nextLine());
        });
        test("Account changes clear screen and request scrollback removal without a pause", () -> {
            Scanner input = new Scanner("next-user\n");
            String output = capture("always", input, () -> {
                ConsoleTheme.success("Administrator result.");
                ConsoleScreen.identityBoundary();
                ConsoleScreen.begin();
            });
            require(output.contains("\u001b[3J\u001b[2J\u001b[H"));
            require(!output.contains("Press Enter"));
            equal("next-user", input.nextLine());
        });
        test("Disabled screen mode retains plain output without requiring acknowledgement", () -> {
            String output = capture("never", new Scanner(""), () -> {
                ConsoleTheme.success("Saved.");
                ConsoleScreen.begin();
                ConsoleScreen.identityBoundary();
            });
            require(output.contains("Saved."));
            require(!output.contains("\u001b["));
            require(!output.contains("Press Enter"));
        });
        test("Interactive screen mode supports acknowledgement, navigation and clean logout", () -> {
            String output = ConsoleTest.launchScreen(
                    Files.createTempDirectory("smartsite-screen-ui-"), "admin\nadmin\n\n10\n0\n", 0, false, "always"
            );
            require(output.contains("Press Enter to continue"));
            require(output.contains("HOME / admin / ADMIN"));
            int signOut = output.indexOf("Signed out.");
            require(signOut >= 0);
            int nextLogin = output.indexOf("STAFF SIGN IN", signOut);
            require(nextLogin > signOut);
            require(output.substring(signOut, nextLogin).contains("\u001b[3J\u001b[2J\u001b[H"));
        });
        test("EOF while acknowledging a result exits without a console loop", () -> {
            String output = ConsoleTest.launchScreen(
                    Files.createTempDirectory("smartsite-screen-eof-"), "admin\nadmin\n", 0, false, "always"
            );
            require(output.contains("Press Enter to continue"));
            require(output.contains("Input closed. CSV data saved."));
        });
        finish();
    }
}
