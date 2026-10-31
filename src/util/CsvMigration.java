package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import model.attendance.AttendanceRecord;
import model.auth.Account;
import model.auth.Permission;
import model.incident.Incident;
import model.person.Contractor;
import model.person.Person;
import model.person.Staff;
import model.person.Visitor;
import model.person.Worker;
import model.zone.RestrictedZone;
import model.zone.Zone;


/**
 * Converts old business CSV schemas and optional accounts into a new directory.
 * Validation precedes output creation, and source files are never overwritten.
 * Attendance transitions must be valid before any destination files are created.
 */
public final class CsvMigration {
    private static final String PERSON_HEADER = "type,id,code,name,phoneNumber,role,extra1,extra2,extra3,active";
    private static final String ZONE_HEADER = "type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes";
    private static final String ATTENDANCE_HEADER = "recordId,personCode,zoneId,timestamp,checkType";
    private static final String INCIDENT_HEADER =
            "incidentId,title,description,severity,status,assignedTo,reportedAt,resolvedAt,archived";
    private static final String ACCOUNT_HEADER = "username,passwordHash,role,enabled,permissions,personCode";

    private CsvMigration() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: java util.CsvMigration <source-directory> <new-directory>");
        }
        Path output = migrate(Paths.get(args[0]), Paths.get(args[1]));
        System.out.println("CSV migration completed: " + output);
        System.out.println("Source files were preserved. Attendance transitions were validated before writing.");
    }

    public static Path migrate(Path sourceDirectory, Path destinationDirectory) throws IOException {
        Path source = sourceDirectory.toRealPath();
        if (!Files.isDirectory(source)) {
            throw new IOException("The migration source must be a directory.");
        }
        Path destination = destinationDirectory.toAbsolutePath().normalize();
        if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("The migration destination already exists: " + destination);
        }
        Path ancestor = destination.getParent();
        while (ancestor != null && !Files.exists(ancestor, LinkOption.NOFOLLOW_LINKS)) {
            ancestor = ancestor.getParent();
        }
        Path resolvedDestination = ancestor == null
                ? destination
                : ancestor.toRealPath().resolve(ancestor.relativize(destination)).normalize();
        if (resolvedDestination.startsWith(source)) {
            throw new IOException("The migration destination must be outside the source directory.");
        }

        Map<String, List<String>> output = new LinkedHashMap<>();
        Set<String> people = new HashSet<>();
        Set<String> zones = new HashSet<>();
        output.put("persons.csv", convertPersons(source.resolve("persons.csv"), people));
        output.put("zones.csv", convertZones(source.resolve("zones.csv"), people, zones));
        output.put("attendance.csv", convertAttendance(source.resolve("attendance.csv"), people, zones));
        output.put("incidents.csv", convertIncidents(source.resolve("incidents.csv"), people));
        Path accountFile = source.resolve("accounts.csv");
        if (Files.exists(accountFile, LinkOption.NOFOLLOW_LINKS)) {
            output.put("accounts.csv", convertAccounts(accountFile, people));
        }

        Path parent = destination.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.createDirectory(destination);
        for (Map.Entry<String, List<String>> table : output.entrySet()) {
            if (!CsvHelper.writeAllLines(destination.resolve(table.getKey()).toString(), table.getValue())) {
                throw new IOException("Migration output is incomplete; source files remain unchanged: " + destination);
            }
        }
        return destination;
    }

    private static List<String[]> read(Path file, String oldHeader, String newHeader) throws IOException {
        try {
            List<String[]> rows = CsvHelper.readRows(file.toString());
            if (rows.isEmpty() || !(Arrays.equals(rows.get(0), oldHeader.split(","))
                    || Arrays.equals(rows.get(0), newHeader.split(",")))) {
                throw new IllegalArgumentException("Unsupported header.");
            }
            int width = rows.get(0).length;
            for (int i = 1; i < rows.size(); i++) {
                if (!CsvHelper.isBlankRow(rows.get(i)) && rows.get(i).length != width) {
                    throw new IllegalArgumentException("Unexpected column count at row " + (i + 1) + ".");
                }
            }
            return rows;
        } catch (RuntimeException e) {
            throw new IOException("Cannot migrate " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    private static List<String> outputRows(String header) {
        List<String> rows = new ArrayList<>();
        rows.add(header);
        return rows;
    }

    private static void unique(Set<String> identifiers, String code, String field) {
        if (!identifiers.add(code)) {
            throw new IllegalArgumentException("Duplicate " + field + " after normalization: " + code);
        }
    }

    private static void reference(Set<String> identifiers, String code, String field) {
        if (!identifiers.contains(code)) {
            throw new IllegalArgumentException("Unknown " + field + ": " + code);
        }
    }

    private static boolean booleanValue(String value) {
        if (!(value.equals("true") || value.equals("false"))) {
            throw new IllegalArgumentException("Boolean fields must contain true or false.");
        }
        return Boolean.parseBoolean(value);
    }

    private static IOException rowError(Path file, int row, RuntimeException cause) {
        return new IOException(file.getFileName() + " row " + row + ": " + cause.getMessage(), cause);
    }

    private static List<String> convertPersons(Path file, Set<String> codes) throws IOException {
        String oldHeader = "type,id,code,name,phoneNumber,role,extra1,extra2,extra3";
        List<String[]> input = read(file, oldHeader, PERSON_HEADER);
        List<String> output = outputRows(PERSON_HEADER);
        Set<String> ids = new HashSet<>();
        for (int i = 1; i < input.size(); i++) {
            String[] fields = input.get(i);
            if (CsvHelper.isBlankRow(fields)) {
                continue;
            }
            try {
                Person person;
                switch (fields[0]) {
                    case "WORKER":
                        person = new Worker(
                                fields[1], fields[2], fields[3], fields[4], fields[7], fields[6], fields[8]
                        );
                        break;
                    case "CONTRACTOR":
                        if (!fields[8].isEmpty()) {
                            throw new IllegalArgumentException("Contractor extra3 must be empty.");
                        }
                        person = new Contractor(fields[1], fields[2], fields[3], fields[4], fields[7], fields[6]);
                        break;
                    case "VISITOR":
                        person = new Visitor(
                                fields[1], fields[2], fields[3], fields[4], fields[6], fields[7], fields[8]
                        );
                        break;
                    case "STAFF":
                        if (!fields[8].isEmpty()) {
                            throw new IllegalArgumentException("Staff extra3 must be empty.");
                        }
                        person = new Staff(fields[1], fields[2], fields[3], fields[4], fields[7], fields[6]);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown personnel type.");
                }
                if (!person.getRole().name().equals(fields[5])) {
                    throw new IllegalArgumentException("Personnel role does not match its type.");
                }
                person.setActive(fields.length == 9 || booleanValue(fields[9]));
                unique(ids, person.getId(), "person ID");
                unique(codes, person.getCode(), "person code");
                output.add(person.toCsvLine());
            } catch (RuntimeException e) {
                throw rowError(file, i + 1, e);
            }
        }
        return output;
    }

    private static List<String> convertZones(Path file, Set<String> people, Set<String> zoneIds) throws IOException {
        List<String[]> input = read(file, ZONE_HEADER, ZONE_HEADER);
        List<String> output = outputRows(ZONE_HEADER);
        for (int i = 1; i < input.size(); i++) {
            String[] fields = input.get(i);
            if (CsvHelper.isBlankRow(fields)) {
                continue;
            }
            try {
                Zone zone;
                if (fields[0].equals("REGULAR")) {
                    zone = new Zone(fields[1], fields[2]);
                } else if (fields[0].equals("RESTRICTED")) {
                    Set<String> allowed = new TreeSet<>(IdentifierHelper.NATURAL_ORDER);
                    if (!fields[4].isEmpty() && !fields[4].equals("NONE")) {
                        for (String code : fields[4].split(";", -1)) {
                            String normalized = IdentifierHelper.normalize(code);
                            reference(people, normalized, "whitelisted person");
                            allowed.add(normalized);
                        }
                    }
                    zone = new RestrictedZone(
                            fields[1], fields[2], RestrictedZone.SafetyLevel.valueOf(fields[3]), allowed
                    );
                } else {
                    throw new IllegalArgumentException("Unknown zone type.");
                }
                unique(zoneIds, zone.getZoneId(), "zone ID");
                output.add(zone.toCsvLine());
            } catch (RuntimeException e) {
                throw rowError(file, i + 1, e);
            }
        }
        return output;
    }

    private static List<String> convertAttendance(Path file, Set<String> people, Set<String> zones) throws IOException {
        List<String[]> input = read(file, ATTENDANCE_HEADER, ATTENDANCE_HEADER);
        List<String> output = outputRows(ATTENDANCE_HEADER);
        Set<String> ids = new HashSet<>();
        Map<String, AttendanceRecord> previous = new LinkedHashMap<>();
        for (int i = 1; i < input.size(); i++) {
            String[] fields = input.get(i);
            if (CsvHelper.isBlankRow(fields)) {
                continue;
            }
            try {
                AttendanceRecord record = new AttendanceRecord(
                        fields[0], fields[1], fields[2], fields[3], AttendanceRecord.CheckType.valueOf(fields[4])
                );
                unique(ids, record.getRecordId(), "attendance ID");
                reference(people, record.getPersonCode(), "attendance person");
                reference(zones, record.getZoneId(), "attendance zone");
                record.assertCanFollow(previous.get(record.getPersonCode()));
                previous.put(record.getPersonCode(), record);
                output.add(record.toCsvLine());
            } catch (RuntimeException e) {
                throw rowError(file, i + 1, e);
            }
        }
        return output;
    }

    private static List<String> convertIncidents(Path file, Set<String> people) throws IOException {
        List<String[]> input = read(file, "incidentId,title,description,severity,status,assignedTo", INCIDENT_HEADER);
        List<String> output = outputRows(INCIDENT_HEADER);
        Set<String> ids = new HashSet<>();
        for (int i = 1; i < input.size(); i++) {
            String[] fields = input.get(i);
            if (CsvHelper.isBlankRow(fields)) {
                continue;
            }
            try {
                Incident incident = new Incident(
                        fields[0], fields[1], fields[2], Incident.IncidentSeverity.valueOf(fields[3]),
                        Incident.IncidentStatus.valueOf(fields[4]), fields[5],
                        fields.length == 6 ? "" : fields[6], fields.length == 6 ? "" : fields[7],
                        fields.length != 6 && booleanValue(fields[8])
                );
                unique(ids, incident.getIncidentId(), "incident ID");
                if (!incident.getAssignedTo().isEmpty()) {
                    reference(people, incident.getAssignedTo(), "incident assignee");
                }
                output.add(incident.toCsvLine());
            } catch (RuntimeException e) {
                throw rowError(file, i + 1, e);
            }
        }
        return output;
    }

    private static List<String> convertAccounts(Path file, Set<String> people) throws IOException {
        List<String[]> input = read(file, "username,passwordHash,role,enabled,permissions", ACCOUNT_HEADER);
        List<String> output = outputRows(ACCOUNT_HEADER);
        Set<String> usernames = new HashSet<>();
        Set<String> linkedPeople = new HashSet<>();
        boolean hasAdmin = false;
        for (int i = 1; i < input.size(); i++) {
            String[] fields = input.get(i);
            if (CsvHelper.isBlankRow(fields)) {
                continue;
            }
            try {
                Account.Role role = Account.Role.valueOf(fields[2]);
                Set<Permission> permissions = EnumSet.noneOf(Permission.class);
                if (!fields[4].isEmpty()) {
                    for (String name : fields[4].split(";", -1)) {
                        if (!permissions.add(Permission.valueOf(name))) {
                            throw new IllegalArgumentException("Duplicate permission.");
                        }
                    }
                }
                if (role == Account.Role.ADMIN) {
                    permissions = role.defaultPermissions();
                }
                String personCode = fields.length == 5 ? "" : fields[5];
                Account account = new Account(
                        fields[0], fields[1], role, booleanValue(fields[3]), permissions, personCode
                );
                unique(usernames, account.getUsername(), "username");
                if (!account.getPersonCode().isEmpty()) {
                    reference(people, account.getPersonCode(), "linked account person");
                    unique(linkedPeople, account.getPersonCode(), "linked account person");
                }
                hasAdmin |= account.isEnabled() && role == Account.Role.ADMIN;
                output.add(account.toCsvLine());
            } catch (RuntimeException e) {
                throw rowError(file, i + 1, e);
            }
        }
        if (!hasAdmin) {
            throw new IOException("accounts.csv must retain at least one enabled administrator.");
        }
        return output;
    }
}
