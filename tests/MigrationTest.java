import static support.Checks.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Account;
import model.Permission;
import repository.AccountRepository;
import repository.AttendanceRepository;
import repository.IncidentRepository;
import repository.PersonRepository;
import util.CsvHelper;
import util.CsvMigration;
import util.PasswordHelper;

public class MigrationTest {
    private static final String ORIGINAL_HASH = PasswordHelper.hash("original-secret");

    private static Path legacySource(boolean accounts) throws IOException {
        Path source = Files.createTempDirectory("smartsite-migration-source-");
        write(source, "persons.csv",
                "type,id,code,name,phoneNumber,role,extra1,extra2,extra3",
                "WORKER,p001,w01,Worker,0901234567,WORKER,X_SAFE,Welder,Team A",
                "VISITOR,P002,V01,Visitor,0901234567,VISITOR,Survey,W01,"
        );
        write(source, "zones.csv",
                "type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes",
                "REGULAR,z01,Regular,NONE,NONE",
                "RESTRICTED,Z02,Restricted,HIGH,w001"
        );
        write(source, "attendance.csv",
                "recordId,personCode,zoneId,timestamp,checkType",
                "ATT-001,w01,z01,2026-10-12 07:30:00,CHECK_IN",
                "ATT-002,W01,Z01,2026-10-12 08:00:00,CHECK_OUT"
        );
        write(source, "incidents.csv",
                "incidentId,title,description,severity,status,assignedTo",
                CsvHelper.toCsvLine("INC-001", "Hazard", "Line one, \"quoted\"\nLine two", "HIGH", "ASSIGNED", "w01")
        );
        if (accounts) {
            write(source, "accounts.csv",
                    "username,passwordHash,role,enabled,permissions",
                    CsvHelper.toCsvLine("Admin", ORIGINAL_HASH, "ADMIN", "true", "MANAGE_ACCOUNTS"),
                    CsvHelper.toCsvLine("officer", ORIGINAL_HASH, "SAFETY_OFFICER", "true", "VIEW_INCIDENTS")
            );
        }
        return source;
    }

    private static void write(Path root, String name, String... rows) throws IOException {
        if (!CsvHelper.writeAllLines(root.resolve(name).toString(), Arrays.asList(rows))) {
            throw new IOException("Fixture write failed.");
        }
    }

    private static Path destination() throws IOException {
        return Files.createTempDirectory("smartsite-migration-destination-").resolve("converted");
    }

