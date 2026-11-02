import static support.Checks.*;

import controller.SiteManagerController;
import exception.DataValidationException;
import exception.InvalidIncidentStateException;
import exception.RestrictedZoneViolationException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import model.attendance.AttendanceRecord;
import model.auth.Account;
import model.common.SafetyLevel;
import model.incident.Incident;
import model.person.Contractor;
import model.person.Person;
import model.person.Visitor;
import model.person.Worker;
import model.zone.RestrictedZone;
import model.zone.Zone;
import repository.IncidentRepository;
import repository.PersonRepository;
import repository.ZoneRepository;

public class BusinessCompletionTest {
    private static SiteManagerController empty() throws Exception {
        SiteManagerController site = new SiteManagerController(Files.createTempDirectory("smartsite-business-"));
        site.loadAllData();
        site.getAuthentication().login("admin", "admin");
        return site;
    }

    private static Path sample() throws Exception {
        Path root = Files.createTempDirectory("smartsite-business-sample-");
        for (String file : Arrays.asList("persons.csv", "zones.csv", "attendance.csv", "incidents.csv")) {
            Files.copy(Paths.get("data", file), root.resolve(file));
        }
        return root;
    }

    private static SiteManagerController open(Path root) throws Exception {
        SiteManagerController site = new SiteManagerController(root);
        site.loadAllData();
        site.getAuthentication().login("admin", "admin");
        return site;
    }

    private static String capture(Body body) throws Exception {
        PrintStream previousOut = System.out;
        PrintStream previousErr = System.err;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(bytes, true, "UTF-8");
        System.setOut(captured);
        System.setErr(captured);
        try {
            body.run();
        } finally {
            System.setOut(previousOut);
            System.setErr(previousErr);
        }
        return new String(bytes.toByteArray(), "UTF-8");
    }

    private static Person storedPerson(Path root, String code) {
        PersonRepository repo = new PersonRepository();
        require(repo.loadFromCsv(root.resolve("persons.csv").toString()));
        return repo.findByCode(code);
    }

    private static Zone storedZone(Path root, String id) {
        ZoneRepository repo = new ZoneRepository();
        require(repo.loadFromCsv(root.resolve("zones.csv").toString()));
        return repo.findById(id);
    }

    private static Incident storedIncident(Path root, String id) {
        IncidentRepository repo = new IncidentRepository();
        require(repo.loadFromCsv(root.resolve("incidents.csv").toString()));
        return repo.findById(id);
    }

