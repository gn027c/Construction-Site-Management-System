package controller;

import model.*;
import service.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Coordinates console controllers, services and the four application CSV files.
 *
 * <p>Loading stages fresh services and checks cross-file references before
 * replacing active services. Saving replaces each CSV file independently;
 * the four-file set is not a single transaction.
 */
public class SiteManagerController {
    private final Path directory;
    private final AuthenticationService authentication;
    private PersonnelService personnelService;
    private ZoneService zoneService;
    private AttendanceService attendanceService;
    private IncidentService incidentService;
    private PersonnelController personnelController;
    private ZoneController zoneController;
    private AttendanceController attendanceController;
    private IncidentController incidentController;
    private boolean dataReady;
    private List<String> integrityIssues = new ArrayList<>();

    private static final String[] FILES = { "persons.csv", "zones.csv", "attendance.csv", "incidents.csv" };

    public SiteManagerController() throws IOException {
        this(Paths.get("data"));
    }

    public SiteManagerController(Path directory) throws IOException {
        this.directory = directory.toAbsolutePath().normalize();
        authentication = new AuthenticationService(this.directory);
        PersonnelService people = new PersonnelService();
        ZoneService zones = new ZoneService();
        useServices(people, zones, new AttendanceService(people, zones), new IncidentService(people));
    }

    private void useServices(
            PersonnelService people,
            ZoneService zones,
            AttendanceService attendance,
            IncidentService incidents
    ) {
        personnelService = people;
        zoneService = zones;
        attendanceService = attendance;
        incidentService = incidents;
        authentication.bindPersonnel(people);
        personnelController = new PersonnelController(this);
        zoneController = new ZoneController(this);
        attendanceController = new AttendanceController(this);
        incidentController = new IncidentController(this);
    }

    /**
     * Loads a complete CSV set into fresh services and validates cross-file references.
     *
     * <p>No existing files means an empty data set. A partial file set is rejected.
     * Active services are replaced only after all loading and reference checks succeed.
     */
    public void loadAllData() throws IOException {
        try {
            loadValidatedData(true);
            authentication.audit("LOAD_DATA", "CSV", "", "SUCCESS", "Loaded validated business data.");
        } catch (IOException | RuntimeException e) {
            if (integrityIssues.isEmpty()) {
                integrityIssues.add(e.getMessage());
            }
            authentication.audit("LOAD_DATA", "CSV", "", "FAILED", String.join(" | ", integrityIssues));
            throw new IOException("Business data rejected. Administrator review required: " + e.getMessage(), e);
        }
    }

    private void loadValidatedData(boolean activate) throws IOException {
        integrityIssues = new ArrayList<>();
        int existing = 0;
        for (String file : FILES) {
            if (Files.exists(directory.resolve(file))) {
                existing++;
            }
        }
        if (existing != 0 && existing != FILES.length) {
            throw new IOException("Incomplete CSV set: all four data files are required.");
        }
        PersonnelService people = new PersonnelService();
        ZoneService zones = new ZoneService();
        AttendanceService attendance = new AttendanceService(people, zones);
        IncidentService incidents = new IncidentService(people);
        if (existing != 0) {
            if (!people.loadFromCsv(directory.resolve(FILES[0]).toString())) {
                throw new IOException("Invalid persons.csv");
            }
            if (!zones.loadFromCsv(directory.resolve(FILES[1]).toString())) {
                throw new IOException("Invalid zones.csv");
            }
            if (!attendance.loadFromCsv(directory.resolve(FILES[2]).toString())) {
                throw new IOException("Invalid attendance.csv");
            }
            if (!incidents.loadFromCsv(directory.resolve(FILES[3]).toString())) {
                throw new IOException("Invalid incidents.csv");
            }
            validateReferences(people, zones, attendance, incidents);
            authentication.validateLinks(people);
            integrityIssues = new AttendanceIntegrityService().scan(attendance.getAllAttendanceRecords());
            if (!integrityIssues.isEmpty()) {
                throw new IOException("Invalid attendance history (" + integrityIssues.size() + " issues).");
            }
        }
        if (activate) {
            useServices(people, zones, attendance, incidents);
            dataReady = true;
        }
    }

