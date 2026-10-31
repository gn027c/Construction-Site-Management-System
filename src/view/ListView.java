package view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.IdentifierHelper;

public final class ListView {
    public static final int PAGE_SIZE = 10;
    private static final Pattern IDENTIFIER = Pattern.compile("(?i)[a-z]+-?[0-9]+");
    private static final Set<String> STATUS_TERMS = new HashSet<>(Arrays.asList(
            "active", "inactive", "open", "assigned",
            "resolved", "archived", "enabled", "disabled",
            "check_in", "check_out", "true", "false"
    ));

    private ListView() {
    }

    public static <T> void show(
            Scanner scanner,
            String title,
            List<T> records,
            Function<T, String> renderer,
            Function<T, String> searchText,
            Comparator<T> order
    ) {
        show(scanner, title, null, records, renderer, searchText, order);
    }

    public static <T> void show(
            Scanner scanner,
            String title,
            String description,
            List<T> records,
            Function<T, String> renderer,
            Function<T, String> searchText,
            Comparator<T> order
    ) {
        showInternal(scanner, title, description, records, null, null, renderer, searchText, order, null);
    }

    public static <T> void showTable(
            Scanner scanner,
            String title,
            List<T> records,
            List<String> headers,
            Function<T, List<String>> rowExtractor,
            Function<T, String> searchText,
            Comparator<T> order
    ) {
        showTable(scanner, title, null, records, headers, rowExtractor, searchText, order);
    }

    public static <T> void showTable(
            Scanner scanner,
            String title,
            String description,
            List<T> records,
            List<String> headers,
            Function<T, List<String>> rowExtractor,
            Function<T, String> searchText,
            Comparator<T> order
    ) {
        showTable(scanner, title, description, records, headers, rowExtractor, searchText, order, null);
    }

    public static <T> void showTable(
            Scanner scanner,
            String title,
            String description,
            List<T> records,
            List<String> headers,
            Function<T, List<String>> rowExtractor,
            Function<T, String> searchText,
            Comparator<T> order,
            Consumer<T> onEdit
    ) {
        showInternal(scanner, title, description, records, headers, rowExtractor, null, searchText, order, onEdit);
    }

