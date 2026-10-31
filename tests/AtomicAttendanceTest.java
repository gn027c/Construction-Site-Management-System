import static support.Checks.*;

import exception.AttendanceOrderException;
import exception.SiteManagementException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import model.attendance.AttendanceRecord;
import model.person.Worker;
import model.zone.Zone;
import repository.AttendanceRepository;
import service.AttendanceService;
import service.PersonnelService;
import service.ZoneService;
import util.CsvHelper;

public class AtomicAttendanceTest {
    private static AttendanceRecord event(String id, String zone, String time, boolean entering) {
        return new AttendanceRecord(
                id, "W1", zone, "2026-10-12 " + time,
                entering ? AttendanceRecord.CheckType.CHECK_IN : AttendanceRecord.CheckType.CHECK_OUT
        );
    }

    private static AttendanceService service() {
        PersonnelService people = new PersonnelService();
        people.addPerson(new Worker("P1", "W1", "Worker", "0901234567", "Welder", "X_SAFE", "Team"));
        ZoneService zones = new ZoneService();
        zones.addZone(new Zone("Z1", "First zone"));
        zones.addZone(new Zone("Z2", "Second zone"));
        return new AttendanceService(people, zones);
    }

    private static void unchanged(Path file, byte[] before, AttendanceRepository repository, int count) throws Exception {
        require(Arrays.equals(before, Files.readAllBytes(file)));
        equal(count, repository.findAll().size());
    }