    private static void validateReferences(
            PersonnelService people,
            ZoneService zones,
            AttendanceService attendance,
            IncidentService incidents
    )
            throws IOException {
        try {
            for (AttendanceRecord record : attendance.getAllAttendanceRecords()) {
                people.findPersonByCode(record.getPersonCode());
                zones.findZoneById(record.getZoneId());
            }
            for (Zone zone : zones.getAllZones()) {
                if (zone instanceof RestrictedZone) {
                    for (String code : ((RestrictedZone) zone).getAllowedPersonCodes()) {
                        people.findPersonByCode(code);
                    }
                }
            }
            for (Incident incident : incidents.getAllIncidents()) {
                if (!incident.getAssignedTo().isEmpty()) {
                    people.findPersonByCode(incident.getAssignedTo());
                }
            }
        } catch (RuntimeException e) {
            throw new IOException("Invalid CSV references: " + e.getMessage(), e);
        }
    }

    /**
     * Validates references and saves each of the four CSV files independently.
     *
     * <p>Each file replacement is atomic, but the complete set is not a transaction.
     * A failure may leave some files saved; the exception names the failed files.
     */
    public void saveAllData() throws IOException {
        if (!dataReady) {
            throw new IOException("Business data is not active; saving is blocked until administrator review.");
        }
        validateReferences(personnelService, zoneService, attendanceService, incidentService);
        List<String> failed = new ArrayList<>();
        if (!personnelService.saveToCsv(directory.resolve(FILES[0]).toString())) {
            failed.add(FILES[0]);
        }
        if (!zoneService.saveToCsv(directory.resolve(FILES[1]).toString())) {
            failed.add(FILES[1]);
        }
        if (!attendanceService.saveToCsv(directory.resolve(FILES[2]).toString())) {
            failed.add(FILES[2]);
        }
        if (!incidentService.saveToCsv(directory.resolve(FILES[3]).toString())) {
            failed.add(FILES[3]);
        }
        if (!failed.isEmpty()) {
            throw new IOException(
                    "Could not save " + String.join(
                            ", ",
                            failed
                    ) + ". Other files may have been saved; retry Save after correcting the error."
            );
        }
    }

    private void saveChanges() {
        try {
            saveAllData();
        } catch (IOException e) {
            throw new exception.SiteManagementException("Save failed; changes remain in memory. " + e.getMessage(), e);
        }
    }

    /**
     * Collects current service data for a separate spreadsheet-oriented export.
     */
    public Path exportCsv(Path destination) throws IOException {
        authentication.require(Permission.EXPORT_REPORTS);
        requireReady();
        validateReferences(personnelService, zoneService, attendanceService, incidentService);
        Map<String, List<String>> tables = new LinkedHashMap<>();
        List<String> persons = new ArrayList<>(
                Arrays.asList("type,id,code,name,phoneNumber,role,extra1,extra2,extra3,active")
        );
        for (Person person : personnelService.getAllPersons()) {
            persons.add(person.toCsvLine());
        }
        tables.put(FILES[0], persons);
        List<String> zones = new ArrayList<>(
                Arrays.asList("type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes")
        );
        for (Zone zone : zoneService.getAllZones()) {
            zones.add(zone.toCsvLine());
        }
        tables.put(FILES[1], zones);
        List<String> attendance = new ArrayList<>(Arrays.asList("recordId,personCode,zoneId,timestamp,checkType"));
        for (AttendanceRecord record : attendanceService.getAllAttendanceRecords()) {
            attendance.add(record.toCsvLine());
        }
        tables.put(FILES[2], attendance);
        List<String> incidents = new ArrayList<>(
                Arrays.asList("incidentId,title,description,severity,status,assignedTo,reportedAt,resolvedAt,archived")
        );
        for (Incident incident : incidentService.getAllIncidents()) {
            incidents.add(incident.toCsvLine());
        }
        tables.put(FILES[3], incidents);
        try {
            Path result = new CsvExportService().export(tables, destination);
            authentication.audit("EXPORT_CSV", "CSV", "", "SUCCESS", "Exported business data.");
            return result;
        } catch (IOException e) {
            authentication.audit("EXPORT_CSV", "CSV", "", "FAILED", e.getMessage());
            throw e;
        }
    }

    public void handlePersonMenu(Scanner scanner) {
        authentication.require(Permission.MANAGE_PERSONNEL);
        personnelController.handleMenu(scanner);
    }

    public void handleZoneMenu(Scanner scanner) {
        authentication.require(Permission.MANAGE_ZONES);
        zoneController.handleMenu(scanner);
    }

    public void handleAttendanceMenu(Scanner scanner) {
        authentication.requireAny(
                Permission.RECORD_ATTENDANCE, Permission.VIEW_ATTENDANCE,
                Permission.RECORD_OWN_ATTENDANCE, Permission.VIEW_OWN_ATTENDANCE
        );
        attendanceController.handleMenu(scanner);
    }

