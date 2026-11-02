import static support.Checks.*;

import controller.SiteManagerController;
import exception.AuthenticationException;
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
import model.auth.Permission;
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
import service.AuthenticationService;

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
            site.addZone(new Zone("Z1", "Gate"));
            site.reportIncident("INC101", "Cable", "Broken cable", Incident.IncidentSeverity.CRITICAL, "Z1");
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
            site.addZone(new Zone("Z1", "Gate"));
            site.reportIncident("INC101", "Cable", "Broken cable", Incident.IncidentSeverity.HIGH, "Z1");
            site.assignIncident("INC101", "W1");
            throwsType(DataValidationException.class, () -> site.assignIncident("INC101", "W1"));
            site.setPersonActive("W1", false);
            site.assignIncident("INC101", "W2");
            Incident incident = site.incidents(false).get(0);
            equal("W2", incident.getAssignedTo());
            equal(Incident.IncidentStatus.ASSIGNED, incident.getStatus());
            site.resolveIncident("INC101", "Cable replaced");
            throwsType(InvalidIncidentStateException.class, () -> site.assignIncident("INC101", "W2"));
        });
        test("Zone and incident console flows accept canonical identifier aliases", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            capture(() -> site.handleZoneMenu(new Scanner("3\nz03\nw003\n0\n")));
            require(((RestrictedZone) storedZone(root, "Z3")).getAllowedPersonCodes().contains("W3"));
            capture(() -> site.handleIncidentMenu(new Scanner("3\ninc0103\nw002\n0\n")));
            equal("W2", storedIncident(root, "INC103").getAssignedTo());
            capture(() -> site.handleIncidentMenu(new Scanner("4\ninc0103\nCable replaced\n0\n")));
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
        test("TC-AUTH-01 A deactivated person cannot sign in until reactivated", () -> {
            SiteManagerController site = empty();
            Worker worker = new Worker("P1", "W1", "Worker", "0901234567", "Welder", "", "Team A");
            site.addPerson(worker);
            String username = site.provisionAccount(worker).getUsername();
            site.setPersonActive("W1", false);
            throwsType(AuthenticationException.class, () -> site.getAuthentication().login(username, "0"));
            site.getAuthentication().login("admin", "admin");
            site.setPersonActive("W1", true);
            equal(username, site.getAuthentication().login(username, "0").getUsername());
        });
        test("TC-AUTH-02 The first administrator must choose a new password", () -> {
            AuthenticationService auth = new AuthenticationService(Files.createTempDirectory("smartsite-first-admin-"));
            require(auth.login("admin", "admin").mustChangePassword());
        });
        test("TC-PERS-08 An administrator deletes an unused person together with the linked account", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            Worker worker = new Worker("P1", "W9", "New Worker", "0901234567", "Welder", "", "Team A");
            site.addPerson(worker);
            String username = site.provisionAccount(worker).getUsername();
            String output = capture(() -> site.handlePersonMenu(new Scanner("4\nw09\ny\n0\n")));
            require(output.contains("linked account '" + username + "'"));
            equal(null, storedPerson(root, "W9"));
            equal(null, new AuthenticationService(root).getAccount(username));
        });
        test("TC-PERS-09 Changing a worker into staff keeps the code and requires an account role", () -> {
            SiteManagerController site = empty();
            site.addZone(new Zone("Z1", "Gate"));
            Worker worker = new Worker("P1", "W1", "Worker", "0901234567", "Welder", "", "Team A");
            site.addPerson(worker);
            String username = site.provisionAccount(worker).getUsername();
            site.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, false);
            model.person.Staff staff = new model.person.Staff(
                    "P1", "W1", "Worker", "0901234567", "Safety Officer", "CERT_W1_SAFE"
            );
            throwsType(DataValidationException.class, () -> site.changePersonType("W1", staff, Account.Role.ADMIN));
            equal(username, site.changePersonType("w01", staff, Account.Role.SAFETY_OFFICER));
            require(site.person("W1") instanceof model.person.Staff);
            equal(Account.Role.SAFETY_OFFICER, site.getAuthentication().getAccount(username).getRole());
            equal("W1", site.attendance(false).get(0).getPersonCode());
            require(site.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_OUT, false));
            throwsType(DataValidationException.class, () -> site.changePersonType(
                    "W1", new Visitor("P1", "W1", "Worker", "0901234567", "Survey", "S1", ""), Account.Role.SITE_USER
            ));
        });
        test("TC-PERS-10 A visitor hired from the console gets an account with the chosen role", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            String output = capture(() -> site.handlePersonMenu(
                    new Scanner("5\nv01\n1\n1\n1\nTeam V\n1\n1\ny\n0\n")
            ));
            Worker hired = (Worker) storedPerson(root, "V1");
            equal("Welder", hired.getTrade());
            equal(SafetyLevel.LOW, hired.getSafetyLevel());
            require(output.contains("Account provisioned"));
            AuthenticationService auth = new AuthenticationService(root);
            equal(Account.Role.SITE_USER, auth.getAccount(auth.linkedUsername("V1")).getRole());
        });
        test("A staff position change is applied with an explicit account role, or kept for managers", () -> {
            SiteManagerController site = empty();
            model.person.Staff staff = new model.person.Staff(
                    "P1", "S1", "Engineer", "0901234567", "Site Engineer", ""
            );
            site.addPerson(staff);
            String username = site.provisionAccount(staff).getUsername();
            capture(() -> site.handlePersonMenu(new Scanner("2\ne\nS1\n\n\nSafety Officer\n\n2\n0\n0\n")));
            equal("Safety Officer", ((model.person.Staff) site.person("S1")).getPosition());
            equal(Account.Role.SAFETY_OFFICER, site.getAuthentication().getAccount(username).getRole());
            site.getAuthentication().createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            site.getAuthentication().login("manager", "secret");
            String output = capture(() -> site.handlePersonMenu(new Scanner("2\ne\nS1\n\n\nProject Manager\n\n0\n0\n")));
            require(output.contains("ask an administrator"));
            equal("Safety Officer", ((model.person.Staff) site.person("S1")).getPosition());
            throwsType(exception.AccessDeniedException.class, () -> site.changePersonType(
                    "S1", new Worker("P1", "S1", "Engineer", "0901234567", "Welder", "", "Team"), Account.Role.SITE_USER
            ));
        });
        test("TC-INC-07 A safety officer reports an incident with its zone; the reporter is recorded", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            site.getAuthentication().createAccount("officer", "secret", Account.Role.SAFETY_OFFICER, "W3");
            site.getAuthentication().login("officer", "secret");
            capture(() -> site.handleIncidentMenu(new Scanner("1\n\nLoose railing\nLevel 3 railing\n3\nz09\nz02\n0\n")));
            Incident stored = storedIncident(root, "INC106");
            equal("Z2", stored.getZoneId());
            equal("officer", stored.getReportedBy());
            equal(Incident.IncidentStatus.OPEN, stored.getStatus());
            throwsType(exception.ZoneNotFoundException.class, () -> site.reportIncident(
                    "INC107", "Spill", "Oil", Incident.IncidentSeverity.LOW, "Z99"
            ));
        });
        test("TC-INC-08 Resolving requires a note, which is saved and reloaded", () -> {
            Path root = sample();
            SiteManagerController site = open(root);
            throwsType(DataValidationException.class, () -> site.resolveIncident("INC101", " "));
            equal(Incident.IncidentStatus.ASSIGNED, site.incidents(false).get(0).getStatus());
            site.resolveIncident("inc101", "Pump installed, water removed");
            Incident stored = storedIncident(root, "INC101");
            equal(Incident.IncidentStatus.RESOLVED, stored.getStatus());
            equal("Pump installed, water removed", stored.getResolutionNote());
            equal("Z3", stored.getZoneId());
            throwsType(DataValidationException.class, () -> new Incident(
                    "I1", "T", "D", Incident.IncidentSeverity.LOW, Incident.IncidentStatus.OPEN, "",
                    "", "", false, "Z1", "admin", "Too early"
            ));
        });
        test("TC-REP-01 Working hours pair each check-in with its check-out and count open sessions", () -> {
            SiteManagerController site = open(sample());
            java.time.LocalDate day = java.time.LocalDate.of(2026, 10, 12);
            List<String[]> rows = site.workingHours(day, day, service.ReportService.Period.DAY, "", "");
            equal("period,personCode,sessions,hours,openSessions", String.join(",", rows.get(0)));
            List<String> lines = rows.stream().map(r -> String.join(",", r)).collect(Collectors.toList());
            require(lines.contains("2026-10-12,W1,3,8.67,0"));
            require(lines.contains("2026-10-12,W2,2,8.08,0"));
            require(lines.contains("2026-10-12,C1,0,0.00,1"));
            List<String[]> filtered = site.workingHours(
                    day, day.plusDays(6), service.ReportService.Period.WEEK, "w01", "z03"
            );
            equal(2, filtered.size());
            equal("2026-10-12,W1,1,3.50,0", String.join(",", filtered.get(1)));
            equal(1, site.workingHours(day.plusDays(1), day.plusDays(1), service.ReportService.Period.DAY, "", "").size());
        });
        test("TC-AUTH-03 Each visible menu option works with only its own permission", () -> {
            Object[][] cases = {
                    { Permission.MANAGE_ZONES, "zones", "3\nZ3\nW3\n0\n" },
                    { Permission.RECORD_ATTENDANCE, "attendance", "1\nW3\nZ2\n2\n0\n" },
                    { Permission.VIEW_OCCUPANCY, "attendance", "5\n0\n0\n" },
                    { Permission.REPORT_INCIDENTS, "incidents", "1\n\nTitle\nDescription\n2\nZ1\n0\n" },
                    { Permission.ASSIGN_INCIDENTS, "incidents", "3\nINC103\nW1\n0\n" },
                    { Permission.RESOLVE_INCIDENTS, "incidents", "4\nINC101\nPump installed\n0\n" },
            };
            for (Object[] item : cases) {
                SiteManagerController site = open(sample());
                AuthenticationService auth = site.getAuthentication();
                auth.createAccount("probe", "secret", Account.Role.SITE_MANAGER, "W2");
                auth.changePermissions("probe", java.util.EnumSet.of((Permission) item[0]));
                auth.login("probe", "secret");
                Scanner input = new Scanner((String) item[2]);
                String output = capture(() -> {
                    if (item[1].equals("zones")) {
                        site.handleZoneMenu(input);
                    } else if (item[1].equals("attendance")) {
                        site.handleAttendanceMenu(input);
                    } else {
                        site.handleIncidentMenu(input);
                    }
                });
                require(!output.contains("Access denied"));
                require(!output.contains("[ERROR]"));
            }
        });
        test("TC-AUTH-04 A personnel manager provisions only SITE_USER accounts", () -> {
            SiteManagerController site = empty();
            site.getAuthentication().createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            site.getAuthentication().login("manager", "secret");
            model.person.Staff staff = new model.person.Staff(
                    "P1", "S1", "Lead", "0901234567", "Project Manager", ""
            );
            site.addPerson(staff);
            Account account = site.provisionAccount(staff);
            equal(Account.Role.SITE_USER, account.getRole());
            equal("S1", account.getPersonCode());
            throwsType(exception.AccessDeniedException.class, () -> site.getAuthentication().provisionPersonnelAccount(
                    "boss", "0", Account.Role.SITE_MANAGER, "S1"
            ));
            throwsType(exception.AccessDeniedException.class, () -> site.getAuthentication().createAccount(
                    "other", "0", Account.Role.SITE_USER, "S1"
            ));
        });
        test("TC-AUTH-05 Accounts cannot be linked to visitors", () -> {
            SiteManagerController site = open(sample());
            AuthenticationService auth = site.getAuthentication();
            throwsType(DataValidationException.class, () -> auth.createAccount("guest", "0", Account.Role.SITE_USER, "V1"));
            auth.createAccount("staff", "0", Account.Role.SAFETY_OFFICER);
            throwsType(DataValidationException.class, () -> auth.linkPerson("staff", "V2"));
            equal("", auth.getAccount("staff").getPersonCode());
        });
        test("An invalid report filter keeps the report screen and the previous filter", () -> {
            SiteManagerController site = open(sample());
            String output = capture(() -> new controller.WorkspaceController(site).attendanceReport(
                    new Scanner("1\n01/10/2026\n31/10/2026\n1\nabc\n\n0\n")
            ));
            require(output.contains("the previous filter was kept"));
            require(!output.contains("[ERROR]"));
        });
        test("Occupancy rows keep names with separators intact", () -> {
            SiteManagerController site = empty();
            site.addZone(new Zone("Z1", "Gate"));
            site.addPerson(new Worker("P1", "W1", "An | Binh", "0901234567", "Welder", "", "Team A"));
            site.recordAttendance("W1", "Z1", AttendanceRecord.CheckType.CHECK_IN, false);
            List<String> row = site.occupancy(false).get(0);
            equal(Arrays.asList("Z1", "W1", "An | Binh", "WORKER"), row.subList(0, 4));
            equal("Active", row.get(5));
            String output = capture(() -> site.handleAttendanceMenu(new Scanner("5\n0\n0\n")));
            require(output.contains("[TOTAL OCCUPANTS: 1]"));
        });
        test("An incident file with the old nine-column header is rejected", () -> {
            Path csv = Files.createTempFile("smartsite-old-incidents-", ".csv");
            try {
                util.CsvHelper.writeAllLines(csv.toString(), Arrays.asList(
                        "incidentId,title,description,severity,status,assignedTo,reportedAt,resolvedAt,archived",
                        "INC1,Title,Description,LOW,OPEN,,,,false"
                ));
                require(!new IncidentRepository().loadFromCsv(csv.toString()));
            } finally {
                Files.deleteIfExists(csv);
            }
        });
        finish();
    }
}
