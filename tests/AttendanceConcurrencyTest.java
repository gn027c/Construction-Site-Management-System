import static support.Checks.*;

import exception.AttendanceOrderException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import model.attendance.AttendanceRecord;
import model.person.Worker;
import model.zone.Zone;
import service.AttendanceService;
import service.PersonnelService;
import service.ZoneService;

public class AttendanceConcurrencyTest {
    private static AttendanceService service() {
        PersonnelService people = new PersonnelService();
        people.addPerson(new Worker("P1", "W1", "Worker", "0901234567", "Welder", "CERT_SAFE", "Team"));
        ZoneService zones = new ZoneService();
        zones.addZone(new Zone("Z1", "First zone"));
        zones.addZone(new Zone("Z2", "Second zone"));
        return new AttendanceService(people, zones);
    }

    private static List<String> race(Callable<Boolean> first, Callable<Boolean> second) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> pending = new ArrayList<>();
        try {
            for (Callable<Boolean> operation : java.util.Arrays.asList(first, second)) {
                pending.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS)) {
                        throw new AssertionError("Race start timeout.");
                    }
                    try {
                        return operation.call() ? "SAVED" : "REJECTED";
                    } catch (AttendanceOrderException e) {
                        return "DENIED";
                    }
                }));
            }
            require(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            List<String> results = new ArrayList<>();
            for (Future<String> future : pending) {
                results.add(future.get(5, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            start.countDown();
            executor.shutdownNow();
            require(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private static void oneAccepted(List<String> results) {
        equal(1L, results.stream().filter("SAVED"::equals).count());
        equal(1L, results.stream().filter("DENIED"::equals).count());
    }

    public static void main(String[] args) throws Exception {
        test("Concurrent check-ins at different zones cannot both enter the ledger", () -> {
            AttendanceService attendance = service();
            oneAccepted(race(
                    () -> attendance.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN),
                    () -> attendance.recordAttendance("w01", "Z2", AttendanceRecord.CheckType.CHECK_IN)
            ));
            equal(1, attendance.getAllAttendanceRecords().size());
            equal("REC1", attendance.getAllAttendanceRecords().get(0).getRecordId());
        });
        test("Concurrent persisted check-ins store exactly one swipe and reload its identifier", () -> {
            AttendanceService attendance = service();
            Path file = Files.createTempDirectory("smartsite-swipe-race-").resolve("attendance.csv");
            oneAccepted(race(
                    () -> attendance.recordAttendanceAndSave(
                            "W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, file.toString()
                    ),
                    () -> attendance.recordAttendanceAndSave(
                            "w001", "Z2", AttendanceRecord.CheckType.CHECK_IN, file.toString()
                    )
            ));
            AttendanceService restarted = service();
            require(restarted.loadFromCsv(file.toString()));
            equal(1, restarted.getAllAttendanceRecords().size());
            AttendanceRecord checkedIn = restarted.getAllAttendanceRecords().get(0);
            restarted.recordAttendance("W1", checkedIn.getZoneId(), AttendanceRecord.CheckType.CHECK_OUT);
            equal("REC2", restarted.getAllAttendanceRecords().get(1).getRecordId());
        });
        test("Concurrent check-outs cannot close the same open visit twice", () -> {
            AttendanceService attendance = service();
            attendance.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN);
            oneAccepted(race(
                    () -> attendance.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_OUT),
                    () -> attendance.recordAttendance("w01", "z01", AttendanceRecord.CheckType.CHECK_OUT)
            ));
            equal(2, attendance.getAllAttendanceRecords().size());
            equal(AttendanceRecord.CheckType.CHECK_OUT, attendance.getAllAttendanceRecords().get(1).getCheckType());
        });
        finish();
    }
}
