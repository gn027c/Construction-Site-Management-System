package view;

import util.ConsoleColor;

public final class ConsoleUi {
    private static final int CONTENT_WIDTH = 58;

    private ConsoleUi() {
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void banner(String title, String subtitle) {
        System.out.println();
        System.out.println(border('═', '╔', '╗'));
        System.out.println(textLine(title));
        if (subtitle != null && !subtitle.trim().isEmpty()) {
            System.out.println(textLine(subtitle));
        }
        System.out.println(border('═', '╚', '╝'));
    }

    public static void menu(String title, String... items) {
        menuWithContext(title, null, items);
    }

    public static void menuWithContext(String title, String context, String... items) {
        System.out.println();
        System.out.println(border('─', '┌', '┐'));
        System.out.println(textLine(title));
        if (context != null && !context.trim().isEmpty()) {
            System.out.println(leftLine(context));
        }
        System.out.println(border('─', '├', '┤'));
        for (String item : items) {
            System.out.println(leftLine(item));
        }
        System.out.println(border('─', '└', '┘'));
    }

    public static void section(String title) {
        int fill = Math.max(0, CONTENT_WIDTH - title.length() - 4);
        System.out.println(ConsoleColor.cyan("── " + title + " " + repeat('─', fill)));
    }

    public static void breadcrumb(String text) {
        System.out.println(ConsoleColor.cyan("[ " + text + " ]"));
    }

    public static void success(String message) {
        System.out.println(ConsoleColor.green("[OK]") + " " + message);
    }

    public static void error(String message) {
        System.out.println(ConsoleColor.red("[ERROR]") + " " + message);
    }

    public static void warning(String message) {
        System.out.println(ConsoleColor.yellow("[WARN]") + " " + message);
    }

    public static void table(String[] headers, java.util.List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return;
        }
        int colCount = headers.length;
        int[] widths = new int[colCount];
        for (int i = 0; i < colCount; i++) {
            widths[i] = headers[i] != null ? headers[i].trim().length() : 0;
        }
        if (rows != null) {
            for (String[] row : rows) {
                for (int i = 0; i < colCount && i < row.length; i++) {
                    if (row[i] != null) {
                        widths[i] = Math.max(widths[i], row[i].trim().length());
                    }
                }
            }
        }

        StringBuilder top = new StringBuilder("┌");
        for (int i = 0; i < colCount; i++) {
            top.append(repeat('─', widths[i] + 2));
            top.append(i < colCount - 1 ? "┬" : "┐");
        }
        System.out.println(top.toString());

        StringBuilder headerLine = new StringBuilder("│");
        for (int i = 0; i < colCount; i++) {
            String h = headers[i] == null ? "" : headers[i].trim();
            int pad = widths[i] - h.length();
            headerLine.append(" ").append(ConsoleColor.bold(ConsoleColor.cyan(h))).append(spaces(pad)).append(" │");
        }
        System.out.println(headerLine.toString());

        StringBuilder middle = new StringBuilder("├");
        for (int i = 0; i < colCount; i++) {
            middle.append(repeat('─', widths[i] + 2));
            middle.append(i < colCount - 1 ? "┼" : "┤");
        }
        System.out.println(middle.toString());

        if (rows != null) {
            for (String[] row : rows) {
                StringBuilder rowLine = new StringBuilder("│");
                for (int i = 0; i < colCount; i++) {
                    String val = (i < row.length && row[i] != null) ? row[i].trim() : "";
                    int pad = widths[i] - val.length();
                    rowLine.append(" ").append(colorizeCell(val)).append(spaces(pad)).append(" │");
                }
                System.out.println(rowLine.toString());
            }
        }

        StringBuilder bottom = new StringBuilder("└");
        for (int i = 0; i < colCount; i++) {
            bottom.append(repeat('─', widths[i] + 2));
            bottom.append(i < colCount - 1 ? "┴" : "┘");
        }
        System.out.println(bottom.toString());
    }

