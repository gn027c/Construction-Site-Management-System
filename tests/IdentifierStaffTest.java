import static support.Checks.*;

import exception.DataValidationException;
import exception.DuplicatePersonCodeException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import model.AttendanceRecord;
import model.RestrictedZone;
import model.Staff;
import model.Worker;
import model.Zone;
import repository.PersonRepository;
import service.AttendanceService;
import service.PersonnelService;
import service.ZoneService;
import util.IdentifierHelper;

public class IdentifierStaffTest {
    public static void main(String[] args) throws Exception {
        test("Equivalent identifier inputs have one canonical representation", () -> {
            for (String value : Arrays.asList("S1", "s01", " s-0001 ", "S001")) {
                equal("S1", IdentifierHelper.normalize(value));
            }
            equal("ATT1", IdentifierHelper.normalize("att-001"));
            equal("INC101", IdentifierHelper.normalize("inc-0101"));
            equal("REC100", IdentifierHelper.normalize("rec00100"));
            equal("P999999999999999999999999", IdentifierHelper.normalize("p999999999999999999999999"));
        });
        test("Zero, negative, fractional and malformed identifiers are rejected", () -> {
            for (String value : Arrays.asList("S0", "S000", "S--1", "S-", "1S", "S1.0", "S 1", "NONE", "")) {
                throwsType(DataValidationException.class, () -> IdentifierHelper.normalize(value));
            }
            equal(null, IdentifierHelper.normalizeForLookup("S0"));
        });
        test("Identifier order compares the numeric suffix rather than text", () -> {
            List<String> codes = new ArrayList<>(Arrays.asList("S100", "S10", "S2", "S1"));
            codes.sort(IdentifierHelper.NATURAL_ORDER);
            equal(Arrays.asList("S1", "S2", "S10", "S100"), codes);
        });
        test("Canonical aliases cannot create duplicate personnel or internal IDs", () -> {
            PersonRepository people = new PersonRepository();
            Worker first = new Worker("p001", "w01", "Worker", "0901234567", "Welder", "X_SAFE", "A");
            require(people.save(first));
            equal(first, people.findByCode("W-0001"));
            throwsType(DuplicatePersonCodeException.class, () -> people.save(new Staff(
                    "P2", "w-001", "Staff", "0901234567", "Supervisor", ""
            )));
            throwsType(DataValidationException.class, () -> people.save(new Staff(
                    "P0001", "S1", "Staff", "0901234567", "Supervisor", ""
            )));
        });
        test("Staff credentials are optional and independent of account roles", () -> {
            Staff staff = new Staff("p001", "s01", "Staff", "0901234567", "Manager", "");
            equal("P1", staff.getId());
            equal("S1", staff.getCode());
            require(!staff.hasValidSafetyCredential());
            staff.setSafetyCertId(" STAFF_SAFE ");
            require(staff.hasValidSafetyCredential());
            RestrictedZone zone = new RestrictedZone(
                    "z001", "Restricted", RestrictedZone.SafetyLevel.HIGH, new HashSet<>(Arrays.asList("s0001"))
            );
            require(zone.checkAccess(staff));
        });
        test("Staff fields and inactive status survive strict CSV round trips", () -> {
            PersonRepository source = new PersonRepository();
            Staff staff = new Staff("P1", "S1", "Staff", "0901234567", "Safety officer", "STAFF_SAFE");
            staff.setActive(false);
            source.save(staff);
            Path file = Files.createTempFile("staff-roundtrip-", ".csv");
            try {
                require(source.saveToCsv(file.toString()));
                PersonRepository target = new PersonRepository();
                require(target.loadFromCsv(file.toString()));
                Staff restored = (Staff) target.findByCode("s01");
                equal("Safety officer", restored.getPosition());
                equal("STAFF_SAFE", restored.getSafetyCertId());
                require(!restored.isActive());
                equal(10, util.CsvHelper.readRows(file.toString()).get(1).length);
            } finally {
                Files.deleteIfExists(file);
            }
        });
        test("Inactive personnel may check out but cannot start another visit", () -> {
            PersonnelService people = new PersonnelService();
            ZoneService zones = new ZoneService();
            Staff staff = new Staff("P1", "S1", "Staff", "0901234567", "Manager", "");
            people.addPerson(staff);
            zones.addZone(new Zone("Z1", "Regular"));
            AttendanceService attendance = new AttendanceService(people, zones);
            require(attendance.recordAttendance("s01", "z001", AttendanceRecord.CheckType.CHECK_IN));
            staff.setActive(false);
            require(attendance.recordAttendance("S1", "Z1", AttendanceRecord.CheckType.CHECK_OUT));
            throwsType(DataValidationException.class, () -> attendance.recordAttendance(
                    "S1", "Z1", AttendanceRecord.CheckType.CHECK_IN
            ));
            equal("REC1", attendance.getAllAttendanceRecords().get(0).getRecordId());
            equal("REC2", attendance.getAllAttendanceRecords().get(1).getRecordId());
        });
        test("Competing check-ins share one validation-and-save boundary", () -> {
            PersonnelService people = new PersonnelService();
            ZoneService zones = new ZoneService();
            people.addPerson(new Staff("P1", "S1", "Staff", "0901234567", "Manager", ""));
            zones.addZone(new Zone("Z1", "Regular"));
            AttendanceService attendance = new AttendanceService(people, zones);
            AtomicInteger succeeded = new AtomicInteger();
            AtomicInteger rejected = new AtomicInteger();
            CountDownLatch ready = new CountDownLatch(1);
            Runnable swipe = () -> {
                try {
                    ready.await();
                    if (attendance.recordAttendance("s01", "z01", AttendanceRecord.CheckType.CHECK_IN)) {
                        succeeded.incrementAndGet();
                    }
                } catch (exception.AttendanceOrderException e) {
                    rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };
            Thread first = new Thread(swipe);
            Thread second = new Thread(swipe);
            first.start();
            second.start();
            ready.countDown();
            first.join(3000);
            second.join(3000);
            require(!first.isAlive() && !second.isAlive());
            equal(1, succeeded.get());
            equal(1, rejected.get());
            equal(1, attendance.getAllAttendanceRecords().size());
        });
        test("A timestamp older than the preceding swipe is rejected", () -> {
            AttendanceRecord previous = new AttendanceRecord(
                    "R1", "S1", "Z1", "2026-10-29 08:00:00", AttendanceRecord.CheckType.CHECK_IN
            );
            AttendanceRecord earlier = new AttendanceRecord(
                    "R2", "S1", "Z1", "2026-10-29 07:59:59", AttendanceRecord.CheckType.CHECK_OUT
            );
            throwsType(exception.AttendanceOrderException.class, () -> earlier.assertCanFollow(previous));
        });
        finish();
    }
}
