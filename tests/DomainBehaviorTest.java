import static support.Checks.*;
import exception.*;
import model.*;
import repository.*;
import service.*;
import java.nio.file.*;
import java.util.*;

public class DomainBehaviorTest {
    private static Incident report() { return Incident.report(" I1 ", " Title ", " Description ", Incident.IncidentSeverity.HIGH); }
    private static AttendanceRecord swipe(String id, String person, String zone, AttendanceRecord.CheckType type) {
        return new AttendanceRecord(id, person, zone, "2026-10-29 07:05:00", type);
    }

    public static void main(String[] args) throws Exception {
        test("Incident report factory creates a normalized OPEN incident", () -> {
            Incident i = report(); equal("I1", i.getIncidentId()); equal("Title", i.getTitle());
            require(i.isOpen()); equal("", i.getAssignedTo());
        });
        test("Incident owns assignment and resolution without a service", () -> {
            Incident i = report(); i.assignTo(" W1 ");
            equal(Incident.IncidentStatus.ASSIGNED, i.getStatus()); equal("W1", i.getAssignedTo());
            i.resolve(); require(i.isResolved()); equal("W1", i.getAssignedTo());
        });
        test("Resolving an OPEN incident preserves its status", () -> {
            Incident i = report(); throwsType(InvalidIncidentStateException.class, i::resolve);
            require(i.isOpen()); equal("", i.getAssignedTo());
        });
        test("Reassignment and repeated resolution are rejected by the entity", () -> {
            Incident i = report(); i.assignTo("W1");
            throwsType(InvalidIncidentStateException.class, () -> i.assignTo("W2"));
            equal("W1", i.getAssignedTo()); i.resolve();
            throwsType(InvalidIncidentStateException.class, i::resolve);
            throwsType(InvalidIncidentStateException.class, () -> i.assignTo("W2"));
            require(i.isResolved()); equal("W1", i.getAssignedTo());
        });
        test("Invalid assignee cannot partially change an incident", () -> {
            Incident i = report(); throwsType(DataValidationException.class, () -> i.assignTo(" "));
            require(i.isOpen()); equal("", i.getAssignedTo());
        });
        test("Incident construction rejects inconsistent persisted fields", () -> {
            throwsType(DataValidationException.class, () -> new Incident("I1", "T", "D", Incident.IncidentSeverity.LOW, Incident.IncidentStatus.OPEN, "W1"));
            throwsType(DataValidationException.class, () -> new Incident("I1", "T", "D", Incident.IncidentSeverity.LOW, Incident.IncidentStatus.ASSIGNED, ""));
            throwsType(DataValidationException.class, () -> new Incident("I1", "T", "D", Incident.IncidentSeverity.LOW, Incident.IncidentStatus.RESOLVED, null));
            throwsType(DataValidationException.class, () -> Incident.report("I1", "T", "D", null));
        });
        test("Incident CSV restoration obeys entity invariants", () -> {
            IncidentRepository repo = new IncidentRepository(); repo.save(report());
            Path csv = Files.createTempFile("smartsite-domain-incident-", ".csv");
            try {
                util.CsvHelper.writeAllLines(csv.toString(), Arrays.asList("incidentId,title,description,severity,status,assignedTo,reportedAt,resolvedAt,archived", "BROKEN1,Title,Description,LOW,ASSIGNED,,,,false"));
                require(!repo.loadFromCsv(csv.toString())); require(repo.findById("I1").isOpen());
                util.CsvHelper.writeAllLines(csv.toString(), Arrays.asList("incidentId,title,description,severity,status,assignedTo,reportedAt,resolvedAt,archived", "RESTORED1,Title,Description,LOW,RESOLVED,W1,,,false"));
                require(repo.loadFromCsv(csv.toString())); require(repo.findById("RESTORED1").isResolved());
                throwsType(InvalidIncidentStateException.class, () -> repo.findById("RESTORED1").resolve());
            } finally { Files.deleteIfExists(csv); }
        });
        test("Attendance entity rejects checkout without checkin", () -> {
            AttendanceRecord out = swipe("R1", "W1", "Z1", AttendanceRecord.CheckType.CHECK_OUT);
            throwsType(AttendanceOrderException.class, () -> out.assertCanFollow(null));
        });
        test("Attendance entity enforces zone and alternating swipe rules", () -> {
            AttendanceRecord in = swipe("R1", "W1", "Z1", AttendanceRecord.CheckType.CHECK_IN);
            in.assertCanFollow(null);
            throwsType(AttendanceOrderException.class, () -> swipe("R2", "W1", "Z2", AttendanceRecord.CheckType.CHECK_IN).assertCanFollow(in));
            throwsType(AttendanceOrderException.class, () -> swipe("R2", "W1", "Z2", AttendanceRecord.CheckType.CHECK_OUT).assertCanFollow(in));
            AttendanceRecord out = swipe("R2", "W1", "Z1", AttendanceRecord.CheckType.CHECK_OUT);
            out.assertCanFollow(in);
            throwsType(AttendanceOrderException.class, () -> swipe("R3", "W1", "Z1", AttendanceRecord.CheckType.CHECK_OUT).assertCanFollow(out));
            swipe("R3", "W1", "Z2", AttendanceRecord.CheckType.CHECK_IN).assertCanFollow(out);
        });
        test("Attendance entity rejects another person's preceding record", () -> {
            AttendanceRecord previous = swipe("R1", "W1", "Z1", AttendanceRecord.CheckType.CHECK_IN);
            throwsType(DataValidationException.class, () -> swipe("R2", "W2", "Z1", AttendanceRecord.CheckType.CHECK_OUT).assertCanFollow(previous));
        });
        test("Attendance required fields use the common validation errors", () -> {
            throwsType(DataValidationException.class, () -> swipe(" ", "W1", "Z1", AttendanceRecord.CheckType.CHECK_IN));
            throwsType(DataValidationException.class, () -> swipe("R1", "W1", "Z1", null));
            throwsType(DataValidationException.class, () -> new AttendanceRecord("R1", "W1", "Z1", "wrong time", AttendanceRecord.CheckType.CHECK_IN));
        });
        test("Rejected repository save does not consume a record counter value", () -> {
            AttendanceRepository repo = new AttendanceRepository() {
                private boolean reject = true;
                @Override public boolean save(AttendanceRecord r) {
                    if (reject) { reject = false; return false; }
                    return super.save(r);
                }
            };
            PersonnelService people = new PersonnelService(); ZoneService zones = new ZoneService();
            people.addPerson(new Worker("P1", "W1", "Worker", "0901234567", "Welder", "X_SAFE", "A"));
            zones.addZone(new Zone("Z1", "Regular"));
            AttendanceService service = new AttendanceService(repo, people, zones);
            require(!service.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN));
            equal(0, repo.findAll().size());
            require(service.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN));
            equal("REC1", repo.findAll().get(0).getRecordId());
        });
        finish();
    }
}