    public static void tableHeader(String... columns) {
        System.out.println("┌" + repeat('─', CONTENT_WIDTH + 2) + "┐");
        StringBuilder row = new StringBuilder("│ ");
        int separators = Math.max(0, (columns.length - 1) * 3);
        int contentWidth = Math.max(1, CONTENT_WIDTH - separators);
        int columnWidth = columns.length == 0 ? contentWidth : Math.max(1, contentWidth / columns.length);
        int remainder = columns.length == 0 ? 0 : contentWidth - columnWidth * columns.length;
        for (int i = 0; i < columns.length; i++) {
            String value = columns[i] == null ? "" : columns[i].trim();
            int width = i == columns.length - 1 ? columnWidth + remainder : columnWidth;
            if (value.length() > width) {
                value = value.substring(0, Math.max(1, width - 1)) + "…";
            }
            int pad = width - value.length();
            row.append(ConsoleColor.bold(ConsoleColor.cyan(value))).append(spaces(pad));
            if (i < columns.length - 1) {
                row.append(" │ ");
            }
        }
        row.append(" │");
        System.out.println(row.toString());
        System.out.println("├" + repeat('─', CONTENT_WIDTH + 2) + "┤");
    }

    public static void tableRow(String... columns) {
        StringBuilder row = new StringBuilder("│ ");
        int separators = Math.max(0, (columns.length - 1) * 3);
        int contentWidth = Math.max(1, CONTENT_WIDTH - separators);
        int columnWidth = columns.length == 0 ? contentWidth : Math.max(1, contentWidth / columns.length);
        int remainder = columns.length == 0 ? 0 : contentWidth - columnWidth * columns.length;
        for (int i = 0; i < columns.length; i++) {
            String value = columns[i] == null ? "" : columns[i].trim();
            int width = i == columns.length - 1 ? columnWidth + remainder : columnWidth;
            if (value.length() > width) {
                value = value.substring(0, Math.max(1, width - 1)) + "…";
            }
            int pad = width - value.length();
            row.append(colorizeCell(value)).append(spaces(pad));
            if (i < columns.length - 1) {
                row.append(" │ ");
            }
        }
        row.append(" │");
        System.out.println(row.toString());
    }

    public static void tableEnd() {
        System.out.println("└" + repeat('─', CONTENT_WIDTH + 2) + "┘");
    }

    private static String colorizeCell(String val) {
        if (val == null || val.isEmpty()) {
            return "";
        }
        switch (val.toUpperCase()) {
            case "ACTIVE":
            case "ENABLED":
            case "RESOLVED":
                return ConsoleColor.green(val);
            case "INACTIVE":
            case "DISABLED":
            case "CRITICAL":
            case "OPEN":
                return ConsoleColor.red(val);
            case "HIGH":
            case "MEDIUM":
            case "INVESTIGATING":
            case "ASSIGNED":
                return ConsoleColor.yellow(val);
            case "LOW":
            case "REPORTED":
                return ConsoleColor.cyan(val);
            default:
                return val;
        }
    }

    private static String border(char fill, char left, char right) {
        return left + repeat(fill, CONTENT_WIDTH + 2) + right;
    }

    private static String textLine(String value) {
        String clean = value == null ? "" : value;
        if (clean.length() > CONTENT_WIDTH) {
            clean = clean.substring(0, CONTENT_WIDTH - 1) + "…";
        }
        int left = (CONTENT_WIDTH - clean.length()) / 2;
        int right = CONTENT_WIDTH - clean.length() - left;
        return "│ " + spaces(left) + clean + spaces(right) + " │";
    }

    private static String leftLine(String value) {
        String clean = value == null ? "" : value;
        if (clean.length() > CONTENT_WIDTH) {
            clean = clean.substring(0, CONTENT_WIDTH - 1) + "…";
        }
        return "│ " + clean + spaces(CONTENT_WIDTH - clean.length()) + " │";
    }

    private static String repeat(char value, int count) {
        StringBuilder result = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            result.append(value);
        }
        return result.toString();
    }

    private static String spaces(int count) {
        return repeat(' ', count);
    }
}