    public void handleIncidentMenu(Scanner scanner) {
        authentication.requireAny(
                Permission.REPORT_INCIDENTS, Permission.VIEW_INCIDENTS, Permission.ASSIGN_INCIDENTS,
                Permission.RESOLVE_INCIDENTS, Permission.PURGE_INCIDENTS
        );
        incidentController.handleMenu(scanner);
    }

    public AuthenticationService getAuthentication() {
        return authentication;
    }

    public void reloadData() throws IOException {
        authentication.require(Permission.RELOAD_DATA);
        loadAllData();
    }

    public boolean isDataReady() {
        return dataReady;
    }

    private void requireReady() {
        if (!dataReady) {
            throw new exception.DataValidationException(
                    "data", "Service temporarily unavailable. Contact an administrator."
            );
        }
    }

    private <T> T operation(
            Permission permission, String action, String target, java.util.function.Supplier<T> body, boolean writes
    ) {
        try {
            authentication.require(permission);
            requireReady();
            T result = body.get();
            if (Boolean.FALSE.equals(result)) {
                throw new exception.DataValidationException("operation", "The requested change was not accepted.");
            }
            if (writes) {
                saveChanges();
            }
            authentication.audit(action, "BUSINESS", target, "SUCCESS", writes ? "Change saved." : "Read data.");
            return result;
        } catch (RuntimeException e) {
            authentication.audit(action, "BUSINESS", target, "FAILED", e.getMessage());
            throw e;
        }
    }

    public List<Person> people() {
        return operation(Permission.MANAGE_PERSONNEL, "LIST_PERSONNEL", "", personnelService::getAllPersons, false);
    }

    public Person person(String code) {
        return operation(Permission.MANAGE_PERSONNEL, "FIND_PERSON", code,
                () -> personnelService.findPersonByCode(code), false);
    }

    public boolean addPerson(Person person) {
        return operation(Permission.MANAGE_PERSONNEL, "ADD_PERSON", person.getCode(),
                () -> personnelService.addPerson(person), true);
    }

    public boolean updatePerson(String code, String name, String phone) {
        return operation(Permission.MANAGE_PERSONNEL, "UPDATE_PERSON", code,
                () -> personnelService.updatePerson(code, name, phone), true);
    }

    public void setPersonActive(String code, boolean active) {
        operation(Permission.MANAGE_PERSONNEL, "SET_PERSON_ACTIVE", code, () -> {
            personnelService.findPersonByCode(code).setActive(active);
            return true;
        }, true);
    }

    public void deletePerson(String code) {
        operation(Permission.MANAGE_PERSONNEL, "DELETE_PERSON", code, () -> {
            String normalized = util.IdentifierHelper.normalize(code);
            boolean referenced = authentication.isPersonLinked(code);
            for (AttendanceRecord record : attendanceService.getAllAttendanceRecords()) {
                referenced |= record.getPersonCode().equals(normalized);
            }
            for (Zone zone : zoneService.getAllZones()) {
                if (zone instanceof RestrictedZone) {
                    referenced |= ((RestrictedZone) zone).getAllowedPersonCodes().contains(normalized);
                }
            }
            for (Incident incident : incidentService.getAllIncidents()) {
                referenced |= incident.getAssignedTo().equals(normalized);
            }
            if (referenced) {
                throw new exception.DataValidationException(
                        "person", "Referenced records cannot be deleted; deactivate instead."
                );
            }
            return personnelService.deletePerson(code);
        }, true);
    }

    public List<Zone> zones() {
        return operation(Permission.MANAGE_ZONES, "LIST_ZONES", "", zoneService::getAllZones, false);
    }

    public boolean addZone(Zone zone) {
        return operation(Permission.MANAGE_ZONES, "ADD_ZONE", zone.getZoneId(), () -> zoneService.addZone(zone), true);
    }

    public boolean changeAccess(String zoneId, String personCode, boolean grant) {
        return operation(Permission.MANAGE_ZONES, grant ? "GRANT_ACCESS" : "REVOKE_ACCESS", zoneId, () -> {
            personnelService.findPersonByCode(personCode);
            return grant ? zoneService.grantZoneAccess(zoneId, personCode)
                    : zoneService.revokeZoneAccess(zoneId, personCode);
        }, true);
    }

