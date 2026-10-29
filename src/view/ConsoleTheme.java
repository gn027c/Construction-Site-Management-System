package view;

/**
 * Renders console messages with optional ANSI colors and visible status labels.
 *
 * <p>{@code NO_COLOR} disables colors. The {@code smartsite.color} property accepts
 * {@code always} or {@code never}; otherwise colors require an attached console.
 * Status labels remain readable when colors are disabled.
 */
public final class ConsoleTheme {
    private static final boolean ENABLED =
            !"never".equalsIgnoreCase(System.getProperty("smartsite.color"))
                    && System.getenv("NO_COLOR") == null
                    && (
                            System.console() != null
                                    || "always".equalsIgnoreCase(System.getProperty("smartsite.color"))
                    );

    private ConsoleTheme() {
    }

    private static String color(String code, String text) {
        return ENABLED ? "\u001b[" + code + "m" + text + "\u001b[0m" : text;
    }

    public static void prompt(String text) {
        ConsoleScreen.inputPrompted();
        System.out.print(color("36", text));
    }

    public static void title(String text) {
        System.out.println(color("1;36", text));
    }

    public static void success(String text) {
        System.out.println(color("32", "[OK] " + text));
        ConsoleScreen.resultShown();
    }

    public static void warning(String text) {
        System.out.println(color("33", "[WARN] " + text));
        ConsoleScreen.resultShown();
    }

    public static void error(String text) {
        System.err.println(color("31", "[ERROR] " + text));
        ConsoleScreen.resultShown();
    }

    public static void menu(String title, String... entries) {
        java.util.Map<Integer, String> options = new java.util.LinkedHashMap<>();
        for (int i = 0; i < entries.length; i++) {
            options.put(i + 1, entries[i]);
        }
        menuOptions(title, options, title.equals("HOME") ? "Save and exit" : "Back");
    }

    public static void menuOptions(String title, java.util.Map<Integer, String> options, String exitLabel) {
        ConsoleScreen.begin();
        System.out.println();
        title("  SMARTSITE  /  " + title);
        System.out.println(color("90", "  " + String.join("", java.util.Collections.nCopies(70, "-"))));
        System.out.println();
        for (java.util.Map.Entry<Integer, String> entry : options.entrySet()) {
            String option = String.format("%4s", "[" + entry.getKey() + "]");
            System.out.println("  " + color("36", option) + " " + entry.getValue());
        }
        System.out.println();
        System.out.println("  " + color("33", " [0]") + " " + exitLabel);
        System.out.println();
    }
}
