import static support.Checks.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import model.attendance.AttendanceRecord;
import model.incident.Incident;
import model.zone.RestrictedZone;
import model.zone.Zone;
import view.AttendanceView;
import view.IncidentView;
import view.ZoneView;

public class DomainViewsTest {
    public static void main(String[] args) throws Exception {
        test("ZoneView formats regular and restricted zones cleanly without debug braces", () -> {
            Zone regular = new Zone("Z1", "Site Office");
            String regularSummary = ZoneView.formatSummary(regular);
            equal("Zone ID: Z1 | Name: Site Office | Type: REGULAR", regularSummary);

            RestrictedZone restricted = new RestrictedZone(
                    "Z2", "Tunnel Excavation", RestrictedZone.SafetyLevel.HIGH,
                    new HashSet<>(Arrays.asList("W1", "W2"))
            );
            String restrictedSummary = ZoneView.formatSummary(restricted);
            equal(
                    "Zone ID: Z2 | Name: Tunnel Excavation | Type: RESTRICTED | Required safety: HIGH | Authorized personnel: 2",
                    restrictedSummary
            );

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintStream prev = System.out;
            try (PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
                System.setOut(out);
                ZoneView.showDetails(restricted);
            } finally {
                System.setOut(prev);
            }
            String details = bytes.toString("UTF-8");
            require(details.contains("ZONE DETAILS"));
            require(details.contains("Zone ID: Z2"));
            require(details.contains("Type: RESTRICTED"));
            require(!details.contains("RestrictedZone{"));
        });

        test("IncidentView formats incident summary and details cleanly", () -> {
            Incident incident = new Incident(
                    "INC1", "Slip hazard", "Spilled oil on floor",
                    Incident.IncidentSeverity.HIGH, Incident.IncidentStatus.OPEN, "",
                    "2026-10-12 08:00:00", "", false
            );
            String summary = IncidentView.formatSummary(incident);
            require(summary.contains("Incident ID: INC1"));
            require(summary.contains("Title: Slip hazard"));
            require(summary.contains("Severity: HIGH"));
            require(summary.contains("Status: OPEN"));
            require(summary.contains("Assigned to: Unassigned"));
            require(summary.contains("Reported: 2026-10-12 08:00:00"));
            require(summary.contains("Archived: No"));
            require(!summary.contains("Incident{"));

            incident.assignTo("W1");
            incident.resolve();
            String resolvedSummary = IncidentView.formatSummary(incident);
            require(resolvedSummary.contains("Status: RESOLVED"));
            require(resolvedSummary.contains("Assigned to: W1"));
            require(resolvedSummary.contains("Resolved:"));
        });

        test("AttendanceView formats swipe records cleanly", () -> {
            AttendanceRecord record = new AttendanceRecord(
                    "ATT1", "W1", "Z1", "2026-10-12 07:30:00", AttendanceRecord.CheckType.CHECK_IN
            );
            String summary = AttendanceView.formatSummary(record);
            equal(
                    "Record ID: ATT1 | Person: W1 | Zone: Z1 | Type: CHECK_IN | Timestamp: 2026-10-12 07:30:00",
                    summary
            );
            require(!summary.contains("AttendanceRecord: recordId="));

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintStream prev = System.out;
            try (PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
                System.setOut(out);
                AttendanceView.showDetails(record);
            } finally {
                System.setOut(prev);
            }
            String details = bytes.toString("UTF-8");
            require(details.contains("ATTENDANCE RECORD DETAILS"));
            require(details.contains("Record ID: ATT1"));
        });

        test("ListView renders tabular data with proper borders and aligned columns", () -> {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintStream prev = System.out;
            try (PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
                System.setOut(out);
                view.ListView.printTable(
                        Arrays.asList("ID", "Name", "Role"),
                        Arrays.asList(
                                Arrays.asList("P1", "Nguyen Van An", "WORKER"),
                                Arrays.asList("P2", "Tran Van Binh", "CONTRACTOR")
                        )
                );
            } finally {
                System.setOut(prev);
            }
            String table = bytes.toString("UTF-8");
            require(table.contains("┌────┬───────────────┬────────────┐"));
            require(table.contains("│ ID │ Name          │ Role       │"));
            require(table.contains("│ P1 │ Nguyen Van An │ WORKER     │"));
            require(table.contains("│ P2 │ Tran Van Binh │ CONTRACTOR │"));
        });

        test("ListView.showTable renders Personnel records in clean table format", () -> {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintStream prev = System.out;
            try (PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
                System.setOut(out);
                view.ListView.showTable(
                        new java.util.Scanner("0\n"),
                        "PERSONNEL",
                        Arrays.asList(
                                new model.person.Worker("P1", "W1", "Nguyen Van An", "0901234567", "Welder", "CERT_01_SAFE", "Team B"),
                                new model.person.Contractor("P6", "C1", "Vu Dinh Phuc", "0912345678", "Delta E&C", "CTR-2026-A")
                        ),
                        view.PersonnelView.TABLE_HEADERS,
                        view.PersonnelView::tableRow,
                        view.PersonnelView::formatSummary,
                        null
                );
            } finally {
                System.setOut(prev);
            }
            String output = bytes.toString("UTF-8");
            require(output.contains("┌────┬──────┬───────────────┬────────────┬────────────┬────────┬────────┬"));
            require(output.contains("│ ID │ Code │ Name          │ Phone      │ Type       │ Status │ Safety │ Details"));
            require(output.contains("P1"));
            require(output.contains("Nguyen Van An"));
            require(output.contains("Vu Dinh Phuc"));
            require(output.contains("Delta E&C (CTR-2026-A)"));
        });

        test("ListView.printTable automatically truncates long text cells to MAX_CELL_WIDTH with ellipsis", () -> {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintStream prev = System.out;
            String longMessage = "This is an extremely long notification message that exceeds fifty characters easily and would wrap";
            try (PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
                System.setOut(out);
                view.ListView.printTable(
                        Arrays.asList("ID", "Message"),
                        Arrays.asList(
                                Arrays.asList("NTF1", longMessage)
                        )
                );
            } finally {
                System.setOut(prev);
            }
            String table = bytes.toString("UTF-8");
            require(table.contains("This is an extremely long notification message..."));
            require(!table.contains(longMessage));
        });

        test("ListView.showTable supports D command to inspect domain card details", () -> {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            PrintStream prev = System.out;
            try (PrintStream out = new PrintStream(bytes, true, "UTF-8")) {
                System.setOut(out);
                view.ListView.showTable(
                        new java.util.Scanner("d 1\n\n0\n"),
                        "PERSONNEL",
                        Arrays.asList(
                                new model.person.Worker("P1", "W1", "Nguyen Van An", "0901234567", "Welder", "CERT_01_SAFE", "Team B")
                        ),
                        view.PersonnelView.TABLE_HEADERS,
                        view.PersonnelView::tableRow,
                        view.PersonnelView::formatSummary,
                        null
                );
            } finally {
                System.setOut(prev);
            }
            String output = bytes.toString("UTF-8");
            require(output.contains("PERSONNEL DETAILS"));
            require(output.contains("Trade: Welder"));
            require(output.contains("Safety certificate: CERT_01_SAFE"));
            require(output.contains("Press Enter to return to table:"));
        });

        finish();
    }
}
