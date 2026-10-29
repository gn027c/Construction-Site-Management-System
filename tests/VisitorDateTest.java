import static support.Checks.*;
import java.nio.file.*;
import exception.DataValidationException;
import java.util.*;
import controller.SiteManagerController;
import model.*;
import repository.PersonRepository;
import util.CsvHelper;

public class VisitorDateTest {
    private static final String HEADER = "type,id,code,name,phoneNumber,role,extra1,extra2,extra3";
    private static Visitor visitor(String date) {
        return new Visitor("P", "V", "Visitor", "0901234567", "Survey", "W", date);
    }

    public static void main(String[] args) throws Exception {
        test("Visitor accepts a real leap-day date", () -> equal("29/02/2024", visitor("29/02/2024").getEntryDate()));
        test("Visitor constructor rejects impossible dates and alternate formats", () -> {
            for (String value : Arrays.asList("31/02/2026", "2026-10-29", "29/10/2026 07:05:00", "Unknown (legacy)"))
                throwsType(DataValidationException.class, () -> visitor(value));
            throwsType(DataValidationException.class, () -> visitor(null));
        });
        test("Visitor date setter preserves its old value on invalid input", () -> {
            Visitor value = visitor("29/10/2026");
            throwsType(DataValidationException.class, () -> value.setEntryDate("29/02/2026"));
            equal("29/10/2026", value.getEntryDate());
            throwsType(DataValidationException.class, () -> value.setEntryDate(" 29/10/2026 "));
            equal("29/10/2026", value.getEntryDate());
        });
        test("Unknown visitor dates round trip as an explicit empty CSV cell", () -> {
            PersonRepository repo = new PersonRepository();
            require(repo.save(visitor("")));
            Path csv = Files.createTempFile("smartsite-unknown-visit-", ".csv");
            try {
                require(repo.saveToCsv(csv.toString()));
                equal("", CsvHelper.readRows(csv.toString()).get(1)[8]);
                PersonRepository loaded = new PersonRepository();
                require(loaded.loadFromCsv(csv.toString()));
                equal("", ((Visitor) loaded.findByCode("V")).getEntryDate());
            } finally { Files.deleteIfExists(csv); }
        });
        test("Invalid visitor dates in CSV cannot replace existing personnel", () -> {
            PersonRepository repo = new PersonRepository();
            require(repo.save(visitor("29/10/2026")));
            Path csv = Files.createTempFile("smartsite-invalid-visit-", ".csv");
            try {
                require(CsvHelper.writeAllLines(csv.toString(), Arrays.asList(HEADER,
                    "VISITOR,P,V,Visitor,0901234567,VISITOR,Survey,W,31/02/2026")));
                require(!repo.loadFromCsv(csv.toString()));
                equal("29/10/2026", ((Visitor) repo.findByCode("V")).getEntryDate());
            } finally { Files.deleteIfExists(csv); }
        });
        test("Old personnel schemas no longer supply fabricated visitor dates", () -> {
            PersonRepository repo = new PersonRepository();
            Path csv = Files.createTempFile("smartsite-old-person-schema-", ".csv");
            try {
                require(CsvHelper.writeAllLines(csv.toString(), Arrays.asList(HEADER,
                    "VISITOR,P,V,Visitor,0901234567,VISITOR,Survey,W")));
                require(!repo.loadFromCsv(csv.toString()));
                require(CsvHelper.writeAllLines(csv.toString(), Arrays.asList(
                    "type,id,code,name,phoneNumber,role,extra1,extra2",
                    "VISITOR,P,V,Visitor,0901234567,VISITOR,Survey,W,29/10/2026")));
                require(!repo.loadFromCsv(csv.toString()));
            } finally { Files.deleteIfExists(csv); }
        });
        test("All personnel fixture rows use nine columns and preserve unknown dates", () -> {
            List<String[]> rows = CsvHelper.readRows("data/persons.csv");
            equal(11, rows.size());
            for (String[] row : rows) equal(9, row.length);
            PersonRepository repo = new PersonRepository();
            require(repo.loadFromCsv("data/persons.csv"));
            equal("", ((Visitor) repo.findByCode("V01")).getEntryDate());
            equal("", ((Visitor) repo.findByCode("V02")).getEntryDate());
        });
        test("Visitor console input, CSV save and export share the validated date", () -> {
            Path root = Files.createTempDirectory("smartsite-visit-console-");
            for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv"))
                Files.copy(Paths.get("data", file), root.resolve(file));
            SiteManagerController controller = new SiteManagerController(root);
            controller.loadAllData();
            controller.handlePersonMenu(new Scanner("1\n3\nP11\nV3\nVisitor 3\n0901234567\nSurvey\nW01\n31/02/2026\n29/10/2026\n0\n"));
            PersonRepository saved = new PersonRepository();
            require(saved.loadFromCsv(root.resolve("persons.csv").toString()));
            equal("29/10/2026", ((Visitor) saved.findByCode("V3")).getEntryDate());
            Path report = controller.exportCsv(root.resolve("reports"));
            for (String[] row : CsvHelper.readRows(report.resolve("persons.csv").toString()))
                if (row[2].equals("V3")) equal("29/10/2026", row[8]);
            equal(12, CsvHelper.readRows(report.resolve("persons.csv").toString()).size());
        });
        finish();
    }
}
