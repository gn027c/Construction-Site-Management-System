package util;

public final class ConsoleColor {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";

    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";
    public static final String GRAY = "\u001B[90m";

    private ConsoleColor() {
    }

    public static String color(String text, String colorCode) {
        if (text == null) {
            return "";
        }
        return colorCode + text + RESET;
    }

    public static String red(String text) {
        return color(text, RED);
    }

    public static String green(String text) {
        return color(text, GREEN);
    }

    public static String yellow(String text) {
        return color(text, YELLOW);
    }

    public static String cyan(String text) {
        return color(text, CYAN);
    }

    public static String blue(String text) {
        return color(text, BLUE);
    }

    public static String bold(String text) {
        return color(text, BOLD);
    }

    public static String gray(String text) {
        return color(text, GRAY);
    }
}