    public List<AttendanceRecord> attendance(boolean personal) {
        return operation(personal ? Permission.VIEW_OWN_ATTENDANCE : Permission.VIEW_ATTENDANCE,
                personal ? "VIEW_OWN_ATTENDANCE" : "VIEW_ATTENDANCE", "", () -> {
                    List<AttendanceRecord> result = new ArrayList<>();
                    String code = personal ? authentication.currentPersonCode() : "";
                    for (AttendanceRecord record : attendanceService.getAllAttendanceRecords()) {
                        if (!personal || record.getPersonCode().equals(code)) {
                            result.add(record);
                        }
                    }
                    return result;
                }, false);
    }

    public boolean recordAttendance(
            String personCode, String zoneId, AttendanceRecord.CheckType type, boolean personal
    ) {
        String subject = personal ? authentication.currentPersonCode() : personCode;
        boolean saved = operation(personal ? Permission.RECORD_OWN_ATTENDANCE : Permission.RECORD_ATTENDANCE,
                type == null ? "ATTENDANCE" : type.name(), subject + " / " + zoneId, () -> {
                    return attendanceService.recordAttendanceAndSave(subject, zoneId, type,
                            directory.resolve("attendance.csv").toString());
                }, false);
        String message = type + " recorded for " + util.IdentifierHelper.normalize(subject)
                + " at " + util.IdentifierHelper.normalize(zoneId) + ".";
        authentication.getNotifications().notifyUser(authentication.currentAccount().getUsername(),
                authentication.getActivityLog().getLastOperationId(), message);
        return saved;
    }

