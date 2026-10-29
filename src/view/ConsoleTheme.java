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
        System.out.print(color("36", text));
    }

    public static void title(String text) {
        System.out.println(color("1;36", text));
    }

    public static void success(String text) {
        System.out.println(color("32", "[OK] " + text));
    }

    public static void warning(String text) {
        System.out.println(color("33", "[WARN] " + text));
    }

    public static void error(String text) {
        System.err.println(color("31", "[ERROR] " + text));
    }

    public static void menu(String title, String... entries) {
        java.util.Map<Integer, String> options = new java.util.LinkedHashMap<>();
        for (int i = 0; i < entries.length; i++) {
            options.put(i + 1, entries[i]);
        }
        menuOptions(title, options, title.equals("HOME") ? "Save and exit" : "Back");
    }

    public static void menuOptions(String title, java.util.Map<Integer, String> options, String exitLabel) {
        System.out.println();
        title("  SMARTSITE  /  " + title);
        System.out.println(color("90", "  ----------------------------------------------------"));
        for (java.util.Map.Entry<Integer, String> entry : options.entrySet()) {
            System.out.println("  " + color("36", "[" + entry.getKey() + "]") + " " + entry.getValue());
        }
        System.out.println("  " + color("33", "[0]") + " " + exitLabel);
        System.out.println();
    }
}
