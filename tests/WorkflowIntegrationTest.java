import static support.Checks.*;

import controller.SiteManagerController;
import exception.AccessDeniedException;
import exception.AttendanceOrderException;
import exception.DataValidationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import model.auth.Account;
import model.attendance.AttendanceRecord;
import model.auth.Permission;
import model.person.Staff;
import model.person.Worker;
import model.zone.Zone;
import service.AuthenticationService;
import util.CsvHelper;

public class WorkflowIntegrationTest {
    private static SiteManagerController site() throws Exception {
        SiteManagerController site = new SiteManagerController(Files.createTempDirectory("smartsite-workflow-"));
        site.loadAllData();
        site.getAuthentication().login("admin", "admin");
        return site;
    }

    private static void setup(SiteManagerController site) throws Exception {
        site.addPerson(new Worker("P1", "w01", "Worker A", "0901234567", "Trade", "CERT_SAFE", "Team"));
        site.addPerson(new Staff("P2", "s02", "Manager B", "0901234568", "Manager", ""));
        site.addZone(new Zone("z01", "First zone"));
        site.addZone(new Zone("Z2", "Second zone"));
    }

    public static void main(String[] args) throws Exception {
        test("Linked site users can only record and view their own attendance", () -> {
            SiteManagerController site = site();
            setup(site);
            AuthenticationService auth = site.getAuthentication();
            auth.createAccount("worker", "secret", Account.Role.SITE_USER, "w001");
            auth.login("worker", "secret");
            site.recordAttendance("S2", "z01", AttendanceRecord.CheckType.CHECK_IN, true);
            equal("W1", site.attendance(true).get(0).getPersonCode());
            throwsType(AccessDeniedException.class, () -> site.attendance(false));
            throwsType(AccessDeniedException.class, () -> site.recordAttendance(
                    "S2", "Z1", AttendanceRecord.CheckType.CHECK_IN, false
            ));
            require(site.occupancy(true).get(0).contains("W1"));
            require(!site.occupancy(true).get(0).contains("S2"));
        });
        test("A staff manager can check in personally and manage site data", () -> {
            SiteManagerController site = site();
            setup(site);
            AuthenticationService auth = site.getAuthentication();
            auth.createAccount("manager", "secret", Account.Role.SITE_MANAGER, "S02");
            auth.login("manager", "secret");
            site.recordAttendance("", "Z1", AttendanceRecord.CheckType.CHECK_IN, true);
            site.updatePerson("w01", "Updated", "0901234567");
            equal("S2", site.attendance(true).get(0).getPersonCode());
            equal("Updated", site.person("W1").getName());
            require(site.occupancy(false).get(0).contains("S2"));
        });
        test("Failed cross-zone check-in is logged and delivered to worker and admin", () -> {
            SiteManagerController site = site();
            setup(site);
            AuthenticationService auth = site.getAuthentication();
            auth.createAccount("worker", "secret", Account.Role.SITE_USER, "W1");
            auth.login("worker", "secret");
            site.recordAttendance("", "Z1", AttendanceRecord.CheckType.CHECK_IN, true);
            throwsType(AttendanceOrderException.class, () -> site.recordAttendance(
                    "", "Z2", AttendanceRecord.CheckType.CHECK_IN, true
            ));
            equal(1, site.attendance(true).size());
        });
        test("Inactive people remain visible inside and can check out but cannot check in again", () -> {
            SiteManagerController site = site();
            setup(site);
            site.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, false);
            site.setPersonActive("W1", false);
            require(site.occupancy(false).get(0).contains("Inactive"));
            site.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_OUT, false);
            throwsType(DataValidationException.class, () -> site.recordAttendance(
                    "W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, false
            ));
            require(site.occupancy(false).isEmpty());
            throwsType(DataValidationException.class, () -> site.deletePerson("W1"));
        });
        test("A person cannot be linked to two accounts or deleted while linked", () -> {
            SiteManagerController site = site();
            setup(site);
            AuthenticationService auth = site.getAuthentication();
            auth.createAccount("worker", "secret", Account.Role.SITE_USER, "W1");
            throwsType(java.io.IOException.class, () -> auth.createAccount(
                    "another", "secret", Account.Role.SITE_USER, "w01"
            ));
            throwsType(DataValidationException.class, () -> site.deletePerson("w001"));
        });
        test("Scanning rejects invalid history without activating it and leaves source intact", () -> {
            Path root = Files.createTempDirectory("smartsite-review-");
            for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv")) {
                Files.copy(java.nio.file.Paths.get("data", file), root.resolve(file));
            }
            CsvHelper.writeAllLines(root.resolve("attendance.csv").toString(), Arrays.asList(
                    "recordId,personCode,zoneId,timestamp,checkType",
                    "ATT1,W1,Z1,2026-10-12 07:30:00,CHECK_IN",
                    "ATT6,W1,Z3,2026-10-12 08:15:00,CHECK_IN"
            ));
            byte[] before = Files.readAllBytes(root.resolve("attendance.csv"));
            SiteManagerController site = new SiteManagerController(root);
            throwsType(java.io.IOException.class, site::loadAllData);
            require(!site.isDataReady());
            AuthenticationService auth = site.getAuthentication();
            auth.login("admin", "admin");
            require(!site.reviewAttendance().isEmpty());
            throwsType(DataValidationException.class, () -> site.exportCsv(root.resolve("reports")));
            require(Arrays.equals(before, Files.readAllBytes(root.resolve("attendance.csv"))));
            require(!Files.exists(root.resolve("attendance_quarantine.csv")));
        });
        test("Reload rejects invalid transitions and accepts a corrected source only after full validation", () -> {
            Path root = Files.createTempDirectory("smartsite-validated-reload-");
            SiteManagerController site = new SiteManagerController(root);
            site.loadAllData();
            site.getAuthentication().login("admin", "admin");
            setup(site);
            CsvHelper.writeAllLines(root.resolve("attendance.csv").toString(), Arrays.asList(
                    "recordId,personCode,zoneId,timestamp,checkType",
                    "ATT1,W1,Z1,2026-10-29 07:00:00,CHECK_IN",
                    "ATT2,W1,Z2,2026-10-29 08:00:00,CHECK_OUT"
            ));
            require(!site.reviewAttendance().isEmpty());
            throwsType(java.io.IOException.class, site::reloadData);
            require(site.attendance(false).isEmpty());
            require(CsvHelper.writeAllLines(root.resolve("attendance.csv").toString(), Arrays.asList(
                    "recordId,personCode,zoneId,timestamp,checkType",
                    "ATT1,W1,Z1,2026-10-29 07:00:00,CHECK_IN",
                    "ATT2,W1,Z1,2026-10-29 08:00:00,CHECK_OUT"
            )));
            site.reloadData();
            require(site.isDataReady());
            require(site.reviewAttendance().isEmpty());
            require(site.occupancy(false).isEmpty());
            equal(2, site.attendance(false).size());
        });
        test("A failed attendance CSV write rolls back the swipe and sequence", () -> {
            Path root = Files.createTempDirectory("smartsite-swipe-failure-");
            SiteManagerController site = new SiteManagerController(root);
            site.loadAllData();
            site.getAuthentication().login("admin", "admin");
            setup(site);
            Path attendance = root.resolve("attendance.csv");
            Files.move(attendance, root.resolve("saved-attendance.csv"));
            Files.createDirectory(attendance);
            throwsType(exception.SiteManagementException.class, () -> site.recordAttendance(
                    "W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, false
            ));
            require(site.attendance(false).isEmpty());
            Files.delete(attendance);
            Files.move(root.resolve("saved-attendance.csv"), attendance);
            site.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, false);
            equal("REC1", site.attendance(false).get(0).getRecordId());
        });
        test("Personal-only role cannot receive global attendance or administrator permissions", () -> {
            SiteManagerController site = site();
            setup(site);
            AuthenticationService auth = site.getAuthentication();
            auth.createAccount("worker", "secret", Account.Role.SITE_USER, "W1");
            throwsType(DataValidationException.class, () -> auth.changePermissions(
                    "worker", EnumSet.of(Permission.VIEW_ATTENDANCE)
            ));
            auth.createAccount("manager", "secret", Account.Role.SITE_MANAGER, "S2");
            throwsType(DataValidationException.class, () -> auth.changePermissions(
                    "manager", EnumSet.of(Permission.MANAGE_ACCOUNTS)
            ));
        });
        finish();
    }
}