    public List<String> occupancy(boolean personal) {
        return operation(personal ? Permission.VIEW_OWN_ATTENDANCE : Permission.VIEW_OCCUPANCY,
                "VIEW_OCCUPANCY", "", () -> {
                    Map<String, AttendanceRecord> latest = new LinkedHashMap<>();
                    for (AttendanceRecord record : attendanceService.getAllAttendanceRecords()) {
                        latest.put(record.getPersonCode(), record);
                    }
                    List<String> result = new ArrayList<>();
                    String own = personal ? authentication.currentPersonCode() : "";
                    Map<String, Integer> counts = new TreeMap<>(util.IdentifierHelper.NATURAL_ORDER);
                    for (AttendanceRecord record : latest.values()) {
                        if (record.getCheckType() == AttendanceRecord.CheckType.CHECK_IN
                                && (!personal || own.equals(record.getPersonCode()))) {
                            Person person = personnelService.findPersonByCode(record.getPersonCode());
                            result.add(record.getZoneId() + " | " + person.getCode() + " | " + person.getName()
                                    + " | " + person.getRole() + " | " + record.getTimestamp()
                                    + (person.isActive() ? "" : " | Inactive"));
                            counts.put(record.getZoneId(), counts.getOrDefault(record.getZoneId(), 0) + 1);
                        }
                    }
                    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                        result.add("TOTAL " + entry.getKey() + " = " + entry.getValue());
                    }
                    result.sort((first, second) -> {
                        String firstZone = first.startsWith("TOTAL ") ? first.split(" ")[1]
                                : first.split("\\|")[0].trim();
                        String secondZone = second.startsWith("TOTAL ") ? second.split(" ")[1]
                                : second.split("\\|")[0].trim();
                        int zoneOrder = util.IdentifierHelper.NATURAL_ORDER.compare(firstZone, secondZone);
                        if (zoneOrder != 0) {
                            return zoneOrder;
                        }
                        if (first.startsWith("TOTAL ") || second.startsWith("TOTAL ")) {
                            return Boolean.compare(first.startsWith("TOTAL "), second.startsWith("TOTAL "));
                        }
                        return util.IdentifierHelper.NATURAL_ORDER.compare(
                                first.split("\\|")[1].trim(), second.split("\\|")[1].trim()
                        );
                    });
                    return result;
                }, false);
    }

    public List<Incident> incidents(boolean archived) {
        return operation(Permission.VIEW_INCIDENTS, "LIST_INCIDENTS", "", () -> {
            List<Incident> result = new ArrayList<>();
            for (Incident incident : incidentService.getAllIncidents()) {
                if (archived || !incident.isArchived()) {
                    result.add(incident);
                }
            }
            return result;
        }, false);
    }

    public boolean reportIncident(String id, String title, String description, Incident.IncidentSeverity severity) {
        return operation(Permission.REPORT_INCIDENTS, "REPORT_INCIDENT", id,
                () -> incidentService.reportIncident(id, title, description, severity), true);
    }

    public boolean assignIncident(String id, String code) {
        return operation(Permission.ASSIGN_INCIDENTS, "ASSIGN_INCIDENT", id,
                () -> incidentService.assignIncident(id, code), true);
    }

    public boolean resolveIncident(String id) {
        return operation(Permission.RESOLVE_INCIDENTS, "RESOLVE_INCIDENT", id,
                () -> incidentService.resolveIncident(id), true);
    }

    public int archiveIncidents() {
        return operation(Permission.PURGE_INCIDENTS, "ARCHIVE_INCIDENTS", "",
                incidentService::archiveResolvedIncidents, true);
    }

    public List<String> reviewAttendance() {
        authentication.require(Permission.REVIEW_ATTENDANCE);
        try {
            loadValidatedData(!dataReady);
        } catch (IOException | RuntimeException e) {
            if (integrityIssues.isEmpty()) {
                integrityIssues.add(e.getMessage());
            }
        }
        authentication.audit("SCAN_ATTENDANCE", "CSV", "", integrityIssues.isEmpty() ? "SUCCESS" : "FAILED",
                integrityIssues.isEmpty() ? "No issues." : String.join(" | ", integrityIssues));
        return new ArrayList<>(integrityIssues);
    }

    public void correctAttendance(
            String id, String zoneId, String timestamp, String type, String reason, String evidence
    )
            throws IOException {
        authentication.require(Permission.REVIEW_ATTENDANCE);
        if (reason.trim().isEmpty() || evidence.trim().isEmpty()) {
            throw new exception.DataValidationException("review", "Reason and evidence are required.");
        }
        List<String[]> rows = util.CsvHelper.readRows(directory.resolve("attendance.csv").toString());
        String target = util.IdentifierHelper.normalize(id);
        boolean found = false;
        int matches = 0;
        String before = "";
        ZoneService verifiedZones = new ZoneService();
        if (!verifiedZones.loadFromCsv(directory.resolve("zones.csv").toString())) {
            throw new IOException("Valid zones are required before correcting attendance.");
        }
        verifiedZones.findZoneById(zoneId);
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length == 5 && util.IdentifierHelper.normalize(row[0]).equals(target)) {
                matches++;
                before = util.CsvHelper.toCsvLine(row);
                AttendanceRecord replacement = new AttendanceRecord(
                        target, row[1], zoneId, timestamp, AttendanceRecord.CheckType.valueOf(type)
                );
                rows.set(i, util.CsvHelper.parseLine(replacement.toCsvLine()));
                found = true;
            }
        }
        if (!found) {
            throw new exception.DataValidationException("recordId", "Record does not exist.");
        }
        if (matches != 1) {
            throw new exception.DataValidationException(
                    "recordId", "Ambiguous duplicate ID; resolve the CSV collision first."
            );
        }
        List<String> lines = new ArrayList<>();
        for (String[] row : rows) {
            lines.add(util.CsvHelper.toCsvLine(row));
        }
        if (!util.CsvHelper.writeAllLines(directory.resolve("attendance.csv").toString(), lines)) {
            throw new IOException("Correction could not be saved.");
        }
        dataReady = false;
        authentication.audit("CORRECT_ATTENDANCE", "ATTENDANCE", target, "SUCCESS",
                "Original: " + before + "; corrected zone=" + zoneId + "; time=" + timestamp
                        + "; action=" + type + "; reason: " + reason + "; evidence: " + evidence);
        reviewAttendance();
    }

    public List<String[]> report(java.time.LocalDate from, java.time.LocalDate to, ReportService.Period period,
            boolean incidents, String personCode, String zoneId) {
        return operation(Permission.EXPORT_REPORTS, "GENERATE_REPORT", "", () -> incidents
                ? ReportService.summarizeIncidents(incidentService.getAllIncidents(), from, to, period)
                : ReportService.summarizeAttendance(
                        attendanceService.getAllAttendanceRecords(), from, to, personCode, zoneId, period
                ), false);
    }

    public Path exportReport(List<String[]> rows, Path destination) throws IOException {
        authentication.require(Permission.EXPORT_REPORTS);
        requireReady();
        Map<String, List<String>> tables = new LinkedHashMap<>();
        List<String> lines = new ArrayList<>();
        for (String[] row : rows) {
            lines.add(util.CsvHelper.toCsvLine(row));
        }
        tables.put("report.csv", lines);
        try {
            Path result = new CsvExportService().export(tables, destination);
            authentication.audit("EXPORT_REPORT", "REPORT", "", "SUCCESS", "Exported filtered report.");
            return result;
        } catch (IOException e) {
            authentication.audit("EXPORT_REPORT", "REPORT", "", "FAILED", e.getMessage());
            throw e;
        }
    }
}
