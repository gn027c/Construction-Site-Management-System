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
    private PersonnelService personnelService;
    private ZoneService zoneService;
    private AttendanceService attendanceService;
    private IncidentService incidentService;
    private PersonnelController personnelController;
    private ZoneController zoneController;
    private AttendanceController attendanceController;
    private IncidentController incidentController;

    private static final String[] FILES = { "persons.csv", "zones.csv", "attendance.csv", "incidents.csv" };

    public SiteManagerController() {
        this(Paths.get("data"));
    }

    public SiteManagerController(Path directory) {
        this.directory = directory.toAbsolutePath().normalize();
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
        personnelController = new PersonnelController(people);
        zoneController = new ZoneController(zones, people);
        attendanceController = new AttendanceController(attendance);
        incidentController = new IncidentController(incidents);
    }

    /**
     * Loads a complete CSV set into fresh services and validates cross-file references.
     *
     * <p>No existing files means an empty data set. A partial file set is rejected.
     * Active services are replaced only after all loading and reference checks succeed.
     */
    public void loadAllData() throws IOException {
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
        }
        useServices(people, zones, attendance, incidents);
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
        validateReferences(personnelService, zoneService, attendanceService, incidentService);
        Map<String, List<String>> tables = new LinkedHashMap<>();
        List<String> persons = new ArrayList<>(
                Arrays.asList("type,id,code,name,phoneNumber,role,extra1,extra2,extra3")
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
                Arrays.asList("incidentId,title,description,severity,status,assignedTo")
        );
        for (Incident incident : incidentService.getAllIncidents()) {
            incidents.add(incident.toCsvLine());
        }
        tables.put(FILES[3], incidents);
        return new CsvExportService().export(tables, destination);
    }

    public void handlePersonMenu(Scanner scanner) {
        personnelController.handleMenu(scanner, this::saveChanges);
    }

    public void handleZoneMenu(Scanner scanner) {
        zoneController.handleMenu(scanner, this::saveChanges);
    }

    public void handleAttendanceMenu(Scanner scanner) {
        attendanceController.handleMenu(scanner, this::saveChanges);
    }

    public void handleIncidentMenu(Scanner scanner) {
        incidentController.handleMenu(scanner, this::saveChanges);
    }
}
