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

    public static String color(String code, String text) {
        return ENABLED ? "\u001b[" + code + "m" + text + "\u001b[0m" : text;
    }

    public static void prompt(String text) {
        ConsoleScreen.inputPrompted();
        if (!ENABLED) {
            System.out.print(text);
            return;
        }
        int openParen = text.indexOf('(');
        if (openParen > 0) {
            String label = text.substring(0, openParen);
            String hint = text.substring(openParen);
            System.out.print("\u001b[1;37m" + label + "\u001b[0m" + "\u001b[36m" + hint + "\u001b[0m");
        } else {
            System.out.print("\u001b[1;37m" + text + "\u001b[0m");
        }
    }

    public static void title(String text) {
        System.out.println(color("1;36", text));
    }

    public static void title(String text, String description) {
        title(text);
        if (description != null && !description.trim().isEmpty()) {
            description(description);
        }
    }

    public static void description(String text) {
        if (text == null) {
            return;
        }
        String clean = text.trim();
        System.out.println(color("90", "  › " + toSmallCaps(clean)));
    }

    public static String toBoldSans(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.appendCodePoint(0x1D5D4 + (c - 'A'));
            } else if (c >= 'a' && c <= 'z') {
                sb.appendCodePoint(0x1D5EE + (c - 'a'));
            } else if (c >= '0' && c <= '9') {
                sb.appendCodePoint(0x1D7EC + (c - '0'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toItalicSans(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.appendCodePoint(0x1D608 + (c - 'A'));
            } else if (c >= 'a' && c <= 'z') {
                sb.appendCodePoint(0x1D622 + (c - 'a'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toSmallCaps(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (Character.toUpperCase(c)) {
                case 'A': sb.append('\u1D00'); break;
                case 'B': sb.append('\u0299'); break;
                case 'C': sb.append('\u1D04'); break;
                case 'D': sb.append('\u1D05'); break;
                case 'E': sb.append('\u1D07'); break;
                case 'F': sb.append('\uA730'); break;
                case 'G': sb.append('\u0262'); break;
                case 'H': sb.append('\u029C'); break;
                case 'I': sb.append('\u026A'); break;
                case 'J': sb.append('\u1D0A'); break;
                case 'K': sb.append('\u1D0B'); break;
                case 'L': sb.append('\u029F'); break;
                case 'M': sb.append('\u1D0D'); break;
                case 'N': sb.append('\u0274'); break;
                case 'O': sb.append('\u1D0F'); break;
                case 'P': sb.append('\u1D18'); break;
                case 'Q': sb.append('\u01EB'); break;
                case 'R': sb.append('\u0280'); break;
                case 'S': sb.append('\uA731'); break;
                case 'T': sb.append('\u1D1B'); break;
                case 'U': sb.append('\u1D1C'); break;
                case 'V': sb.append('\u1D20'); break;
                case 'W': sb.append('\u1D21'); break;
                case 'X': sb.append('x'); break;
                case 'Y': sb.append('\u028F'); break;
                case 'Z': sb.append('\u1D22'); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }

    public static void breadcrumb(String text) {
        breadcrumb(text, null);
    }

    public static void breadcrumb(String text, String description) {
        title("  SMARTSITE / " + text);
        if (description != null && !description.trim().isEmpty()) {
            description(description);
        }
    }

    public static void success(String text) {
        System.out.println(color("1;32", "✔ [OK]") + " " + text);
        ConsoleScreen.resultShown();
    }

    public static void warning(String text) {
        System.out.println(color("1;33", "⚠ [WARN]") + " " + text);
        ConsoleScreen.resultShown();
    }

    public static void error(String text) {
        System.err.println(color("1;31", "✖ [ERROR]") + " " + text);
        ConsoleScreen.resultShown();
    }

    public static void menu(String title, String... entries) {
        java.util.Map<Integer, String> options = new java.util.LinkedHashMap<>();
        for (int i = 0; i < entries.length; i++) {
            options.put(i + 1, entries[i]);
        }
        menuOptions(title, null, options, title.equals("HOME") ? "Save and exit" : "Back");
    }

    public static void menuWithDescription(String title, String description, String... entries) {
        java.util.Map<Integer, String> options = new java.util.LinkedHashMap<>();
        for (int i = 0; i < entries.length; i++) {
            options.put(i + 1, entries[i]);
        }
        menuOptions(title, description, options, title.equals("HOME") ? "Save and exit" : "Back");
    }

    public static void menuOptions(String title, java.util.Map<Integer, String> options, String exitLabel) {
        menuOptions(title, null, options, exitLabel);
    }

    public static void menuOptions(
            String title,
            String description,
            java.util.Map<Integer, String> options,
            String exitLabel
    ) {
        ConsoleScreen.begin();
        System.out.println();
        breadcrumb(title, description);
        System.out.println(color("90", "  " + String.join("", java.util.Collections.nCopies(70, "─"))));
        System.out.println();
        for (java.util.Map.Entry<Integer, String> entry : options.entrySet()) {
            String option = String.format("%4s", "[" + entry.getKey() + "]");
            System.out.println("  " + color("36", option) + " " + entry.getValue());
        }
        System.out.println();
        System.out.println("  " + color("33", " [0]") + " " + exitLabel);
        System.out.println();
    }

    public static void categorizedMenuOptions(
            String title,
            java.util.Map<String, java.util.Map<Integer, String>> sections,
            String exitLabel
    ) {
        categorizedMenuOptions(title, null, sections, exitLabel);
    }

    public static void categorizedMenuOptions(
            String title,
            String description,
            java.util.Map<String, java.util.Map<Integer, String>> sections,
            String exitLabel
    ) {
        ConsoleScreen.begin();
        System.out.println();
        breadcrumb(title, description);
        System.out.println(color("90", "  " + String.join("", java.util.Collections.nCopies(70, "─"))));
        System.out.println();
        for (java.util.Map.Entry<String, java.util.Map<Integer, String>> section : sections.entrySet()) {
            java.util.Map<Integer, String> options = section.getValue();
            if (options.isEmpty()) {
                continue;
            }
            System.out.println(color("36", "  ◆ ") + color("1;37", toSmallCaps(section.getKey())));
            for (java.util.Map.Entry<Integer, String> entry : options.entrySet()) {
                String option = String.format("%4s", "[" + entry.getKey() + "]");
                System.out.println("  " + color("36", option) + " " + entry.getValue());
            }
            System.out.println();
        }
        System.out.println("  " + color("33", " [0]") + " " + exitLabel);
        System.out.println();
    }
}
