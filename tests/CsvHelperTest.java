import static support.Checks.*;
import java.nio.file.*;
import java.util.*;
import util.CsvHelper;
public class CsvHelperTest {
    public static void main(String[] args) throws Exception {
        test("CSV commas quotes and trailing empties", () -> {
            String[] values = {"id", "Cap dut, nguy hiem! \"gap\"", ""};
            require(Arrays.equals(values, CsvHelper.parseLine(CsvHelper.toCsvLine(values))));
        });
        test("CSV UTF8 BOM multiline round trip", () -> {
            Path file = Files.createTempFile("smartsite-csv", ".csv");
            try {
                String value = "Tiếng Việt\r\nLine 2\nLine 3";
                require(CsvHelper.writeAllLines(file.toString(), Arrays.asList("id,text", CsvHelper.toCsvLine("1", value))));
                equal((byte)0xEF, Files.readAllBytes(file)[0]);
                equal(value, CsvHelper.readRows(file.toString()).get(1)[1]);
            } finally { Files.deleteIfExists(file); }
        });
        test("Malformed CSV fails visibly", () -> {
            throwsType(IllegalArgumentException.class, () -> CsvHelper.parseLine("1,\"unfinished"));
            throwsType(IllegalArgumentException.class, () -> CsvHelper.parseLine("1,\"ok\"bad"));
        });
        test("Failed write preserves original", () -> {
            Path file = Files.createTempFile("smartsite-original", ".csv");
            try {
                Files.write(file, new byte[]{42});
                require(!CsvHelper.writeAllLines(file.toString(), Arrays.asList("changed", null)));
                equal((byte)42, Files.readAllBytes(file)[0]);
                require(!CsvHelper.writeAllLines(file.resolve("child").toString(), Arrays.asList("bad")));
            } finally { Files.deleteIfExists(file); }
        });
        finish();
    }
}