    public static void main(String[] args) throws Exception {
        test("Checkout without an entry cannot create memory or CSV data", () -> {
            AttendanceRepository repository = new AttendanceRepository();
            Path file = Files.createTempDirectory("smartsite-atomic-first-").resolve("attendance.csv");
            throwsType(AttendanceOrderException.class, () -> repository.saveAndPersist(
                    event("REC1", "Z1", "07:00:00", false), file.toString()
            ));
            require(repository.findAll().isEmpty());
            require(!Files.exists(file));
        });
        test("Repeated and cross-zone entries leave committed memory and file bytes unchanged", () -> {
            AttendanceRepository repository = new AttendanceRepository();
            Path file = Files.createTempFile("smartsite-atomic-entry-", ".csv");
            require(repository.saveAndPersist(event("REC1", "Z1", "07:00:00", true), file.toString()));
            byte[] before = Files.readAllBytes(file);
            for (String zone : Arrays.asList("Z1", "Z2")) {
                throwsType(AttendanceOrderException.class, () -> repository.saveAndPersist(
                        event("REC2", zone, "08:00:00", true), file.toString()
                ));
                unchanged(file, before, repository, 1);
            }
        });
        test("Wrong-zone and repeated exits are rejected before changing either representation", () -> {
            AttendanceRepository repository = new AttendanceRepository();
            Path file = Files.createTempFile("smartsite-atomic-exit-", ".csv");
            require(repository.saveAndPersist(event("REC1", "Z1", "07:00:00", true), file.toString()));
            byte[] before = Files.readAllBytes(file);
            throwsType(AttendanceOrderException.class, () -> repository.saveAndPersist(
                    event("REC2", "Z2", "08:00:00", false), file.toString()
            ));
            unchanged(file, before, repository, 1);
            require(repository.saveAndPersist(event("REC2", "Z1", "08:00:00", false), file.toString()));
            byte[] checkedOut = Files.readAllBytes(file);
            throwsType(AttendanceOrderException.class, () -> repository.saveAndPersist(
                    event("REC3", "Z1", "09:00:00", false), file.toString()
            ));
            unchanged(file, checkedOut, repository, 2);
        });
        test("Chronology validation also protects callers that bypass the service", () -> {
            AttendanceRepository repository = new AttendanceRepository();
            Path file = Files.createTempFile("smartsite-atomic-time-", ".csv");
            require(repository.saveAndPersist(event("REC1", "Z1", "08:00:00", true), file.toString()));
            byte[] before = Files.readAllBytes(file);
            throwsType(AttendanceOrderException.class, () -> repository.saveAndPersist(
                    event("REC2", "Z1", "07:00:00", false), file.toString()
            ));
            unchanged(file, before, repository, 1);
        });
        test("Direct in-memory saves cannot bypass the same attendance rules", () -> {
            AttendanceRepository repository = new AttendanceRepository();
            require(repository.save(event("REC1", "Z1", "07:00:00", true)));
            throwsType(AttendanceOrderException.class, () -> repository.save(event("REC2", "Z2", "08:00:00", true)));
            equal(1, repository.findAll().size());
        });
        test("Invalid CSV transitions reject the entire staged load and retain active records", () -> {
            AttendanceRepository repository = new AttendanceRepository();
            require(repository.save(event("REC9", "Z1", "06:00:00", true)));
            Path file = Files.createTempFile("smartsite-atomic-load-", ".csv");
            require(CsvHelper.writeAllLines(file.toString(), Arrays.asList(
                    "recordId,personCode,zoneId,timestamp,checkType",
                    event("REC1", "Z1", "07:00:00", true).toCsvLine(),
                    event("REC2", "Z2", "08:00:00", true).toCsvLine()
            )));
            byte[] before = Files.readAllBytes(file);
            require(!repository.loadFromCsv(file.toString()));
            unchanged(file, before, repository, 1);
            equal("REC9", repository.findAll().get(0).getRecordId());
            require(repository.getLastLoadError().contains("row 3 / REC2"));
        });
        test("A failed file replacement does not publish a staged event or consume its sequence", () -> {
            AttendanceService attendance = service();
            Path directory = Files.createTempDirectory("smartsite-atomic-write-fail-");
            Path blocked = directory.resolve("blocked.csv");
            Files.createDirectory(blocked);
            Files.write(blocked.resolve("keep.txt"), Arrays.asList("preserve"));
            throwsType(SiteManagementException.class, () -> attendance.recordAttendanceAndSave(
                    "W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, blocked.toString()
            ));
            require(attendance.getAllAttendanceRecords().isEmpty());
            equal(Arrays.asList("preserve"), Files.readAllLines(blocked.resolve("keep.txt")));
            Path valid = directory.resolve("attendance.csv");
            require(attendance.recordAttendanceAndSave("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, valid.toString()));
            equal("REC1", attendance.getAllAttendanceRecords().get(0).getRecordId());
        });
        test("Rejected transitions do not consume identifiers and remain rejected after restart", () -> {
            AttendanceService attendance = service();
            Path file = Files.createTempFile("smartsite-atomic-sequence-", ".csv");
            require(attendance.recordAttendanceAndSave("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, file.toString()));
            byte[] before = Files.readAllBytes(file);
            throwsType(AttendanceOrderException.class, () -> attendance.recordAttendanceAndSave(
                    "w01", "Z2", AttendanceRecord.CheckType.CHECK_IN, file.toString()
            ));
            require(Arrays.equals(before, Files.readAllBytes(file)));
            AttendanceService restarted = service();
            require(restarted.loadFromCsv(file.toString()));
            throwsType(AttendanceOrderException.class, () -> restarted.recordAttendanceAndSave(
                    "W1", "Z2", AttendanceRecord.CheckType.CHECK_OUT, file.toString()
            ));
            require(restarted.recordAttendanceAndSave("W1", "Z1", AttendanceRecord.CheckType.CHECK_OUT, file.toString()));
            equal("REC2", restarted.getAllAttendanceRecords().get(1).getRecordId());
        });
        test("Competing repository writers validate and commit under the same lock", () -> {
            AttendanceRepository repository = new AttendanceRepository();
            Path file = Files.createTempFile("smartsite-atomic-race-", ".csv");
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch start = new CountDownLatch(1);
            try {
                java.util.List<Future<Boolean>> results = new java.util.ArrayList<>();
                for (int i = 1; i <= 2; i++) {
                    final int number = i;
                    results.add(executor.submit(() -> {
                        require(start.await(5, TimeUnit.SECONDS));
                        try {
                            return repository.saveAndPersist(
                                    event("REC" + number, "Z" + number, "07:00:00", true), file.toString()
                            );
                        } catch (AttendanceOrderException e) {
                            return false;
                        }
                    }));
                }
                start.countDown();
                int accepted = 0;
                for (Future<Boolean> result : results) {
                    if (result.get(5, TimeUnit.SECONDS)) {
                        accepted++;
                    }
                }
                equal(1, accepted);
                equal(1, repository.findAll().size());
                equal(2, CsvHelper.readRows(file.toString()).size());
            } finally {
                start.countDown();
                executor.shutdownNow();
                require(executor.awaitTermination(5, TimeUnit.SECONDS));
            }
        });
        test("Duplicate IDs cannot replace a committed event", () -> {
            AttendanceRepository repository = new AttendanceRepository();
            Path file = Files.createTempFile("smartsite-atomic-duplicate-", ".csv");
            require(repository.saveAndPersist(event("REC1", "Z1", "07:00:00", true), file.toString()));
            byte[] before = Files.readAllBytes(file);
            require(!repository.saveAndPersist(event("REC1", "Z1", "08:00:00", false), file.toString()));
            unchanged(file, before, repository, 1);
        });
        finish();
    }
}
