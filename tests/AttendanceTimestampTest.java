import static support.Checks.*;
import exception.DataValidationException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import model.*;
import repository.AttendanceRepository;
import service.*;
import util.CsvHelper;
import util.DateTimeHelper;

public class AttendanceTimestampTest {
    private static AttendanceRecord record(String id, String timestamp) {
        return new AttendanceRecord(id, "W1", "Z1", timestamp, AttendanceRecord.CheckType.CHECK_IN);
    }

    public static void main(String[] args) throws Exception {
        test("Shared timestamp format preserves leap day and seconds", () -> {
            LocalDateTime date = LocalDateTime.of(2024, 2, 29, 7, 5, 9);
            equal("2024-02-29 07:05:09", DateTimeHelper.formatTimestamp(date));
            equal(date, DateTimeHelper.parseTimestamp("2024-02-29 07:05:09"));
        });
        test("Only the exact timestamp format is accepted", () -> {
            String[] invalid = {null, "", "29/10/2026 07:05", "29/10/2026 07:05:00",
                "2026-10-29 07:05", "2026-10-29T07:05:00", "2026-10-29 07:05:00.123",
                "2026-10-29 07:05:00Z", " 2026-10-29 07:05:00", "2026-10-29 07:05:00 ",
                "2026-1-29 07:05:00"};
            for (String value : invalid)
                throwsType(DateTimeParseException.class, () -> DateTimeHelper.parseTimestamp(value));
        });
        test("Invalid calendar dates and clock values are rejected", () -> {
            for (String value : Arrays.asList("2026-02-29 07:05:00", "2026-02-31 07:05:00",
                    "2026-10-29 24:00:00", "2026-10-29 07:05:60", "0000-10-29 07:05:00"))
                throwsType(DateTimeParseException.class, () -> DateTimeHelper.parseTimestamp(value));
        });
        test("Legacy timestamp reload fails without replacing loaded records", () -> {
            AttendanceRepository repo = new AttendanceRepository();
            require(repo.save(record("REC1", "2026-10-29 07:05:00")));
            Path csv = Files.createTempFile("smartsite-old-time-", ".csv");
            try {
                require(CsvHelper.writeAllLines(csv.toString(), Arrays.asList(
                    "recordId,personCode,zoneId,timestamp,checkType",
                    "REC0002,W1,Z1,29/10/2026 07:06,CHECK_OUT")));
                require(!repo.loadFromCsv(csv.toString()));
                equal(1, repo.findAll().size());
                equal("REC1", repo.findAll().get(0).getRecordId());
            } finally { Files.deleteIfExists(csv); }
        });
        test("Attendance model rejects legacy timestamps at construction", () -> {
            throwsType(DataValidationException.class, () -> record("REC1", "29/10/2026 07:05"));
        });
        test("Invalid replacement cannot change an immutable attendance event or its CSV", () -> {
            AttendanceRepository repo = new AttendanceRepository();
            AttendanceRecord record = record("REC1", "2026-10-29 07:05:00");
            require(repo.save(record));
            Path csv = Files.createTempFile("smartsite-time-write-", ".csv");
            try {
                require(repo.saveToCsv(csv.toString()));
                byte[] before = Files.readAllBytes(csv);
                throwsType(DataValidationException.class, () -> record("REC1", "29/10/2026 07:05"));
                equal("2026-10-29 07:05:00", record.getTimestamp());
                require(repo.saveToCsv(csv.toString()));
                require(Arrays.equals(before, Files.readAllBytes(csv)));
            } finally { Files.deleteIfExists(csv); }
        });
        test("Attendance service generates the same format accepted by CSV loader", () -> {
            PersonnelService people = new PersonnelService();
            ZoneService zones = new ZoneService();
            people.addPerson(new Worker("P1", "W1", "Worker", "0901234567", "Welder", "X_SAFE", "A"));
            zones.addZone(new Zone("Z1", "Regular"));
            AttendanceService service = new AttendanceService(people, zones);
            require(service.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN));
            String timestamp = service.getAllAttendanceRecords().get(0).getTimestamp();
            equal(timestamp, DateTimeHelper.formatTimestamp(DateTimeHelper.parseTimestamp(timestamp)));
            Path csv = Files.createTempFile("smartsite-time-service-", ".csv");
            try {
                require(service.saveToCsv(csv.toString()));
                AttendanceRepository loaded = new AttendanceRepository();
                require(loaded.loadFromCsv(csv.toString()));
                equal(timestamp, loaded.findAll().get(0).getTimestamp());
            } finally { Files.deleteIfExists(csv); }
        });
        test("All attendance fixture timestamps already use the single supported format", () -> {
            List<String[]> rows = CsvHelper.readRows("data/attendance.csv");
            equal(16, rows.size());
            for (int i = 1; i < rows.size(); i++) {
                String value = rows.get(i)[3];
                equal(value, DateTimeHelper.formatTimestamp(DateTimeHelper.parseTimestamp(value)));
            }
        });
        finish();
    }
}