    public static void main(String[] args) throws Exception {
        test("TC-PERS-07 A worker certified after training can enter a restricted zone", () -> {
            SiteManagerController site = empty();
            site.addPerson(new Worker("P1", "W1", "Trainee", "0901234567", "Welder", "", "Team A"));
            site.addZone(new RestrictedZone("Z1", "Pit", SafetyLevel.HIGH, new HashSet<>()));
            site.changeAccess("Z1", "W1", true);
            throwsType(RestrictedZoneViolationException.class, () -> site.recordAttendance(
                    "W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, false
            ));
            site.updatePersonProfile(
                    "w01", "Trainee", "0901234567", new String[] { "Rigger", "Team B", "CERT_W1_SAFE" }, SafetyLevel.HIGH
            );
            require(site.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, false));
            Worker worker = (Worker) site.person("W1");
            equal("Rigger", worker.getTrade());
            equal("Team B", worker.getAssignedTeam());
            equal("CERT_W1_SAFE", worker.getSafetyCertId());
            equal(SafetyLevel.HIGH, worker.getSafetyLevel());
        });
        test("A rejected profile update leaves every field unchanged", () -> {
            Worker worker = new Worker("P1", "W1", "Original", "0901234567", "Welder", "CERT_SAFE", "Team A");
            throwsType(DataValidationException.class, () -> worker.updateProfile(
                    "Changed", "0907654321", new String[] { "Rigger", " ", "" }, SafetyLevel.NONE
            ));
            throwsType(DataValidationException.class, () -> worker.updateProfile(
                    "Changed", "123", new String[] { "Rigger", "Team B", "" }, SafetyLevel.NONE
            ));
            throwsType(DataValidationException.class, () -> worker.updateProfile(
                    "Changed", "0907654321", new String[] { "Rigger", "Team B" }, SafetyLevel.NONE
            ));
            equal("Original", worker.getName());
            equal("0901234567", worker.getPhoneNumber());
            equal("Welder", worker.getTrade());
            equal("Team A", worker.getAssignedTeam());
            equal("CERT_SAFE", worker.getSafetyCertId());
        });
        test("Losing the credential returns the safety level to NONE", () -> {
            Contractor contractor = new Contractor("P1", "C1", "Contractor", "0901234567", "Delta", "CTR-2026-A");
            contractor.setSafetyLevel(SafetyLevel.HIGH);
            contractor.updateProfile("Contractor", "0901234567", new String[] { "Delta", "EXPIRED-1" }, SafetyLevel.HIGH);
            equal("EXPIRED-1", contractor.getContractId());
            equal(SafetyLevel.NONE, contractor.getSafetyLevel());
            Visitor visitor = new Visitor("P2", "V1", "Guest", "0901234568", "Survey", "W1", "");
            visitor.updateProfile("Guest", "0901234568", new String[] { "Audit", "C1", "15/11/2026" }, SafetyLevel.HIGH);
            equal("Audit", visitor.getPurpose());
            equal("15/11/2026", visitor.getEntryDate());
            equal(SafetyLevel.NONE, visitor.getSafetyLevel());
        });
        test("The console edit form renews a contractor contract and saves it", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            capture(() -> site.handlePersonMenu(new Scanner("2\ne\nc03\n\n\n\nCTR-2026-C\n3\n0\n0\n")));
            Contractor stored = (Contractor) storedPerson(root, "C3");
            equal("CTR-2026-C", stored.getContractId());
            equal("Coteccons", stored.getCompanyName());
            equal(SafetyLevel.HIGH, stored.getSafetyLevel());
        });
        test("The console edit form clears an optional certificate with a dash", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            String output = capture(() -> site.handlePersonMenu(new Scanner("2\ne\nW1\n\n\n\nTeam Z\n-\n0\n0\n")));
            Worker stored = (Worker) storedPerson(root, "W1");
            equal("Team Z", stored.getAssignedTeam());
            equal("", stored.getSafetyCertId());
            equal(SafetyLevel.NONE, stored.getSafetyLevel());
            require(output.contains("safety level returns to NONE"));
        });
        test("TC-INC-05 Incidents are assigned only to active site personnel", () -> {
            SiteManagerController site = empty();
            site.addPerson(new Worker("P1", "W1", "Active", "0901234567", "Welder", "", "Team A"));
            site.addPerson(new Worker("P2", "W2", "Former", "0901234568", "Welder", "", "Team A"));
            site.addPerson(new Visitor("P3", "V1", "Guest", "0901234569", "Survey", "W1", ""));
            site.setPersonActive("W2", false);
            site.reportIncident("INC101", "Cable", "Broken cable", Incident.IncidentSeverity.CRITICAL);
            throwsType(DataValidationException.class, () -> site.assignIncident("INC101", "W2"));
            throwsType(DataValidationException.class, () -> site.assignIncident("INC101", "V1"));
            equal(Arrays.asList("W1"), site.candidateAssignees().stream()
                    .map(Person::getCode).collect(Collectors.toList()));
            require(site.assignIncident("INC101", "w01"));
            equal("W1", site.incidents(false).get(0).getAssignedTo());
        });
        test("TC-INC-06 An assigned incident can be handed to another person until it is resolved", () -> {
            SiteManagerController site = empty();
            site.addPerson(new Worker("P1", "W1", "First", "0901234567", "Welder", "", "Team A"));
            site.addPerson(new Worker("P2", "W2", "Second", "0901234568", "Welder", "", "Team A"));
            site.reportIncident("INC101", "Cable", "Broken cable", Incident.IncidentSeverity.HIGH);
            site.assignIncident("INC101", "W1");
            throwsType(DataValidationException.class, () -> site.assignIncident("INC101", "W1"));
            site.setPersonActive("W1", false);
            site.assignIncident("INC101", "W2");
            Incident incident = site.incidents(false).get(0);
            equal("W2", incident.getAssignedTo());
            equal(Incident.IncidentStatus.ASSIGNED, incident.getStatus());
            site.resolveIncident("INC101");
            throwsType(InvalidIncidentStateException.class, () -> site.assignIncident("INC101", "W2"));
        });
        test("Zone and incident console flows accept canonical identifier aliases", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            capture(() -> site.handleZoneMenu(new Scanner("3\nz03\nw003\n0\n")));
            require(((RestrictedZone) storedZone(root, "Z3")).getAllowedPersonCodes().contains("W3"));
            capture(() -> site.handleIncidentMenu(new Scanner("3\ninc0103\nw002\n0\n")));
            equal("W2", storedIncident(root, "INC103").getAssignedTo());
            capture(() -> site.handleIncidentMenu(new Scanner("4\ninc0103\n0\n")));
            equal(Incident.IncidentStatus.RESOLVED, storedIncident(root, "INC103").getStatus());
        });
        test("Visitors cannot be granted restricted-zone access", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            throwsType(DataValidationException.class, () -> site.changeAccess("Z3", "V1", true));
            String output = capture(() -> site.handleZoneMenu(new Scanner("3\nZ3\nV1\n0\n")));
            require(output.contains("Visitors cannot enter restricted zones"));
            require(!((RestrictedZone) storedZone(root, "Z3")).getAllowedPersonCodes().contains("V1"));
        });
        test("Granting a listed person or revoking an unlisted one reports that nothing changed", () -> {
            SiteManagerController site = open(sample());
            String granted = capture(() -> site.handleZoneMenu(new Scanner("3\nZ3\nW1\n0\n")));
            require(granted.contains("W1 is already on the allowed list of Z3"));
            require(!granted.contains("Access list updated"));
            String revoked = capture(() -> site.handleZoneMenu(new Scanner("4\nZ3\nW3\n0\n")));
            require(revoked.contains("W3 is not on the allowed list of Z3"));
        });
        test("TC-ZONE-06 Zone name and required safety level can be edited", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            site.updateZone("z04", "Tower crane", SafetyLevel.HIGH);
            equal(SafetyLevel.HIGH, ((RestrictedZone) storedZone(root, "Z4")).getRequiredSafetyLevel());
            equal("Tower crane", storedZone(root, "Z4").getZoneName());
            throwsType(DataValidationException.class, () -> site.updateZone("Z1", "Gate", SafetyLevel.HIGH));
            throwsType(DataValidationException.class, () -> site.updateZone("Z3", "Pit", SafetyLevel.NONE));
            equal(SafetyLevel.HIGH, ((RestrictedZone) storedZone(root, "Z3")).getRequiredSafetyLevel());
            capture(() -> site.handleZoneMenu(new Scanner("5\nz05\nChemical store\n4\n0\n")));
            RestrictedZone edited = (RestrictedZone) storedZone(root, "Z5");
            equal("Chemical store", edited.getZoneName());
            equal(SafetyLevel.CRITICAL, edited.getRequiredSafetyLevel());
            capture(() -> site.handleZoneMenu(new Scanner("5\nZ1\nMain gate\n0\n")));
            equal("Main gate", storedZone(root, "Z1").getZoneName());
        });
        test("A site user records personal attendance from the console", () -> {
            SiteManagerController site = empty();
            site.addPerson(new Worker("P1", "W1", "Worker", "0901234567", "Welder", "", "Team A"));
            site.addZone(new Zone("Z1", "Gate"));
            site.getAuthentication().createAccount("worker", "secret", Account.Role.SITE_USER, "W1");
            site.getAuthentication().login("worker", "secret");
            String output = capture(() -> site.handleAttendanceMenu(new Scanner("3\nz01\n1\n0\n")));
            require(output.contains("Attendance recorded."));
            List<AttendanceRecord> own = site.attendance(true);
            equal(1, own.size());
            equal("Z1", own.get(0).getZoneId());
        });
        finish();
    }
}
