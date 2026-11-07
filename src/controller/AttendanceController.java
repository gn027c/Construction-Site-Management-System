package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import model.AttendanceRecord;
import model.Permission;
import model.Person;
import model.Zone;
import service.AttendanceService;
import service.AuthenticationService;
import service.PersonnelService;
import util.InputHelper;
import view.ConsoleUi;
import view.Pagination;

public class AttendanceController {
    private final AttendanceService attendanceService;
    private final PersonnelService personnelService;
    private final AuthenticationService authentication;

    public AttendanceController(
            AttendanceService attendanceService, PersonnelService personnelService,
            AuthenticationService authentication
    ) {
        this.attendanceService = attendanceService;
        this.personnelService = personnelService;
        this.authentication = authentication;
    }

    public void handleMenu(Scanner scanner) {
        String filter = "";
        String statusMessage = null;
        List<AttendanceRecord> records = fetchRecords();
        List<AttendanceRecord> filtered = filterRecords(records, filter);
        Pagination<AttendanceRecord> pagination = new Pagination<>(filtered, 8);
        boolean back = false;

        while (!back) {
            ConsoleUi.clearScreen();
            String breadcrumb = "ATTENDANCE MANAGEMENT | PAGE "
                    + (pagination.isEmpty() ? "1/1" : (pagination.pageNumber() + "/" + pagination.pageCount()));
            if (!filter.isEmpty()) {
                breadcrumb += " | Filter: \"" + filter + "\"";
            }
            ConsoleUi.breadcrumb(breadcrumb);

            showHistoryTable(pagination.currentPage());

            if (statusMessage != null) {
                System.out.println(statusMessage);
                statusMessage = null;
            }

            String command = InputHelper.getString(
                    scanner, "Select: 1 (Check-In/Out), F (Filter), C (Clear filter), N (Next), P (Prev), L (Last), 0 (Back): ");

            if (command.equalsIgnoreCase("1")) {
                statusMessage = recordAttendanceFlow(scanner);
                records = fetchRecords();
                filtered = filterRecords(records, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("F")) {
                String term = InputHelper.getString(scanner, "Filter by Person, Zone, Action, or ID: ");
                filter = term.trim().toLowerCase();
                filtered = filterRecords(records, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("C")) {
                filter = "";
                filtered = filterRecords(records, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("N")) {
                pagination.next();
            } else if (command.equalsIgnoreCase("P")) {
                pagination.previous();
            } else if (command.equalsIgnoreCase("L")) {
                pagination.last();
            } else if (command.equals("0")) {
                back = true;
            } else {
                System.out.println("[WARN] Invalid command. Choose 1, F, C, N, P, L or 0.");
            }
        }
    }

    private List<AttendanceRecord> fetchRecords() {
        if (authentication.allows(Permission.VIEW_ATTENDANCE)) {
            return attendanceService.getAllAttendanceRecords();
        }
        if (authentication.allows(Permission.VIEW_OWN_ATTENDANCE)) {
            return attendanceService.getRecordsOf(authentication.currentPersonCode());
        }
        return Collections.emptyList();
    }

    private List<AttendanceRecord> filterRecords(List<AttendanceRecord> records, String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return new ArrayList<>(records);
        }
        String term = filter.trim().toLowerCase();
        List<AttendanceRecord> matches = new ArrayList<>();
        for (AttendanceRecord record : records) {
            boolean match = record.getRecordId().toLowerCase().contains(term)
                    || record.getPersonCode().toLowerCase().contains(term)
                    || record.getZoneId().toLowerCase().contains(term)
                    || record.getTimestamp().toLowerCase().contains(term)
                    || record.getCheckType().name().toLowerCase().contains(term);
            if (match) {
                matches.add(record);
            }
        }
        return matches;
    }

    private void showHistoryTable(List<AttendanceRecord> records) {
        if (records == null || records.isEmpty()) {
            System.out.println("No attendance records found.");
            return;
        }
        String[] headers = { "ID", "PERSON", "ZONE", "TIMESTAMP", "ACTION" };
        List<String[]> rows = new ArrayList<>();
        for (AttendanceRecord record : records) {
            rows.add(new String[] {
                    record.getRecordId(),
                    record.getPersonCode(),
                    record.getZoneId(),
                    record.getTimestamp(),
                    record.getCheckType().name()
            });
        }
        ConsoleUi.table(headers, rows);
    }

    private String personForRecording(Scanner scanner) {
        if (authentication.allows(Permission.RECORD_ATTENDANCE)) {
            return choosePerson(scanner);
        }
        authentication.require(Permission.RECORD_OWN_ATTENDANCE);
        return authentication.currentPersonCode();
    }

    private String choosePerson(Scanner scanner) {
        List<Person> all = new ArrayList<>(personnelService.getAllPersons());
        all.sort(Comparator.comparing(Person::getCode, String.CASE_INSENSITIVE_ORDER));
        Pagination<Person> pagination = new Pagination<>(all, 8);
        if (pagination.isEmpty()) {
            throw new exception.DataValidationException("No people are available for attendance.");
        }
        while (true) {
            ConsoleUi.clearScreen();
            ConsoleUi.breadcrumb("RECORD ATTENDANCE > SELECT PERSON | PAGE " + pagination.pageNumber() + "/" + pagination.pageCount());
            showPersonChoices(pagination.currentPage(), (pagination.pageNumber() - 1) * pagination.pageSize());
            String command = InputHelper.getString(
                    scanner, "Select Index, N next, P previous, F filter, L last, 0 cancel: ");
            if (command.equalsIgnoreCase("N")) {
                pagination.next();
            } else if (command.equalsIgnoreCase("P")) {
                pagination.previous();
            } else if (command.equalsIgnoreCase("L")) {
                pagination.last();
            } else if (command.equalsIgnoreCase("F")) {
                String nextFilter = InputHelper.getString(scanner, "Filter by ID, code or name: ");
                List<Person> filtered = filterPeople(all, nextFilter.trim().toLowerCase());
                if (filtered.isEmpty()) {
                    System.out.println("[WARN] No person matched that filter.");
                } else {
                    pagination = new Pagination<>(filtered, 8);
                }
            } else if (command.equals("0")) {
                throw new exception.DataValidationException("Attendance selection cancelled.");
            } else {
                try {
                    int index = Integer.parseInt(command);
                    int first = (pagination.pageNumber() - 1) * pagination.pageSize();
                    List<Person> page = pagination.currentPage();
                    int offset = index - first - 1;
                    if (offset < 0 || offset >= page.size()) {
                        throw new NumberFormatException();
                    }
                    return page.get(offset).getCode();
                } catch (NumberFormatException e) {
                    System.out.println("[WARN] Select a valid index from the current page.");
                }
            }
        }
    }

    private List<Person> filterPeople(List<Person> people, String filter) {
        List<Person> matches = new ArrayList<>();
        for (Person person : people) {
            if (filter == null || filter.isEmpty()
                    || person.getId().toLowerCase().contains(filter)
                    || person.getCode().toLowerCase().contains(filter)
                    || person.getName().toLowerCase().contains(filter)) {
                matches.add(person);
            }
        }
        return matches;
    }

    private void showPersonChoices(List<Person> people, int offset) {
        String[] headers = { "INDEX", "CODE", "TYPE", "NAME", "STATUS" };
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < people.size(); i++) {
            Person person = people.get(i);
            rows.add(new String[] {
                    String.valueOf(offset + i + 1),
                    person.getCode(),
                    person.getClass().getSimpleName(),
                    person.getName(),
                    person.isActive() ? "ACTIVE" : "INACTIVE"
            });
        }
        ConsoleUi.table(headers, rows);
    }

    private String recordAttendanceFlow(Scanner scanner) {
        try {
            String personCode = personForRecording(scanner);
            Zone zone = chooseZone(scanner);
            if (zone == null) {
                return null;
            }
            String zoneId = zone.getZoneId();

            ConsoleUi.menu("SELECT ATTENDANCE ACTION", "1. CHECK-IN (Enter Zone)", "2. CHECK-OUT (Leave Zone)");
            int typeChoice = InputHelper.getInt(scanner, "Choose action (1-2, 0-Cancel): ", 0, 2);
            if (typeChoice == 0) {
                return null;
            }
            AttendanceRecord.CheckType type = (typeChoice == 1)
                    ? AttendanceRecord.CheckType.CHECK_IN
                    : AttendanceRecord.CheckType.CHECK_OUT;

            attendanceService.recordAttendance(personCode, zoneId, type);
            System.out.println("Attendance recorded successfully: " + type + " at zone " + zoneId);
            personnelService.findPersonByCode(personCode).displayDetails();
            return "[OK] Attendance recorded successfully: " + type + " for " + personCode + " at zone " + zoneId;
        } catch (exception.SiteManagementException e) {
            System.out.println("Error: " + e.getMessage());
            return "[ERROR] " + e.getMessage();
        }
    }

    private Zone chooseZone(Scanner scanner) {
        while (true) {
            String zoneId = InputHelper.getString(scanner, "Enter Zone ID (e.g., Z01): ");
            if (zoneId.equals("0")) {
                return null;
            }
            try {
                return attendanceService.getZoneService().findZoneById(zoneId);
            } catch (exception.NotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
