package view;

/** Terminal colors are optional and never required to understand a status. */
public final class ConsoleTheme {
    private static final boolean ENABLED = !"never".equalsIgnoreCase(System.getProperty("smartsite.color"))
        && System.getenv("NO_COLOR") == null
        && (System.console() != null || "always".equalsIgnoreCase(System.getProperty("smartsite.color")));
    private ConsoleTheme() { }
    private static String color(String code, String text) { return ENABLED ? "\u001b[" + code + "m" + text + "\u001b[0m" : text; }
    public static void prompt(String text) { System.out.print(color("36", text)); }
    public static void title(String text) { System.out.println(color("1;36", text)); }
    public static void success(String text) { System.out.println(color("32", "[OK] " + text)); }
    public static void warning(String text) { System.out.println(color("33", "[WARN] " + text)); }
    public static void error(String text) { System.err.println(color("31", "[ERROR] " + text)); }
    public static void menu(String title, String... entries) {
        System.out.println();
        title("  SMARTSITE  /  " + title);
        System.out.println(color("90", "  ----------------------------------------------------"));
        for (int i = 0; i < entries.length; i++) System.out.println("  " + color("36", "[" + (i+1) + "]") + " " + entries[i]);
        System.out.println("  " + color("33", "[0]") + " " + (title.equals("HOME") ? "Save and exit" : "Back"));
        System.out.println();
    }
}
