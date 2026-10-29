package view;

import java.util.Scanner;

/**
 * Manages screen transitions and result acknowledgement using the application's shared input reader.
 * Redirected sessions retain plain sequential output unless screen mode is explicitly enabled.
 */
public final class ConsoleScreen {
    private static Scanner input;
    private static boolean enabled;
    private static boolean unreadResult;

    private ConsoleScreen() {
    }

    public static void attach(Scanner scanner) {
        String mode = System.getProperty("smartsite.screen", "auto");
        input = scanner;
        enabled = "always".equalsIgnoreCase(mode)
                || (!"never".equalsIgnoreCase(mode)
                        && System.console() != null && !"dumb".equalsIgnoreCase(System.getenv("TERM")));
        unreadResult = false;
    }

    /**
     * Keeps the previous result visible until acknowledged, then starts a clean screen.
     */
    public static void begin() {
        if (!enabled) {
            return;
        }
        if (unreadResult) {
            System.out.print("\n  Press Enter to continue...");
            System.out.flush();
            input.nextLine();
        }
        clear(false);
    }

    /**
     * Clears the visible screen and requests scrollback removal when changing account context.
     * Terminal support varies; this does not revoke access to captured output or local files.
     */
    public static void identityBoundary() {
        if (enabled) {
            clear(true);
        }
        unreadResult = false;
    }

    public static void resultShown() {
        unreadResult = true;
    }

    public static void inputPrompted() {
        unreadResult = false;
    }

    private static void clear(boolean discardScrollback) {
        if (discardScrollback) {
            System.out.print("\u001b[3J");
        }
        System.out.print("\u001b[2J\u001b[H");
        System.out.flush();
        unreadResult = false;
    }
}