    private static Map<String, byte[]> snapshot(Path root) throws IOException {
        Map<String, byte[]> content = new LinkedHashMap<>();
        try (java.util.stream.Stream<Path> files = Files.list(root)) {
            for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                content.put(file.getFileName().toString(), Files.readAllBytes(file));
            }
        }
        return content;
    }

    private static void unchanged(Path root, Map<String, byte[]> snapshot) throws IOException {
        for (Map.Entry<String, byte[]> file : snapshot.entrySet()) {
            require(Arrays.equals(file.getValue(), Files.readAllBytes(root.resolve(file.getKey()))));
        }
    }

    public static void main(String[] args) throws Exception {
        test("Migration creates strict schemas and canonical references without modifying the source", () -> {
            Path source = legacySource(false);
            Map<String, byte[]> before = snapshot(source);
            Path target = destination();
            equal(target.toAbsolutePath(), CsvMigration.migrate(source, target));
            unchanged(source, before);
            PersonRepository people = new PersonRepository();
            IncidentRepository incidents = new IncidentRepository();
            AttendanceRepository attendance = new AttendanceRepository();
            require(people.loadFromCsv(target.resolve("persons.csv").toString()));
            require(incidents.loadFromCsv(target.resolve("incidents.csv").toString()));
            require(attendance.loadFromCsv(target.resolve("attendance.csv").toString()));
            equal("P1", people.findByCode("w01").getId());
            require(people.findByCode("W1").isActive());
            equal(10, CsvHelper.readRows(target.resolve("persons.csv").toString()).get(0).length);
            equal(9, CsvHelper.readRows(target.resolve("incidents.csv").toString()).get(0).length);
            equal("ATT1", attendance.findAll().get(0).getRecordId());
            equal("Z1", attendance.findAll().get(0).getZoneId());
            equal("W1", incidents.findById("INC1").getAssignedTo());
            equal("", incidents.findById("INC1").getReportedAt());
            equal("", incidents.findById("INC1").getResolvedAt());
            require(!incidents.findById("INC1").isArchived());
            equal("W1", CsvHelper.readRows(target.resolve("zones.csv").toString()).get(2)[4]);
        });
        test("Migration rejects invalid swipe cycles before creating output and preserves source text", () -> {
            Path source = legacySource(false);
            write(source, "attendance.csv", "recordId,personCode,zoneId,timestamp,checkType",
                    "ATT-001,w01,z01,2026-10-12 07:30:00,CHECK_IN",
                    "ATT-002,W01,Z02,2026-10-12 08:00:00,CHECK_IN");
            Map<String, byte[]> before = snapshot(source);
            Path target = destination();
            throwsType(IOException.class, () -> CsvMigration.migrate(source, target));
            require(!Files.exists(target));
            unchanged(source, before);
            equal("W01", CsvHelper.readRows(source.resolve("persons.csv").toString()).get(2)[7]);
            equal("Line one, \"quoted\"\nLine two",
                    CsvHelper.readRows(source.resolve("incidents.csv").toString()).get(1)[2]);
        });
        test("Existing password hashes and custom staff permissions survive migration", () -> {
            Path source = legacySource(true);
            Map<String, byte[]> before = snapshot(source);
            Path target = destination();
            CsvMigration.migrate(source, target);
            unchanged(source, before);
            AccountRepository accounts = new AccountRepository(target.resolve("accounts.csv"));
            accounts.load();
            Account admin = accounts.find("admin");
            equal(ORIGINAL_HASH, admin.getPasswordHash());
            require(PasswordHelper.verify("original-secret", admin.getPasswordHash()));
            equal(Account.Role.ADMIN.defaultPermissions(), admin.getPermissions());
            Account officer = accounts.find("officer");
            equal(java.util.EnumSet.of(Permission.VIEW_INCIDENTS), officer.getPermissions());
            equal("", officer.getPersonCode());
            equal(6, CsvHelper.readRows(target.resolve("accounts.csv").toString()).get(0).length);
        });
        test("Canonical alias collisions stop migration before any output is created", () -> {
            Path source = legacySource(false);
            List<String> rows = new ArrayList<>(CsvHelper.readAllLines(source.resolve("persons.csv").toString()));
            rows.add("STAFF,P003,W1,Duplicate,0901234567,STAFF,,Manager,");
            require(CsvHelper.writeAllLines(source.resolve("persons.csv").toString(), rows));
            Map<String, byte[]> before = snapshot(source);
            Path target = destination();
            throwsType(IOException.class, () -> CsvMigration.migrate(source, target));
            require(!Files.exists(target));
            unchanged(source, before);
        });
        test("An existing destination or destination within the source is rejected", () -> {
            Path source = legacySource(false);
            Path existing = Files.createTempDirectory("smartsite-existing-migration-");
            Path marker = existing.resolve("keep.txt");
            Files.write(marker, new byte[] { 1, 2, 3 });
            throwsType(IOException.class, () -> CsvMigration.migrate(source, existing));
            require(Arrays.equals(new byte[] { 1, 2, 3 }, Files.readAllBytes(marker)));
            throwsType(IOException.class, () -> CsvMigration.migrate(source, source.resolve("converted")));
            require(!Files.exists(source.resolve("converted")));
        });
        test("Modern schemas retain active status, incident dates and linked account identity", () -> {
            Path source = legacySource(true);
            Path first = destination();
            CsvMigration.migrate(source, first);
            List<String[]> people = CsvHelper.readRows(first.resolve("persons.csv").toString());
            people.get(1)[9] = "false";
            List<String> personLines = new ArrayList<>();
            for (String[] row : people) {
                personLines.add(CsvHelper.toCsvLine(row));
            }
            require(CsvHelper.writeAllLines(first.resolve("persons.csv").toString(), personLines));
            write(first, "incidents.csv",
                    "incidentId,title,description,severity,status,assignedTo,reportedAt,resolvedAt,archived",
                    "INC-001,Hazard,Resolved,HIGH,RESOLVED,W01,2026-10-12 09:00:00,2026-10-12 10:00:00,true"
            );
            List<String> accounts = CsvHelper.readAllLines(first.resolve("accounts.csv").toString());
            accounts.add(CsvHelper.toCsvLine(
                    "worker", ORIGINAL_HASH, "SITE_USER", "true", "VIEW_OWN_ATTENDANCE", "w001"
            ));
            require(CsvHelper.writeAllLines(first.resolve("accounts.csv").toString(), accounts));
            Path second = destination();
            CsvMigration.migrate(first, second);
            PersonRepository personRepository = new PersonRepository();
            require(personRepository.loadFromCsv(second.resolve("persons.csv").toString()));
            require(!personRepository.findByCode("W1").isActive());
            IncidentRepository incidents = new IncidentRepository();
            require(incidents.loadFromCsv(second.resolve("incidents.csv").toString()));
            equal("2026-10-12 09:00:00", incidents.findById("INC1").getReportedAt());
            equal("2026-10-12 10:00:00", incidents.findById("INC1").getResolvedAt());
            require(incidents.findById("INC1").isArchived());
            AccountRepository accountRepository = new AccountRepository(second.resolve("accounts.csv"));
            accountRepository.load();
            equal("W1", accountRepository.find("worker").getPersonCode());
            equal(ORIGINAL_HASH, accountRepository.find("worker").getPasswordHash());
        });
        test("Unknown references are rejected before producing a migration folder", () -> {
            Path source = legacySource(false);
            write(source, "zones.csv", "type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes",
                    "RESTRICTED,Z1,Restricted,HIGH,W999");
            Path target = destination();
            throwsType(IOException.class, () -> CsvMigration.migrate(source, target));
            require(!Files.exists(target));
        });
        finish();
    }
}
