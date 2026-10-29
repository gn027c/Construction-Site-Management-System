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

/**
 * Displays an already-authorized collection with search, ordering and ten-row pages.
 * Callers must restrict records to the current account before passing them to this view.
 */
public final class ListView {
    public static final int PAGE_SIZE = 10;
    private static final Pattern IDENTIFIER = Pattern.compile("(?i)[a-z]+-?[0-9]+");
    private static final Set<String> STATUS_TERMS = new HashSet<>(Arrays.asList(
            "active", "inactive", "open", "assigned", "resolved", "archived", "enabled", "disabled",
            "check_in", "check_out", "success", "failed", "cancelled", "true", "false"
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
        show(scanner, title, records, renderer, searchText, order, action -> {
        });
    }

    /**
     * Reports navigation action names without including arbitrary user-entered search text.
     */
    public static <T> void show(
            Scanner scanner,
            String title,
            List<T> records,
            Function<T, String> renderer,
            Function<T, String> searchText,
            Comparator<T> order,
            Consumer<String> onNavigation
    ) {
        int currentPage = 1;
        String query = "";
        while (true) {
            List<T> matching = filterAndSort(records, query, searchText, order);
            int pages = pageCount(matching.size());
            currentPage = Math.min(currentPage, Math.max(1, pages));
            ConsoleScreen.begin();
            ConsoleTheme.title(title);
            System.out.println("Page " + (pages == 0 ? 0 : currentPage) + "/" + pages
                    + " | Results: " + matching.size() + " | Filter: " + (query.isEmpty() ? "All" : query));
            if (matching.isEmpty()) {
                System.out.println("No matching records.");
            }
            for (T record : page(matching, currentPage)) {
                System.out.println(renderer.apply(record));
            }
            ConsoleTheme.prompt("N-Next, P-Previous, F-Filter, R-Reset, page number, Enter/0-Back: ");
            String command = scanner.nextLine().trim();
            if (command.isEmpty() || command.equals("0") || command.equalsIgnoreCase("/cancel")) {
                onNavigation.accept("BACK");
                return;
            }
            if (command.equalsIgnoreCase("n")) {
                onNavigation.accept("NEXT_PAGE");
                if (currentPage < pages) {
                    currentPage++;
                } else {
                    ConsoleTheme.warning("No next page.");
                }
            } else if (command.equalsIgnoreCase("p")) {
                onNavigation.accept("PREVIOUS_PAGE");
                if (currentPage > 1) {
                    currentPage--;
                } else {
                    ConsoleTheme.warning("No previous page.");
                }
            } else if (command.equalsIgnoreCase("f")) {
                query = util.InputHelper.readLine(scanner, "Search text or codes (space-separated terms): ").trim();
                currentPage = 1;
                onNavigation.accept("FILTER");
            } else if (command.equalsIgnoreCase("r")) {
                query = "";
                currentPage = 1;
                onNavigation.accept("RESET_FILTER");
            } else {
                try {
                    int requestedPage = Integer.parseInt(command);
                    if (requestedPage < 1 || requestedPage > pages) {
                        onNavigation.accept("INVALID_PAGE");
                        ConsoleTheme.warning("Choose an existing page.");
                    } else {
                        currentPage = requestedPage;
                        onNavigation.accept("SELECT_PAGE_" + requestedPage);
                    }
                } catch (NumberFormatException e) {
                    onNavigation.accept("INVALID_NAVIGATION");
                    ConsoleTheme.warning("Choose a page number or a displayed command.");
                }
            }
        }
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