    private static <T> void showInternal(
            Scanner scanner,
            String title,
            String description,
            List<T> records,
            List<String> headers,
            Function<T, List<String>> rowExtractor,
            Function<T, String> lineRenderer,
            Function<T, String> searchText,
            Comparator<T> order,
            Consumer<T> onEdit
    ) {
        int currentPage = 1;
        String query = "";
        while (true) {
            List<T> matching = filterAndSort(records, query, searchText, order);
            int pages = pageCount(matching.size());
            currentPage = Math.min(currentPage, Math.max(1, pages));
            ConsoleScreen.begin();
            ConsoleTheme.title(title);
            if (description != null && !description.trim().isEmpty()) {
                ConsoleTheme.description(description);
            }
            System.out.println("Page " + (pages == 0 ? 0 : currentPage) + "/" + pages
                    + " | Results: " + matching.size() + " | Filter: " + (query.isEmpty() ? "All" : query));
            List<T> currentRows = page(matching, currentPage);
            if (currentRows.isEmpty()) {
                System.out.println("No matching records.");
            } else if (headers != null && rowExtractor != null) {
                List<List<String>> rows = new ArrayList<>();
                for (T record : currentRows) {
                    rows.add(rowExtractor.apply(record));
                }
                printTable(headers, rows);
            } else if (lineRenderer != null) {
                for (T record : currentRows) {
                    System.out.println(lineRenderer.apply(record));
                }
            }
            String promptMsg = onEdit != null
                    ? "N-Next, P-Previous, R-Reset, D [row/id]-Detail, E [row/id]-Edit, page (1.." + Math.max(1, pages)
                            + "), search text, Enter/0-Back: "
                    : "N-Next, P-Previous, R-Reset, D [row/id]-Detail, page (1.." + Math.max(1, pages)
                            + "), search text, Enter/0-Back: ";
            ConsoleTheme.prompt(promptMsg);
            String command = scanner.nextLine().trim();
            if (command.isEmpty() || command.equals("0") || command.equalsIgnoreCase("/cancel")) {
                return;
            }
            if (command.equalsIgnoreCase("n")) {
                if (currentPage < pages) {
                    currentPage++;
                } else {
                    ConsoleTheme.warning("No next page.");
                }
            } else if (command.equalsIgnoreCase("p")) {
                if (currentPage > 1) {
                    currentPage--;
                } else {
                    ConsoleTheme.warning("No previous page.");
                }
            } else if (command.equalsIgnoreCase("f")) {
                query = util.InputHelper.readLine(scanner, "Search text or codes (space-separated terms): ").trim();
                currentPage = 1;
            } else if (command.equalsIgnoreCase("r")) {
                query = "";
                currentPage = 1;
            } else if (onEdit != null && (command.equalsIgnoreCase("e") || command.toLowerCase().startsWith("e ") || command.toLowerCase().startsWith("edit"))) {
                String target = command.equalsIgnoreCase("e") || command.equalsIgnoreCase("edit") ? "" : command.substring(command.indexOf(' ') + 1).trim();
                if (target.isEmpty()) {
                    if (currentRows.isEmpty()) {
                        ConsoleTheme.warning("No records to edit.");
                        continue;
                    }
                    ConsoleTheme.prompt("Enter row number (1-" + currentRows.size() + ") or ID/Code to edit: ");
                    target = scanner.nextLine().trim();
                }
                if (!target.isEmpty() && !target.equals("0") && !target.equalsIgnoreCase("/cancel")) {
                    T selectedRecord = null;
                    if (target.matches("^[1-9]\\d*$")) {
                        int idx = Integer.parseInt(target);
                        if (idx >= 1 && idx <= currentRows.size()) {
                            selectedRecord = currentRows.get(idx - 1);
                        }
                    }
                    if (selectedRecord == null) {
                        selectedRecord = findRecord(matching, target, headers, rowExtractor, searchText);
                    }
                    if (selectedRecord != null) {
                        onEdit.accept(selectedRecord);
                    } else {
                        ConsoleTheme.warning("No record found matching '" + target + "'.");
                    }
                }
            } else if (command.equalsIgnoreCase("d") || command.toLowerCase().startsWith("d ") || command.toLowerCase().startsWith("detail")) {
                String target = command.equalsIgnoreCase("d") ? "" : command.substring(command.indexOf(' ') + 1).trim();
                if (target.isEmpty()) {
                    if (currentRows.isEmpty()) {
                        ConsoleTheme.warning("No records to inspect.");
                        continue;
                    }
                    ConsoleTheme.prompt("Enter row number (1-" + currentRows.size() + ") or ID/Code to inspect: ");
                    target = scanner.nextLine().trim();
                }
                if (!target.isEmpty() && !target.equals("0") && !target.equalsIgnoreCase("/cancel")) {
                    T selectedRecord = null;
                    if (target.matches("^[1-9]\\d*$")) {
                        int idx = Integer.parseInt(target);
                        if (idx >= 1 && idx <= currentRows.size()) {
                            selectedRecord = currentRows.get(idx - 1);
                        }
                    }
                    if (selectedRecord == null) {
                        selectedRecord = findRecord(matching, target, headers, rowExtractor, searchText);
                    }
                    if (selectedRecord != null) {
                        showRecordCard(scanner, selectedRecord, headers, rowExtractor, lineRenderer, onEdit);
                    } else {
                        ConsoleTheme.warning("No record found matching '" + target + "'.");
                    }
                }
            } else {
                boolean isPageNumber = command.matches("^[1-9]\\d{0,3}$");
                if (isPageNumber) {
                    try {
                        int requestedPage = Integer.parseInt(command);
                        if (requestedPage < 1 || requestedPage > pages) {
                            ConsoleTheme.warning("Choose an existing page.");
                        } else {
                            currentPage = requestedPage;
                        }
                    } catch (NumberFormatException e) {
                        query = command;
                        currentPage = 1;
                    }
                } else {
                    query = command;
                    currentPage = 1;
                }
            }
        }
    }

