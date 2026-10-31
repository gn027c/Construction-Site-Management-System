package controller;

import model.person.*;
import model.zone.*;
import model.attendance.*;
import model.incident.*;
import model.auth.*;
import model.common.*;
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
                throw new IOException("Invalid attendance.csv: " + attendance.getLastLoadError());
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
     * <p>Each file is overwritten separately; the complete set is not a transaction.
     * A failure may leave some files saved; the exception names the failed files.
     */
    public void saveAllData() throws IOException {
        if (!dataReady) {
            throw new IOException("Business data is not active; saving is blocked until administrator review.");
        }
        validateReferences(personnelService, zoneService, attendanceService, incidentService);
        List<String> historyIssues = new AttendanceIntegrityService().scan(attendanceService.getAllAttendanceRecords());
        if (!historyIssues.isEmpty()) {
            throw new IOException("Attendance validation failed; no business CSV files were saved.");
        }
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

    public boolean shouldProvisionAccount(Person person) {
        return person != null && person.shouldProvisionAccount();
    }

    public Account provisionAccount(Person person) throws IOException {
        return provisionAccount(person, "0");
    }

    public Account provisionAccount(Person person, String initialPassword) throws IOException {
        if (!shouldProvisionAccount(person)) {
            throw new exception.DataValidationException(
                    "person", "Account provisioning is not supported for " + (person == null ? "null" : person.getRole())
            );
        }
        String normalizedCode = util.IdentifierHelper.normalize(person.getCode());
        if (authentication.isPersonLinked(normalizedCode)) {
            throw new exception.DataValidationException("personCode", "Person is already linked to an account.");
        }
        String username = generateProvisionedUsername(person);
        Account.Role role = resolveAccountRole(person);
        String pass = (initialPassword == null || initialPassword.trim().isEmpty()) ? "0" : initialPassword;

        authentication.createAccount(username, pass, role, normalizedCode, true);
        return authentication.getAccount(username);
    }

    public static String removeDiacritics(String text) {
        if (text == null) {
            return "";
        }
        String nfd = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfd).replaceAll("")
                .replace('\u0111', 'd')
                .replace('\u0110', 'D')
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }

    public static String buildUsernameBase(String name, String code) {
        if (code == null) {
            code = "";
        }
        String cleanCode = code.trim().toUpperCase(java.util.Locale.ROOT);
        if (name == null || name.trim().isEmpty()) {
            return ("User" + cleanCode).replaceAll("[^a-zA-Z0-9_.-]", "");
        }
        String noAccents = removeDiacritics(name);
        String[] words = noAccents.trim().split("\\s+");
        List<String> validWords = new ArrayList<>();
        for (String w : words) {
            String cleaned = w.replaceAll("[^a-zA-Z0-9]", "");
            if (!cleaned.isEmpty()) {
                validWords.add(cleaned);
            }
        }
        if (validWords.isEmpty()) {
            return ("User" + cleanCode).replaceAll("[^a-zA-Z0-9_.-]", "");
        }
        String lastWord = validWords.get(validWords.size() - 1);
        String givenName = Character.toUpperCase(lastWord.charAt(0))
                + (lastWord.length() > 1 ? lastWord.substring(1).toLowerCase(java.util.Locale.ROOT) : "");

        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < validWords.size() - 1; i++) {
            initials.append(Character.toUpperCase(validWords.get(i).charAt(0)));
        }

        String base = givenName + initials.toString() + cleanCode;
        if (base.length() < 3) {
            base = "User" + cleanCode;
        }
        return base;
    }

    public String generateProvisionedUsername(Person person) {
        String base = buildUsernameBase(person.getName(), person.getCode());
        String candidate = base;
        int counter = 2;
        while (authentication.isUsernameTaken(candidate)) {
            candidate = base + "_" + counter++;
        }
        return candidate;
    }

    private Account.Role resolveAccountRole(Person person) {
        if (person instanceof Staff) {
            Staff s = (Staff) person;
            String pos = s.getPosition() != null ? s.getPosition().toLowerCase() : "";
            if (pos.contains("safety")) {
                return Account.Role.SAFETY_OFFICER;
            } else if (pos.contains("manager") || pos.contains("supervisor")) {
                return Account.Role.SITE_MANAGER;
            }
        }
        return Account.Role.SITE_USER;
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
        return operation(personal ? Permission.RECORD_OWN_ATTENDANCE : Permission.RECORD_ATTENDANCE,
                type == null ? "ATTENDANCE" : type.name(), subject + " / " + zoneId, () -> {
                    return attendanceService.recordAttendanceAndSave(subject, zoneId, type,
                            directory.resolve("attendance.csv").toString());
                }, false);
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

    public List<Person> candidateAssignees() {
        return operation(Permission.ASSIGN_INCIDENTS, "LIST_ASSIGNEES", "",
                personnelService::getAllPersons, false);
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

    public List<String[]> report(java.time.LocalDate from, java.time.LocalDate to, ReportService.Period period,
            boolean incidents, String personCode, String zoneId) {
        return operation(Permission.EXPORT_REPORTS, "GENERATE_REPORT", "", () -> incidents
                ? ReportService.summarizeIncidents(incidentService.getAllIncidents(), from, to, period)
                : ReportService.summarizeAttendance(
                        attendanceService.getAllAttendanceRecords(), from, to, personCode, zoneId, period
                ), false);
    }

    public java.time.LocalDate[] getReportDateBounds(boolean incidents) {
        authentication.require(Permission.EXPORT_REPORTS);
        java.time.LocalDate min = null;
        java.time.LocalDate max = null;
        if (incidents) {
            for (Incident inc : incidentService.getAllIncidents()) {
                if (inc.getReportedAt() != null && !inc.getReportedAt().isEmpty()) {
                    try {
                        java.time.LocalDate d = util.DateTimeHelper.parseTimestamp(inc.getReportedAt()).toLocalDate();
                        if (min == null || d.isBefore(min)) min = d;
                        if (max == null || d.isAfter(max)) max = d;
                    } catch (RuntimeException ignored) {}
                }
                if (inc.getResolvedAt() != null && !inc.getResolvedAt().isEmpty()) {
                    try {
                        java.time.LocalDate d = util.DateTimeHelper.parseTimestamp(inc.getResolvedAt()).toLocalDate();
                        if (min == null || d.isBefore(min)) min = d;
                        if (max == null || d.isAfter(max)) max = d;
                    } catch (RuntimeException ignored) {}
                }
            }
        } else {
            for (AttendanceRecord r : attendanceService.getAllAttendanceRecords()) {
                if (r.getTimestamp() != null && !r.getTimestamp().isEmpty()) {
                    try {
                        java.time.LocalDate d = util.DateTimeHelper.parseTimestamp(r.getTimestamp()).toLocalDate();
                        if (min == null || d.isBefore(min)) min = d;
                        if (max == null || d.isAfter(max)) max = d;
                    } catch (RuntimeException ignored) {}
                }
            }
        }
        if (min == null || max == null) {
            java.time.LocalDate now = java.time.LocalDate.now();
            return new java.time.LocalDate[] { now.minusMonths(1), now };
        }
        return new java.time.LocalDate[] { min, max };
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

    public String suggestNextPersonId() {
        return generateRandomPersonId();
    }

    public String generateRandomPersonId() {
        Set<String> existingIds = new HashSet<>();
        for (Person p : personnelService.getAllPersons()) {
            if (p.getId() != null) {
                existingIds.add(p.getId().toUpperCase());
            }
        }
        Random random = new Random();
        String candidate;
        do {
            int num = 10000 + random.nextInt(90000);
            candidate = "P" + num;
        } while (existingIds.contains(candidate));
        return candidate;
    }

    public String suggestNextPersonCode(int type) {
        String prefix;
        switch (type) {
            case 1:
                prefix = "W";
                break;
            case 2:
                prefix = "C";
                break;
            case 3:
                prefix = "V";
                break;
            case 4:
                prefix = "S";
                break;
            default:
                prefix = "P";
                break;
        }
        int max = 0;
        for (Person p : personnelService.getAllPersons()) {
            String code = p.getCode();
            if (code != null && code.toUpperCase().startsWith(prefix)) {
                String digits = code.substring(prefix.length()).trim();
                if (digits.matches("[0-9]+")) {
                    try {
                        int val = Integer.parseInt(digits);
                        max = Math.max(max, val);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return prefix + (max + 1);
    }

    public String suggestNextZoneId() {
        int max = 0;
        for (Zone z : zoneService.getAllZones()) {
            String id = z.getZoneId();
            if (id != null && id.matches("(?i)Z[0-9]+")) {
                try {
                    int val = Integer.parseInt(id.replaceAll("[^0-9]", ""));
                    max = Math.max(max, val);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "Z" + (max + 1);
    }

    public String suggestNextIncidentId() {
        int max = 100;
        for (Incident inc : incidentService.getAllIncidents()) {
            String id = inc.getIncidentId();
            if (id != null && id.matches("(?i)INC[0-9]+")) {
                try {
                    int val = Integer.parseInt(id.replaceAll("[^0-9]", ""));
                    max = Math.max(max, val);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "INC" + (max + 1);
    }
}
