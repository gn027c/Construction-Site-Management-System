import static support.Checks.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import util.IdentifierHelper;
import view.ListView;

public class PagingTest {
    public static void main(String[] args) throws Exception {
        test("Natural identifier order places W2 before W10 and W100", () -> {
            List<String> rows = ListView.filterAndSort(
                    Arrays.asList("W100", "W10", "W2", "W1"), "", value -> value, IdentifierHelper.NATURAL_ORDER
            );
            equal(Arrays.asList("W1", "W2", "W10", "W100"), rows);
        });
        test("Identifier searches normalize zero padding and hyphens without matching a different identifier", () -> {
            List<String> rows = ListView.filterAndSort(
                    Arrays.asList("W1 | Nguyen Van An", "W10 | Tran Van Binh"),
                    "w-001", value -> value, Comparator.naturalOrder()
            );
            equal(1, rows.size());
            equal("W1 | Nguyen Van An", rows.get(0));
        });
        test("Text filters ignore case and combine space-separated terms", () -> {
            List<String> rows = ListView.filterAndSort(
                    Arrays.asList("W1 | Nguyen Van An | ACTIVE", "W2 | Nguyen Van Binh | INACTIVE"),
                    "NGUYEN An ACTIVE", value -> value, null
            );
            equal(Arrays.asList("W1 | Nguyen Van An | ACTIVE"), rows);
        });
        test("Pages contain ten records and return independent lists", () -> {
            List<Integer> rows = new ArrayList<>();
            for (int i = 1; i <= 21; i++) {
                rows.add(i);
            }
            equal(3, ListView.pageCount(rows.size()));
            equal(10, ListView.page(rows, 1).size());
            equal(Integer.valueOf(11), ListView.page(rows, 2).get(0));
            equal(Arrays.asList(21), ListView.page(rows, 3));
            ListView.page(rows, 1).clear();
            equal(21, rows.size());
            equal(0, ListView.page(rows, 4).size());
        });
        test("Empty lists and invalid page requests have explicit behavior", () -> {
            equal(0, ListView.pageCount(0));
            equal(0, ListView.page(Arrays.asList(), 1).size());
            throwsType(IllegalArgumentException.class, () -> ListView.page(Arrays.asList("W1"), 0));
            throwsType(IllegalArgumentException.class, () -> ListView.pageCount(-1));
        });
        test("View navigation supports next, previous, filtering, reset and invalid pages", () -> {
            List<String> rows = new ArrayList<>();
            for (int i = 1; i <= 12; i++) {
                rows.add("W" + i);
            }
            PrintStream original = System.out;
            ByteArrayOutputStream captured = new ByteArrayOutputStream();
            try {
                System.setOut(new PrintStream(captured, true, "UTF-8"));
                ListView.show(
                        new Scanner("n\np\nf\nw012\nr\n99\n0\n"), "Workers", rows,
                        value -> value, value -> value, IdentifierHelper.NATURAL_ORDER
                );
            } finally {
                System.setOut(original);
            }
            String output = captured.toString("UTF-8");
            require(output.contains("Page 2/2"));
            require(output.contains("Results: 1 | Filter: w012"));
            require(output.contains("Choose an existing page."));
            require(output.contains("Results: 12 | Filter: All"));
        });
        test("Filtering never adds records outside the already-authorized collection", () -> {
            List<String> scoped = Arrays.asList("W1 | Personal history");
            equal(0, ListView.filterAndSort(scoped, "W2", value -> value, null).size());
            equal(1, scoped.size());
        });
        test("Navigation callbacks identify actions without disclosing arbitrary filter text", () -> {
            List<String> actions = new ArrayList<>();
            PrintStream original = System.out;
            try {
                System.setOut(new PrintStream(new ByteArrayOutputStream()));
                ListView.show(
                        new Scanner("f\nsecret-like-filter\nr\n0\n"), "Rows", Arrays.asList("W1"),
                        value -> value, value -> value, null, actions::add
                );
            } finally {
                System.setOut(original);
            }
            equal(Arrays.asList("FILTER", "RESET_FILTER", "BACK"), actions);
        });
        finish();
    }
}
