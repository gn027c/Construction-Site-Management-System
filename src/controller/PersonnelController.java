package controller;

import exception.AppError;
import exception.DataValidationException;
import exception.NotFoundException;
import exception.SiteManagementException;
import model.Account;
import model.Contractor;
import model.Permission;
import model.Person;
import model.SafetyLevel;
import model.Visitor;
import model.Worker;
import service.AuthenticationService;
import service.PersonRemovalService;
import service.PersonnelService;
import view.ConsoleUi;
import view.Pagination;
import util.InputHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PersonnelController {
    private final PersonnelService personnelService;
    private final AuthenticationService authentication;
    private final PersonRemovalService removal;

    public PersonnelController(
            PersonnelService personnelService, AuthenticationService authentication, PersonRemovalService removal
    ) {
        this.personnelService = personnelService;
        this.authentication = authentication;
        this.removal = removal;
    }

    public void handleMenu(Scanner scanner) {
        authentication.require(Permission.MANAGE_PERSONNEL);
        String filter = "";
        String statusMessage = null;
        List<Person> people = fetchPeople();
        List<Person> filtered = filterPeople(people, filter);
        Pagination<Person> pagination = new Pagination<>(filtered, 8);
        boolean back = false;

        while (!back) {
            ConsoleUi.clearScreen();
            String breadcrumb = "PERSONNEL MANAGEMENT | PERSONNEL LIST | PAGE "
                    + (pagination.isEmpty() ? "1/1" : (pagination.pageNumber() + "/" + pagination.pageCount()));
            if (!filter.isEmpty()) {
                breadcrumb += " | Filter: \"" + filter + "\"";
            }
            ConsoleUi.breadcrumb(breadcrumb);

            showPeopleTable(pagination.currentPage());

            if (statusMessage != null) {
                System.out.println(statusMessage);
                statusMessage = null;
            }

            System.out.println("Actions: 1 (Add), 2 (Edit), 3 (Status), 4 (Safety), 5 (Visit Date), 6 (Position), 7 (Delete), 8 (Profile), 0 (Back)");
            String command = InputHelper.getString(
                    scanner, "Select [F (Filter), C (Clear filter), N (Next), P (Prev), L (Last)]: "
            );

            if (command.equalsIgnoreCase("1") || command.equalsIgnoreCase("add")) {
                statusMessage = addPersonFlow(scanner);
                people = fetchPeople();
                filtered = filterPeople(people, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("2") || command.equalsIgnoreCase("edit")) {
                statusMessage = searchAndUpdatePersonFlow(scanner);
                people = fetchPeople();
                filtered = filterPeople(people, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("3") || command.equalsIgnoreCase("status")) {
                statusMessage = setStatusFlow(scanner);
                people = fetchPeople();
                filtered = filterPeople(people, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("4") || command.equalsIgnoreCase("safety")) {
                statusMessage = setSafetyLevelFlow(scanner);
                people = fetchPeople();
                filtered = filterPeople(people, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("5") || command.equalsIgnoreCase("date")) {
                statusMessage = setVisitDateFlow(scanner);
                people = fetchPeople();
                filtered = filterPeople(people, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("6") || command.equalsIgnoreCase("position")) {
                statusMessage = changePositionFlow(scanner);
                people = fetchPeople();
                filtered = filterPeople(people, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("7") || command.equalsIgnoreCase("delete")) {
                statusMessage = deleteFlow(scanner);
                people = fetchPeople();
                filtered = filterPeople(people, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("8") || command.equalsIgnoreCase("9") || command.equalsIgnoreCase("profile")) {
                statusMessage = updateProfileFlow(scanner);
                people = fetchPeople();
                filtered = filterPeople(people, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("F")) {
                String term = InputHelper.getString(scanner, "Filter by ID, Code, Name, Type, Phone, Status or Safety: ");
                filter = term.trim().toLowerCase();
                filtered = filterPeople(people, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("C")) {
                filter = "";
                filtered = filterPeople(people, filter);
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
                System.out.println("[WARN] Invalid command. Choose 1-8, F, C, N, P, L or 0.");
            }
        }
    }

    private List<Person> fetchPeople() {
        return personnelService.getAllPersons();
    }

    private List<Person> filterPeople(List<Person> list, String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return new ArrayList<>(list);
        }
        String term = filter.trim().toLowerCase();
        List<Person> matches = new ArrayList<>();
        for (Person p : list) {
            boolean match = p.getId().toLowerCase().contains(term)
                    || p.getCode().toLowerCase().contains(term)
                    || p.getName().toLowerCase().contains(term)
                    || p.getClass().getSimpleName().toLowerCase().contains(term)
                    || p.getPhoneNumber().toLowerCase().contains(term)
                    || p.getPosition().toLowerCase().contains(term)
                    || (p.isActive() ? "active" : "inactive").contains(term)
                    || p.getSafetyLevel().name().toLowerCase().contains(term);
            if (match) {
                matches.add(p);
            }
        }
        return matches;
    }

    private void showPeopleTable(List<Person> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No personnel available.");
            return;
        }
        String[] headers = { "ID", "CODE", "TYPE", "NAME", "PHONE", "STATUS", "SAFETY" };
        List<String[]> rows = new ArrayList<>();
        for (Person p : list) {
            rows.add(new String[] {
                    p.getId(),
                    p.getCode(),
                    p.getClass().getSimpleName(),
                    p.getName(),
                    p.getPhoneNumber(),
                    p.isActive() ? "ACTIVE" : "INACTIVE",
                    p.getSafetyLevel().name()
            });
        }
        ConsoleUi.table(headers, rows);
    }

    private String addPersonFlow(Scanner scanner) {
        System.out.println("\n--- ADD NEW PERSON ---");
        ConsoleUi.menu("SELECT PERSON TYPE",
                "1. Worker (Công nhân thi công)",
                "2. Contractor (Nhà thầu phụ)",
                "3. Visitor (Khách tham quan / Thanh tra)");
        int type = InputHelper.getInt(scanner, "Choice (1-3, 0-Cancel): ", 0, 3);
        if (type == 0) {
            return null;
        }

        String rawId = InputHelper.getString(scanner, "Enter ID (e.g., ID001): ");
        String id = util.IdHelper.normalize(rawId);
        String rawCode = InputHelper.getString(scanner, "Enter Person Code (e.g., W01, C01, V01): ");
        String code = util.IdHelper.normalize(rawCode);
        String name = InputHelper.getString(scanner, "Enter Full Name: ");
        String phone = InputHelper.getString(scanner, "Enter Phone Number (10 digits starting with 0): ");

        try {
            Person newPerson;

            switch (type) {
                case 1:
                    String trade = InputHelper.getString(scanner, "Enter Trade/Skill (e.g., Welder, Electrician): ");
                    System.out.print("Enter Safety Cert ID (e.g., CERT01_SAFE, Enter if none): ");
                    String certId = scanner.nextLine().trim();
                    String team = InputHelper.getString(scanner, "Enter Assigned Team: ");
                    newPerson = new Worker(id, code, name, phone, trade, certId, team);
                    break;
                case 2:
                    String company = InputHelper.getString(scanner, "Enter Subcontractor Company Name: ");
                    String contractId = InputHelper.getString(scanner, "Enter Contract ID (e.g., CTR-2026-001): ");
                    newPerson = new Contractor(id, code, name, phone, company, contractId);
                    break;
                case 3:
                    String purpose = InputHelper.getString(scanner, "Enter Visit Purpose: ");
                    String host = InputHelper.getString(scanner, "Enter Host / Sponsor Person Name: ");
                    System.out.print("Enter Entry Date (dd/MM/yyyy, Enter if unknown): ");
                    String entryDate = scanner.nextLine().trim();
                    newPerson = new Visitor(id, code, name, phone, purpose, host, entryDate);
                    break;
                default:
                    return null;
            }

            if (personnelService.addPerson(newPerson)) {
                return "[OK] Person '" + name + "' (" + code + ") has been added.";
            } else {
                return "[WARN] Could not add the person. Please check the entered data.";
            }

        } catch (AppError | DataValidationException e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    private String searchAndUpdatePersonFlow(Scanner scanner) {
        System.out.println("\n--- SEARCH & UPDATE PERSON ---");
        String code = InputHelper.getString(scanner, "Enter Person Code to search (0 to cancel): ");
        if (code.equals("0")) {
            return null;
        }
        try {
            Person p = personnelService.findPersonByCode(code);
            System.out.println("\n[Current Information]");
            p.displayDetails();

            System.out.println("\nEnter new details (press Enter to keep the current value):");

            System.out.print("New Name [" + p.getName() + "]: ");
            String newName = scanner.nextLine().trim();
            if (newName.isEmpty()) {
                newName = p.getName();
            }

            System.out.print("New Phone [" + p.getPhoneNumber() + "]: ");
            String newPhone = scanner.nextLine().trim();
            if (newPhone.isEmpty()) {
                newPhone = p.getPhoneNumber();
            }

            personnelService.updatePerson(code, newName, newPhone);
            return "[OK] Person '" + code + "' has been updated.";

        } catch (NotFoundException e) {
            return "[ERROR] No person found with code '" + code + "'. " + e.getMessage();
        } catch (DataValidationException e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    private String setStatusFlow(Scanner scanner) {
        System.out.println("\n--- ACTIVATE / DEACTIVATE PERSON ---");
        String code = InputHelper.getString(scanner, "Enter Person Code (0 to cancel): ");
        if (code.equals("0")) {
            return null;
        }
        try {
            personnelService.findPersonByCode(code);
            int choice = InputHelper.getInt(scanner, "1. Activate | 2. Deactivate (0-Cancel): ", 0, 2);
            if (choice == 0) {
                return null;
            }
            personnelService.setActive(code, choice == 1);
            return "[OK] Status of '" + code + "' has been updated.";
        } catch (NotFoundException e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    private String setSafetyLevelFlow(Scanner scanner) {
        System.out.println("\n--- SET SAFETY LEVEL ---");
        String code = InputHelper.getString(scanner, "Enter Person Code (0 to cancel): ");
        if (code.equals("0")) {
            return null;
        }
        try {
            personnelService.findPersonByCode(code);
            SafetyLevel[] levels = SafetyLevel.values();
            String[] choices = new String[levels.length];
            for (int i = 0; i < levels.length; i++) {
                choices[i] = (i + 1) + ". " + levels[i];
            }
            ConsoleUi.menu("SELECT SAFETY LEVEL", choices);
            int choice = InputHelper.getInt(scanner, "Choose level (1-" + levels.length + ", 0-Cancel): ", 0, levels.length);
            if (choice == 0) {
                return null;
            }
            personnelService.setSafetyLevel(code, levels[choice - 1]);
            return "[OK] Safety level of '" + code + "' is now " + levels[choice - 1] + ".";
        } catch (NotFoundException | AppError e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    private String setVisitDateFlow(Scanner scanner) {
        System.out.println("\n--- CHANGE VISITOR DATE ---");
        String code = InputHelper.getString(scanner, "Enter Visitor Code (0 to cancel): ");
        if (code.equals("0")) {
            return null;
        }
        try {
            personnelService.findPersonByCode(code);
            String date = InputHelper.getString(scanner, "Enter New Visit Date (dd/MM/yyyy): ");
            personnelService.setVisitDate(code, date);
            return "[OK] Visit date of '" + code + "' is now " + date + ".";
        } catch (NotFoundException | AppError | DataValidationException e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    private String changePositionFlow(Scanner scanner) {
        System.out.println("\n--- CHANGE POSITION ---");
        String code = InputHelper.getString(scanner, "Enter Person Code (0 to cancel): ");
        if (code.equals("0")) {
            return null;
        }
        try {
            personnelService.findPersonByCode(code);
            System.out.print("New Position (Enter to clear): ");
            String position = scanner.nextLine().trim();
            Account.Role role = authentication.accountOf(code) == null
                    ? null : AccountController.chooseRole(scanner);
            authentication.changePosition(code, position, role);
            return "[OK] Position of '" + code + "' has been updated.";
        } catch (SiteManagementException e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    private String deleteFlow(Scanner scanner) {
        System.out.println("\n--- DELETE PERSON ---");
        String code = InputHelper.getString(scanner, "Enter Person Code to delete (0 to cancel): ");
        if (code.equals("0")) {
            return null;
        }
        try {
            personnelService.findPersonByCode(code);
            int choice = InputHelper.getInt(scanner, "Delete this person? 1. Yes | 0. Cancel: ", 0, 1);
            if (choice == 0) {
                return null;
            }
            removal.delete(code);
            return "[OK] Person '" + code + "' has been deleted.";
        } catch (SiteManagementException e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    private String updateProfileFlow(Scanner scanner) {
        System.out.println("\n--- UPDATE PROFILE ---");
        String code = InputHelper.getString(scanner, "Enter Person Code (0 to cancel): ");
        if (code.equals("0")) {
            return null;
        }
        try {
            Person person = personnelService.findPersonByCode(code);
            String[] labels = person.profileLabels();
            String[] values = person.profileValues();
            for (int i = 0; i < labels.length; i++) {
                System.out.print(labels[i] + " [" + values[i] + "]: ");
                String entered = scanner.nextLine().trim();
                if (entered.equals("-")) {
                    values[i] = "";
                } else if (!entered.isEmpty()) {
                    values[i] = entered;
                }
            }
            personnelService.updateProfile(code, values);
            return "[OK] Profile of '" + code + "' has been updated.";
        } catch (SiteManagementException e) {
            return "[ERROR] " + e.getMessage();
        }
    }
}
