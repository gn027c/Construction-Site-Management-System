import static support.Checks.*;
import java.nio.file.*;
import exception.DataValidationException;
import java.util.*;
import controller.SiteManagerController;
import model.person.*;
import model.zone.*;
import model.attendance.*;
import model.incident.*;
import model.auth.*;
import model.common.*;
import repository.PersonRepository;
import util.CsvHelper;

public class VisitorDateTest {
    private static final String HEADER = "type,id,code,name,phoneNumber,role,extra1,extra2,extra3,active,safetyLevel";
    private static Visitor visitor(String date) {
        return new Visitor("P1", "V1", "Visitor", "0901234567", "Survey", "W1", date);
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
                equal("", ((Visitor) loaded.findByCode("V1")).getEntryDate());
            } finally { Files.deleteIfExists(csv); }
        });
        test("Invalid visitor dates in CSV cannot replace existing personnel", () -> {
            PersonRepository repo = new PersonRepository();
            require(repo.save(visitor("29/10/2026")));
            Path csv = Files.createTempFile("smartsite-invalid-visit-", ".csv");
            try {
                require(CsvHelper.writeAllLines(csv.toString(), Arrays.asList(HEADER,
                    "VISITOR,P1,V1,Visitor,0901234567,VISITOR,Survey,W1,31/02/2026,true")));
                require(!repo.loadFromCsv(csv.toString()));
                equal("29/10/2026", ((Visitor) repo.findByCode("V1")).getEntryDate());
            } finally { Files.deleteIfExists(csv); }
        });
        test("Old personnel schemas no longer supply fabricated visitor dates", () -> {
            PersonRepository repo = new PersonRepository();
            Path csv = Files.createTempFile("smartsite-old-person-schema-", ".csv");
            try {
                require(CsvHelper.writeAllLines(csv.toString(), Arrays.asList(HEADER,
                    "VISITOR,P1,V1,Visitor,0901234567,VISITOR,Survey,W")));
                require(!repo.loadFromCsv(csv.toString()));
                require(CsvHelper.writeAllLines(csv.toString(), Arrays.asList(
                    "type,id,code,name,phoneNumber,role,extra1,extra2",
                    "VISITOR,P1,V1,Visitor,0901234567,VISITOR,Survey,W1,29/10/2026")));
                require(!repo.loadFromCsv(csv.toString()));
            } finally { Files.deleteIfExists(csv); }
        });
        test("All personnel fixture rows use eleven columns and preserve unknown dates", () -> {
            List<String[]> rows = CsvHelper.readRows("data/persons.csv");
            require(rows.size() >= 11);
            for (String[] row : rows) equal(11, row.length);
            PersonRepository repo = new PersonRepository();
            require(repo.loadFromCsv("data/persons.csv"));
            equal("", ((Visitor) repo.findByCode("V01")).getEntryDate());
            equal("", ((Visitor) repo.findByCode("V02")).getEntryDate());
        });
        test("Visitor console input, CSV save and export share the validated date", () -> {
            Path root = Files.createTempDirectory("smartsite-visit-console-");
            for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv"))
                Files.copy(Paths.get("data", file), root.resolve(file));
            Files.write(root.resolve("attendance.csv"), Arrays.asList("recordId,personCode,zoneId,timestamp,checkType"));
            SiteManagerController controller = new SiteManagerController(root);
            controller.getAuthentication().login("admin", "admin");
            controller.loadAllData();
            int initialCount = controller.people().size();
            controller.handlePersonMenu(new Scanner("1\n3\nVisitor 3\n0901234567\nSurvey\nW01\n31/02/2026\n29/10/2026\n0\n"));
            PersonRepository saved = new PersonRepository();
            require(saved.loadFromCsv(root.resolve("persons.csv").toString()));
            equal("29/10/2026", ((Visitor) saved.findByCode("V3")).getEntryDate());
            Path report = controller.exportCsv(root.resolve("reports"));
            for (String[] row : CsvHelper.readRows(report.resolve("persons.csv").toString()))
                if (row[2].equals("V3")) equal("29/10/2026", row[8]);
            equal(initialCount + 2, CsvHelper.readRows(report.resolve("persons.csv").toString()).size());
        });
        test("Visitors check in only on the booked visit date", () -> {
            String today = util.DateTimeHelper.formatDate(java.time.LocalDate.now());
            String yesterday = util.DateTimeHelper.formatDate(java.time.LocalDate.now().minusDays(1));
            service.PersonnelService people = new service.PersonnelService();
            service.ZoneService zones = new service.ZoneService();
            zones.addZone(new Zone("Z1", "Gate"));
            Visitor booked = new Visitor("P1", "V1", "Booked", "0901234567", "Survey", "W1", today);
            Visitor late = new Visitor("P2", "V2", "Late", "0901234567", "Survey", "W1", yesterday);
            Visitor unknown = new Visitor("P3", "V3", "Unknown", "0901234567", "Survey", "W1", "");
            people.addPerson(booked);
            people.addPerson(late);
            people.addPerson(unknown);
            service.AttendanceService attendance = new service.AttendanceService(people, zones);
            require(attendance.recordAttendance("V1", "Z1", AttendanceRecord.CheckType.CHECK_IN));
            DataValidationException wrongDay = null;
            try {
                attendance.recordAttendance("V2", "Z1", AttendanceRecord.CheckType.CHECK_IN);
            } catch (DataValidationException e) {
                wrongDay = e;
            }
            require(wrongDay != null && wrongDay.getMessage().contains("Visit is booked for " + yesterday));
            throwsType(DataValidationException.class, () -> attendance.recordAttendance(
                    "V3", "Z1", AttendanceRecord.CheckType.CHECK_IN
            ));
            equal(1, attendance.getAllAttendanceRecords().size());
        });
        test("A visitor can always check out, even after the visit date changes", () -> {
            String today = util.DateTimeHelper.formatDate(java.time.LocalDate.now());
            service.PersonnelService people = new service.PersonnelService();
            service.ZoneService zones = new service.ZoneService();
            zones.addZone(new Zone("Z1", "Gate"));
            Visitor guest = new Visitor("P1", "V1", "Guest", "0901234567", "Survey", "W1", today);
            people.addPerson(guest);
            service.AttendanceService attendance = new service.AttendanceService(people, zones);
            require(attendance.recordAttendance("V1", "Z1", AttendanceRecord.CheckType.CHECK_IN));
            guest.setEntryDate("01/01/2020");
            require(attendance.recordAttendance("V1", "Z1", AttendanceRecord.CheckType.CHECK_OUT));
        });
        test("Editing a visitor records a new visit date in CSV", () -> {
            Path root = Files.createTempDirectory("smartsite-visit-edit-");
            for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv"))
                Files.copy(Paths.get("data", file), root.resolve(file));
            SiteManagerController site = new SiteManagerController(root);
            site.loadAllData();
            site.getAuthentication().login("admin", "admin");
            site.handlePersonMenu(new Scanner("2\ne\nV1\n\n\n31/02/2026\n15/11/2026\n0\n0\n"));
            PersonRepository repo = new PersonRepository();
            require(repo.loadFromCsv(root.resolve("persons.csv").toString()));
            equal("15/11/2026", ((Visitor) repo.findByCode("V1")).getEntryDate());
        });
        finish();
    }
}
