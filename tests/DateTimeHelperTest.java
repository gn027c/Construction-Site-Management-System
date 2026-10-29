import static support.Checks.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import util.DateTimeHelper;
import util.InputHelper;

public class DateTimeHelperTest {
    public static void main(String[] args) throws Exception {
        test("Date-only format round trips valid leap days", () -> {
            LocalDate value = LocalDate.of(2024, 2, 29);
            equal("29/02/2024", DateTimeHelper.formatDate(value));
            equal(value, DateTimeHelper.parseDate("29/02/2024"));
        });
        test("Date-only parsing rejects invalid dates and alternate formats", () -> {
            String[] invalid = {null, "", "29/02/2026", "31/04/2026", "00/10/2026", "29/10/0000",
                "2026-10-29", "1/10/2026", "29/10/2026 00:00:00", "29/10/2026 junk", " 29/10/2026"};
            for (String value : invalid)
                throwsType(DateTimeParseException.class, () -> DateTimeHelper.parseDate(value));
        });
        test("Date input retries an impossible date and returns a trimmed valid date", () -> {
            try (Scanner scanner = new Scanner("31/02/2026\n 29/10/2026 \n")) {
                equal("29/10/2026", InputHelper.getDate(scanner, "Date: "));
            }
        });
        test("Formatters consistently reject null values and unsupported years", () -> {
            throwsType(NullPointerException.class, () -> DateTimeHelper.formatDate(null));
            throwsType(NullPointerException.class, () -> DateTimeHelper.formatTimestamp(null));
            throwsType(IllegalArgumentException.class, () -> DateTimeHelper.formatDate(LocalDate.of(0, 1, 1)));
            throwsType(IllegalArgumentException.class, () -> DateTimeHelper.formatDate(LocalDate.of(10000, 1, 1)));
            throwsType(IllegalArgumentException.class, () -> DateTimeHelper.formatTimestamp(LocalDateTime.of(10000, 1, 1, 0, 0)));
        });
        finish();
    }
}
