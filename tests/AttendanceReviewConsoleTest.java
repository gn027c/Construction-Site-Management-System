import static support.Checks.*;

import controller.SiteManagerController;
import exception.AccessDeniedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import model.Account;
import model.AttendanceRecord;

public class AttendanceReviewConsoleTest {
    private static Path fixture() throws Exception {
        Path root = Files.createTempDirectory("smartsite-review-ui-");
        for (String file : Arrays.asList("persons.csv", "zones.csv", "incidents.csv")) {
            Files.copy(Paths.get("data", file), root.resolve(file));
        }
        Files.write(root.resolve("attendance.csv"), Arrays.asList(
                "recordId,personCode,zoneId,timestamp,checkType",
                "ATT1,W1,Z1,2026-10-29 07:00:00,CHECK_IN",
                "ATT2,W1,Z3,2026-10-29 08:00:00,CHECK_OUT"
        ), StandardCharsets.UTF_8);
        return root;
    }

    public static void main(String[] args) throws Exception {
        test("Admin reviews a timeline and repairs a verified zone while retaining time and action", () -> {
            Path root = fixture();
            String output = ConsoleTest.launchRaw(
                    root, "admin\nadmin\n11\n3\nw01\n0\n2\natt02\nz01\n\n0\n"
                            + "Wrong zone in register\nGate register entry 2\nyes\n0\n0\n", 0, false
            );
            require(output.contains("ATTENDANCE TIMELINE / CSV ORDER / W1"));
            require(output.contains("ORIGINAL ATTENDANCE RECORD"));
            require(output.contains("ATT2 | W1 | 2026-10-29 08:00:00 | CHECK_OUT | Z3"));
            require(output.contains("All checks passed. Business data is active."));
            SiteManagerController site = new SiteManagerController(root);
            site.loadAllData();
            site.getAuthentication().login("admin", "admin");
            AttendanceRecord corrected = site.attendanceForReview("w01").get(1);
            equal("Z1", corrected.getZoneId());
            equal("2026-10-29 08:00:00", corrected.getTimestamp());
            equal(AttendanceRecord.CheckType.CHECK_OUT, corrected.getCheckType());
            String log = new String(Files.readAllBytes(root.resolve("activity_log.csv")), StandardCharsets.UTF_8);
            require(log.contains("Gate register entry 2"));
            require(log.contains("Original: ATT2,W1,Z3"));
        });
        test("Cancelling a proposed correction preserves the original CSV", () -> {
            Path root = fixture();
            byte[] original = Files.readAllBytes(root.resolve("attendance.csv"));
            String output = ConsoleTest.launchRaw(
                    root, "admin\nadmin\n11\n2\nATT2\nZ1\n\n0\nReason\nReference\nno\n0\n0\n", 0, false
            );
            require(output.contains("Correction cancelled. Original record retained."));
            require(Arrays.equals(original, Files.readAllBytes(root.resolve("attendance.csv"))));
        });
        test("Guidance explains missing evidence and a nonexistent record cannot change CSV", () -> {
            Path root = fixture();
            byte[] original = Files.readAllBytes(root.resolve("attendance.csv"));
            String output = ConsoleTest.launchRaw(root, "admin\nadmin\n11\n2\nATT999\n0\n", 0, false);
            require(output.contains("it does not prove misconduct"));
            require(output.contains("do not invent times or evidence"));
            require(output.contains("Record does not exist"));
            require(Arrays.equals(original, Files.readAllBytes(root.resolve("attendance.csv"))));
        });
        test("Timeline inspection enforces administrator access while data is blocked", () -> {
            SiteManagerController site = new SiteManagerController(fixture());
            throwsType(AccessDeniedException.class, () -> site.attendanceForReview("W1"));
            site.getAuthentication().login("admin", "admin");
            site.getAuthentication().createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            site.getAuthentication().login("manager", "secret");
            throwsType(AccessDeniedException.class, () -> site.attendanceForReview("W1"));
        });
        finish();
    }
}
