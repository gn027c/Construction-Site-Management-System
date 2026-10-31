import exception.*;
import model.person.*;
import model.zone.*;
import model.attendance.*;
import model.incident.*;
import model.auth.*;
import model.common.*;
import repository.IncidentRepository;
import repository.PersonRepository;
import service.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class HistoricalMatrixTest {
    static Path output;

    interface Body { String run() throws Exception; }

    static final List<String[]> results = new ArrayList<>();

    static void tc(String id, String module, String type, String scenario, String expected, Body body, boolean expectPass) {
        PrintStream orig = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf, true));
        String actual;
        boolean ok;
        try {
            actual = body.run();
            ok = expectPass;
        } catch (Exception e) {
            actual = "Threw " + e.getClass().getSimpleName() + ": " + e.getMessage();
            ok = !expectPass && expected.contains(e.getClass().getSimpleName());
        } catch (AssertionError e) {
            actual = e.getMessage();
            ok = false;
        }
        System.setOut(orig);
        String console = buf.toString().trim();
        String status = ok ? "PASS" : "FAIL";
        results.add(new String[]{id, module, type, scenario, expected, actual, status, console});
        System.out.println("=== " + id + " [" + status + "] " + scenario);
        System.out.println("    Expected: " + expected);
        System.out.println("    Actual  : " + actual);
        if (!console.isEmpty()) {
            for (String l : console.split("\\R")) System.out.println("    | " + l);
        }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    public static void main(String[] args) throws Exception {
        output = Files.createTempDirectory("smartsite-historical-matrix-");
        PersonnelService ps = new PersonnelService();
        ZoneService zs = new ZoneService();
        AttendanceService as = new AttendanceService(ps, zs);
        IncidentService is = new IncidentService(ps);
        ps.loadFromCsv(); zs.loadFromCsv(); as.loadFromCsv(); is.loadFromCsv();
        try { ps.deletePerson("W6"); } catch (Exception ignored) {}

        tc("TC-PERS-01", "Personnel", "Normal", "Add new Worker W06 (phone 0906789012, cert CERT_06_SAFE)",
           "Record added; findPersonByCode(\"W06\") returns a Worker",
           () -> {
               ps.addPerson(new Worker("P011", "W06", "Tran Thi Lan", "0906789012", "Electrician", "CERT_06_SAFE", "Team B"));
               Person p = ps.findPersonByCode("W06");
               check(p instanceof Worker, "W06 is not a Worker");
               return "Added; lookup returned " + p.getClass().getSimpleName() + " '" + p.getName() + "'";
           }, true);
        tc("TC-PERS-02", "Personnel", "Exceptional", "Add Contractor with duplicate code W01",
           "DuplicatePersonCodeException; existing record unchanged",
           () -> { ps.addPerson(new Contractor("P099", "W01", "Fake", "0900000000", "Fake Co", "CTR-9")); return "Added (unexpected)"; }, false);
        tc("TC-PERS-03", "Personnel", "Exceptional", "Add Visitor V03 with phone '12345'",
           "InvalidPhoneNumberException; record not added",
           () -> { ps.addPerson(new Visitor("P012", "V03", "Le Van Tam", "12345", "Survey", "W01", "20/10/2026")); return "Added (unexpected)"; }, false);
        tc("TC-PERS-04", "Personnel", "Normal", "Search W01 by code (LinkedHashMap lookup)",
           "Returns Worker 'Nguyen Van An'",
           () -> {
               Person p = ps.findPersonByCode("W01");
               check("Nguyen Van An".equals(p.getName()), "wrong person: " + p.getName());
               return "Found " + p.getClass().getSimpleName() + " '" + p.getName() + "', phone " + p.getPhoneNumber();
           }, true);
        tc("TC-PERS-05", "Personnel", "Exceptional", "Search unknown code X99",
           "PersonNotFoundException",
           () -> { ps.findPersonByCode("X99"); return "Found (unexpected)"; }, false);
        tc("TC-PERS-06", "Personnel", "Boundary", "Update W02 phone to '09ABC12345', then to '0987654321'",
           "First update rejected with InvalidPhoneNumberException; second update stored",
           () -> {
               try {
                   ps.updatePerson("W02", null, "09ABC12345");
                   check(false, "invalid phone accepted");
               } catch (InvalidPhoneNumberException e) {
                   System.out.println("Rejected: " + e.getMessage());
               }
               ps.updatePerson("W02", null, "0987654321");
               String phone = ps.findPersonByCode("W02").getPhoneNumber();
               check("0987654321".equals(phone), "phone not updated: " + phone);
               return "Invalid phone rejected; valid phone stored (" + phone + ")";
           }, true);

        tc("TC-ZONE-01", "Zone Security", "Normal", "Visitor V01 enters regular zone Z01",
           "Zone.checkAccess() returns true (dynamic binding to base class)",
           () -> {
               Zone z = zs.findZoneById("Z01");
               boolean r = z.checkAccess(ps.findPersonByCode("V01"));
               check(r, "access denied");
               return z.getClass().getSimpleName() + ".checkAccess() = " + r;
           }, true);
        tc("TC-ZONE-02", "Zone Security", "Normal", "Worker W01 (whitelisted, CERT_01_SAFE) enters restricted Z03",
           "RestrictedZone.checkAccess() returns true; verifyAccess() succeeds",
           () -> {
               Zone z = zs.findZoneById("Z03");
               boolean r = zs.verifyAccess("Z03", ps.findPersonByCode("W01"));
               check(r, "access denied");
               return z.getClass().getSimpleName() + " -> verifyAccess() = " + r;
           }, true);
        tc("TC-ZONE-03", "Zone Security", "Exceptional", "Worker W04 (not whitelisted, CERT_UNSAFE) enters Z03",
           "RestrictedZoneViolationException",
           () -> { zs.verifyAccess("Z03", ps.findPersonByCode("W04")); return "Granted (unexpected)"; }, false);
        tc("TC-ZONE-04", "Zone Security", "Normal", "grantZoneAccess(Z04, W05) then W05 enters Z04",
           "W05 added to whitelist; verifyAccess() returns true",
           () -> {
               zs.grantZoneAccess("Z04", "W05");
               boolean r = zs.verifyAccess("Z04", ps.findPersonByCode("W05"));
               check(r, "access denied");
               return "Granted; verifyAccess() = " + r;
           }, true);
        tc("TC-ZONE-05", "Zone Security", "Boundary", "revokeZoneAccess(Z04, W05) then W05 enters Z04 again",
           "RestrictedZoneViolationException after revocation",
           () -> { zs.revokeZoneAccess("Z04", "W05"); zs.verifyAccess("Z04", ps.findPersonByCode("W05")); return "Granted (unexpected)"; }, false);

        tc("TC-ATT-01", "Attendance FSM", "Normal", "W06 CHECK_IN at Z01 (no prior record)",
           "Record created; state = CHECK_IN",
           () -> {
               check(as.recordAttendance("W06", "Z01", AttendanceRecord.CheckType.CHECK_IN), "returned false");
               return "Recorded CHECK_IN at Z01";
           }, true);
        tc("TC-ATT-02", "Attendance FSM", "Exceptional", "W06 CHECK_IN again at Z02 without CHECK_OUT",
           "AttendanceOrderException (CHECK_IN twice)",
           () -> { as.recordAttendance("W06", "Z02", AttendanceRecord.CheckType.CHECK_IN); return "Recorded (unexpected)"; }, false);
        tc("TC-ATT-03", "Attendance FSM", "Exceptional", "W06 CHECK_OUT at Z02 while inside Z01",
           "AttendanceOrderException (zone mismatch)",
           () -> { as.recordAttendance("W06", "Z02", AttendanceRecord.CheckType.CHECK_OUT); return "Recorded (unexpected)"; }, false);
        tc("TC-ATT-04", "Attendance FSM", "Normal", "W06 CHECK_OUT at Z01",
           "Record created; state = CHECK_OUT",
           () -> {
               check(as.recordAttendance("W06", "Z01", AttendanceRecord.CheckType.CHECK_OUT), "returned false");
               return "Recorded CHECK_OUT at Z01";
           }, true);
        tc("TC-ATT-05", "Attendance FSM", "Exceptional", "W06 CHECK_OUT again without a new CHECK_IN",
           "AttendanceOrderException (already checked out)",
           () -> { as.recordAttendance("W06", "Z01", AttendanceRecord.CheckType.CHECK_OUT); return "Recorded (unexpected)"; }, false);
        tc("TC-ATT-06", "Attendance FSM", "Exceptional", "Visitor V02 (whitelisted for Z05, no safety credential) CHECK_IN at Z05",
           "RestrictedZoneViolationException; no attendance record created",
           () -> {
               ((model.person.Visitor) ps.findPersonByCode("V02"))
                       .setEntryDate(util.DateTimeHelper.formatDate(java.time.LocalDate.now()));
               int before = as.getAllAttendanceRecords().size();
               try {
                   as.recordAttendance("V02", "Z05", AttendanceRecord.CheckType.CHECK_IN);
               } catch (RestrictedZoneViolationException e) {
                   int after = as.getAllAttendanceRecords().size();
                   check(before == after, "record created despite denial");
                   return "Threw RestrictedZoneViolationException: " + e.getMessage() + " | records unchanged (" + after + ")";
               }
               throw new AssertionError("Restricted check-in unexpectedly accepted");
           }, true);

        tc("TC-INC-01", "Incident Lifecycle", "Normal", "Report INC-201 'Lan can tang 3 bi long' (HIGH)",
           "Incident created with status OPEN",
           () -> {
               is.reportIncident("INC-201", "Lan can tang 3 bi long", "Lan can tam tai tang 3 bi long chot", Incident.IncidentSeverity.HIGH);
               Incident i = is.findIncidentById("INC-201");
               check(i != null && i.getStatus() == Incident.IncidentStatus.OPEN, "not OPEN");
               return "Created; status = " + i.getStatus();
           }, true);
        tc("TC-INC-02", "Incident Lifecycle", "Normal", "Assign INC-201 to W01",
           "Status OPEN -> ASSIGNED; assignedTo = W01",
           () -> {
               is.assignIncident("INC-201", "W01");
               Incident i = is.findIncidentById("INC-201");
               check(i.getStatus() == Incident.IncidentStatus.ASSIGNED, "not ASSIGNED");
               return "Status = " + i.getStatus() + ", assignedTo = " + i.getAssignedTo();
           }, true);
        tc("TC-INC-03", "Incident Lifecycle", "Exceptional", "Resolve INC-201, then resolve it a second time",
           "First call -> RESOLVED; second call -> InvalidIncidentStateException",
           () -> {
               is.resolveIncident("INC-201");
               System.out.println("After first resolve: " + is.findIncidentById("INC-201").getStatus());
               is.resolveIncident("INC-201");
               return "Second resolve accepted (unexpected)";
           }, false);
        tc("TC-INC-04", "Incident Lifecycle", "Normal", "archiveResolvedIncidents() with INC102, INC105, INC201 RESOLVED",
           "3 incidents archived; all historical records retained",
           () -> {
               int removed = is.purgeResolvedIncidents();
               int left = is.getAllIncidents().size();
               check(removed == 3, "removed " + removed);
               check(left == 6, "historical incidents must remain");
               return "Archived " + removed + "; " + left + " incidents retained";
           }, true);

        tc("TC-IO-01", "File I/O", "Normal", "Load all 4 CSV types with a valid attendance test ledger (fresh services)",
           "10 persons, 5 zones, 15 attendance records, 5 incidents loaded",
           () -> {
               PersonnelService p2 = new PersonnelService(); ZoneService z2 = new ZoneService();
               AttendanceService a2 = new AttendanceService(p2, z2); IncidentService i2 = new IncidentService(p2);
               p2.loadFromCsv(); z2.loadFromCsv(); i2.loadFromCsv();
               Path validAttendance = output.resolve("valid_attendance.csv");
               List<String> validRows = new ArrayList<>();
               validRows.add("recordId,personCode,zoneId,timestamp,checkType");
               for (int i = 0; i < 15; i++) {
                   validRows.add("ATT" + (i + 1) + ",W1,Z1,2026-10-12 07:"
                           + String.format("%02d", i) + ":00," + (i % 2 == 0 ? "CHECK_IN" : "CHECK_OUT"));
               }
               check(util.CsvHelper.writeAllLines(validAttendance.toString(), validRows), "fixture write failed");
               check(a2.loadFromCsv(validAttendance.toString()), "valid history rejected");
               int np = p2.getAllPersons().size(), nz = z2.getAllZones().size(),
                   na = a2.getAllAttendanceRecords().size(), ni = i2.getAllIncidents().size();
               check((np == 10 || np == 11) && nz == 5 && na == 15 && ni == 5, "counts " + np + "/" + nz + "/" + na + "/" + ni);
               return np + " persons, " + nz + " zones, " + na + " attendance, " + ni + " incidents";
           }, true);
        tc("TC-IO-02", "File I/O", "Normal", "Save personnel to CSV and reload into a new repository",
           "11 records reloaded; Worker/Contractor/Visitor subtypes preserved",
           () -> {
               ps.saveToCsv(output.resolve("persons_roundtrip.csv").toString());
               PersonRepository r = new PersonRepository();
               r.loadFromCsv(output.resolve("persons_roundtrip.csv").toString());
               String types = r.findByCode("W06").getClass().getSimpleName() + "/" + r.findByCode("C01").getClass().getSimpleName()
                       + "/" + r.findByCode("V01").getClass().getSimpleName();
               check(r.findAll().size() == 11 && types.equals("Worker/Contractor/Visitor"), "got " + r.findAll().size() + " " + types);
               return r.findAll().size() + " records; W06/C01/V01 = " + types;
           }, true);
        tc("TC-IO-03", "File I/O", "Normal", "First character of a saved CSV file",
           "UTF-8 BOM (U+FEFF) present so Excel shows Vietnamese correctly",
           () -> {
               byte[] b = Files.readAllBytes(Paths.get(output.resolve("persons_roundtrip.csv").toString()));
               boolean bom = b.length >= 3 && (b[0] & 0xFF) == 0xEF && (b[1] & 0xFF) == 0xBB && (b[2] & 0xFF) == 0xBF;
               check(bom, "no BOM");
               return "File starts with EF BB BF (UTF-8 BOM)";
           }, true);
        tc("TC-IO-04", "File I/O", "Boundary", "Save and reload an incident whose description contains a comma and double quotes",
           "Description identical after reload: Cap dut, nguy hiem! \"gap\"",
           () -> {
               IncidentRepository r = new IncidentRepository();
               r.save(new Incident("INC-301", "Cap dien", "Cap dut, nguy hiem! \"gap\"", Incident.IncidentSeverity.HIGH, Incident.IncidentStatus.OPEN, ""));
               r.saveToCsv(output.resolve("incidents_roundtrip.csv").toString());
               IncidentRepository r2 = new IncidentRepository();
               r2.loadFromCsv(output.resolve("incidents_roundtrip.csv").toString());
               Incident back = r2.findById("INC-301");
               String got = back == null ? "<record lost>" : back.getDescription();
               check("Cap dut, nguy hiem! \"gap\"".equals(got), "Reloaded description = " + got);
               return "Description preserved: " + got;
           }, true);

        try (Writer w = new OutputStreamWriter(new FileOutputStream(output.resolve("results.tsv").toString()), StandardCharsets.UTF_8)) {
            for (String[] r : results) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < r.length; i++) {
                    if (i > 0) sb.append('\t');
                    sb.append(r[i].replace("\t", " ").replace("\r", "").replace("\n", "\\n"));
                }
                w.write(sb.append('\n').toString());
            }
        }
        long pass = results.stream().filter(r -> r[6].equals("PASS")).count();
        System.out.println("\nSUMMARY: " + pass + "/" + results.size() + " PASS");
        if (pass != results.size()) throw new AssertionError("Historical matrix failed");
    }
}