    private static <T> void showRecordCard(
            Scanner scanner,
            T record,
            List<String> headers,
            Function<T, List<String>> rowExtractor,
            Function<T, String> lineRenderer,
            Consumer<T> onEdit
    ) {
        ConsoleScreen.begin();
        System.out.println();
        if (record instanceof model.person.Person) {
            PersonnelView.showDetails((model.person.Person) record);
        } else if (record instanceof model.incident.Incident) {
            IncidentView.showDetails((model.incident.Incident) record);
        } else if (record instanceof model.zone.Zone) {
            ZoneView.showDetails((model.zone.Zone) record);
        } else if (record instanceof model.attendance.AttendanceRecord) {
            AttendanceView.showDetails((model.attendance.AttendanceRecord) record);
        } else if (headers != null && rowExtractor != null) {
            ConsoleTheme.title("  ◆ RECORD DETAILS");
            int width = 68;
            System.out.println(ConsoleTheme.color("90", "  ┌" + String.join("", java.util.Collections.nCopies(width, "─")) + "┐"));
            List<String> values = rowExtractor.apply(record);
            int maxLabelLen = 0;
            for (String h : headers) {
                maxLabelLen = Math.max(maxLabelLen, h.length());
            }
            maxLabelLen = Math.max(maxLabelLen, 12);
            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i);
                String val = i < values.size() && values.get(i) != null ? values.get(i) : "";
                System.out.printf("  │ %-" + maxLabelLen + "s : %s%n", header, val);
            }
            System.out.println(ConsoleTheme.color("90", "  └" + String.join("", java.util.Collections.nCopies(width, "─")) + "┘"));
        } else if (lineRenderer != null) {
            ConsoleTheme.title("  ◆ RECORD DETAILS");
            System.out.println("  " + lineRenderer.apply(record));
        } else {
            ConsoleTheme.title("  ◆ RECORD DETAILS");
            System.out.println("  " + record.toString());
        }
        System.out.println();
        if (onEdit != null) {
            ConsoleTheme.prompt("E [edit this record], Enter [back to table]: ");
            String ans = scanner.nextLine().trim();
            if (ans.equalsIgnoreCase("e") || ans.equalsIgnoreCase("edit")) {
                onEdit.accept(record);
            }
        } else {
            ConsoleTheme.prompt("Press Enter to return to table: ");
            scanner.nextLine();
        }
    }

    private static <T> T findRecord(
            List<T> records,
            String target,
            List<String> headers,
            Function<T, List<String>> rowExtractor,
            Function<T, String> searchText
    ) {
        if (target == null || target.trim().isEmpty() || records == null || records.isEmpty()) {
            return null;
        }
        String normalizedTargetId = IdentifierHelper.normalizeForLookup(target);

        if (headers != null && rowExtractor != null && normalizedTargetId != null) {
            for (T rec : records) {
                List<String> row = rowExtractor.apply(rec);
                for (int i = 0; i < Math.min(row.size(), headers.size()); i++) {
                    String h = headers.get(i).toLowerCase(Locale.ROOT);
                    if (h.contains("id") || h.contains("code")) {
                        String cell = row.get(i);
                        if (cell != null && normalizedTargetId.equals(IdentifierHelper.normalizeForLookup(cell))) {
                            return rec;
                        }
                    }
                }
            }
        }

        if (normalizedTargetId != null) {
            for (T rec : records) {
                if (rec instanceof model.person.Person) {
                    model.person.Person p = (model.person.Person) rec;
                    if (normalizedTargetId.equals(IdentifierHelper.normalizeForLookup(p.getCode()))
                            || normalizedTargetId.equals(IdentifierHelper.normalizeForLookup(p.getId()))) {
                        return rec;
                    }
                } else if (rec instanceof model.zone.Zone) {
                    model.zone.Zone z = (model.zone.Zone) rec;
                    if (normalizedTargetId.equals(IdentifierHelper.normalizeForLookup(z.getZoneId()))) {
                        return rec;
                    }
                } else if (rec instanceof model.incident.Incident) {
                    model.incident.Incident inc = (model.incident.Incident) rec;
                    if (normalizedTargetId.equals(IdentifierHelper.normalizeForLookup(inc.getIncidentId()))) {
                        return rec;
                    }
                } else if (rec instanceof model.attendance.AttendanceRecord) {
                    model.attendance.AttendanceRecord att = (model.attendance.AttendanceRecord) rec;
                    if (normalizedTargetId.equals(IdentifierHelper.normalizeForLookup(att.getRecordId()))) {
                        return rec;
                    }
                }
            }
        }

        for (T rec : records) {
            if (rowExtractor != null) {
                List<String> row = rowExtractor.apply(rec);
                for (String cell : row) {
                    if (cell != null && cell.equalsIgnoreCase(target)) {
                        return rec;
                    }
                }
            }
        }

        for (T rec : records) {
            String st = searchText != null ? searchText.apply(rec) : rec.toString();
            if (matches(st, target)) {
                return rec;
            }
        }
        return null;
    }

    public static final int MAX_CELL_WIDTH = 50;

    public static void printTable(List<String> headers, List<List<String>> rows) {
        if (headers == null || headers.isEmpty() || rows == null || rows.isEmpty()) {
            return;
        }
        int columns = headers.size();
        int maxCellLimit = columns > 6 ? 32 : MAX_CELL_WIDTH;
        int[] widths = new int[columns];
        for (int i = 0; i < columns; i++) {
            widths[i] = headers.get(i).length();
        }
        List<List<String>> formattedRows = new ArrayList<>(rows.size());
        for (List<String> row : rows) {
            List<String> formattedRow = new ArrayList<>(columns);
            for (int i = 0; i < columns; i++) {
                String val = i < row.size() && row.get(i) != null ? row.get(i) : "";
                val = val.replace("\r", "").replace("\n", " ");
                if (val.length() > maxCellLimit) {
                    val = val.substring(0, maxCellLimit - 3).trim() + "...";
                }
                formattedRow.add(val);
                widths[i] = Math.max(widths[i], val.length());
            }
            formattedRows.add(formattedRow);
        }
        StringBuilder topBorder = new StringBuilder("  ┌");
        for (int i = 0; i < columns; i++) {
            topBorder.append(String.join("", java.util.Collections.nCopies(widths[i] + 2, "─")));
            topBorder.append(i == columns - 1 ? "┐" : "┬");
        }

        StringBuilder midBorder = new StringBuilder("  ├");
        for (int i = 0; i < columns; i++) {
            midBorder.append(String.join("", java.util.Collections.nCopies(widths[i] + 2, "─")));
            midBorder.append(i == columns - 1 ? "┤" : "┼");
        }

        StringBuilder botBorder = new StringBuilder("  └");
        for (int i = 0; i < columns; i++) {
            botBorder.append(String.join("", java.util.Collections.nCopies(widths[i] + 2, "─")));
            botBorder.append(i == columns - 1 ? "┘" : "┴");
        }

        System.out.println(ConsoleTheme.color("90", topBorder.toString()));
        StringBuilder headerRow = new StringBuilder("  │");
        for (int i = 0; i < columns; i++) {
            headerRow.append(String.format(" %-" + widths[i] + "s │", headers.get(i)));
        }
        System.out.println(ConsoleTheme.color("1;37", headerRow.toString()));
        System.out.println(ConsoleTheme.color("90", midBorder.toString()));

        for (List<String> row : formattedRows) {
            StringBuilder rowStr = new StringBuilder("  │");
            for (int i = 0; i < columns; i++) {
                String val = row.get(i);
                rowStr.append(String.format(" %-" + widths[i] + "s │", val));
            }
            System.out.println(rowStr.toString());
        }
        System.out.println(ConsoleTheme.color("90", botBorder.toString()));
    }

    public static <T> List<T> filterAndSort(
            List<T> records,
            String query,
            Function<T, String> searchText,
            Comparator<T> order
    ) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<T> matching = new ArrayList<>();
        for (T record : records) {
            String text = searchText.apply(record).toLowerCase(Locale.ROOT);
            boolean accepts = true;
            for (String term : normalizedQuery.split("\\s+")) {
                if (!matches(text, term)) {
                    accepts = false;
                    break;
                }
            }
            if (accepts) {
                matching.add(record);
            }
        }
        if (order != null) {
            matching.sort(order);
        }
        return matching;
    }

    private static boolean matches(String text, String term) {
        if (term.isEmpty()) {
            return true;
        }
        if (!IDENTIFIER.matcher(term).matches()) {
            if (STATUS_TERMS.contains(term)) {
                return Pattern.compile("(?<![a-z0-9_])" + Pattern.quote(term) + "(?![a-z0-9_])")
                        .matcher(text).find();
            }
            return text.contains(term);
        }
        String wanted = IdentifierHelper.normalizeForLookup(term);
        if (wanted == null) {
            return false;
        }
        Matcher identifiers = IDENTIFIER.matcher(text);
        while (identifiers.find()) {
            if (wanted.equals(IdentifierHelper.normalizeForLookup(identifiers.group()))) {
                return true;
            }
        }
        return false;
    }

    public static int pageCount(int recordCount) {
        if (recordCount < 0) {
            throw new IllegalArgumentException("Record count must not be negative.");
        }
        return recordCount / PAGE_SIZE + (recordCount % PAGE_SIZE == 0 ? 0 : 1);
    }

    public static <T> List<T> page(List<T> records, int pageNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("Page number must be positive.");
        }
        long start = ((long) pageNumber - 1) * PAGE_SIZE;
        if (start >= records.size()) {
            return new ArrayList<>();
        }
        int end = (int) Math.min(start + PAGE_SIZE, records.size());
        return new ArrayList<>(records.subList((int) start, end));
    }
}
