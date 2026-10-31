package controller;

import exception.DuplicatePersonCodeException;
import exception.InvalidPhoneNumberException;
import exception.PersonNotFoundException;
import model.person.Contractor;
import model.person.Person;
import model.person.Visitor;
import model.person.Worker;
import model.auth.Account;
import service.AuthenticationService;
import model.auth.Permission;
import view.MenuView;
import view.PersonnelView;
import util.InputHelper;
import java.util.List;
import java.util.Scanner;

public class PersonnelController {
    private final SiteManagerController site;
    private final AuthenticationService authentication;

    public PersonnelController(SiteManagerController site) {
        this.site = site;
        this.authentication = site.getAuthentication();
    }

    public void handleMenu(Scanner scanner) {
        authentication.require(Permission.MANAGE_PERSONNEL);
        while (true) {
            List<Person> people = site.people();
            if (people != null) {
                people.sort(java.util.Comparator.comparing(Person::getCode, util.IdentifierHelper.NATURAL_ORDER));
            }
            MenuView.showPersonMenu(people);
            int choice = InputHelper.getMenuChoice(scanner, "Enter option (0-4): ", 4);
            view.ConsoleScreen.begin();
            switch (choice) {
                case 1:
                    addPersonFlow(scanner);
                    break;
                case 2:
                    viewAndUpdatePersonnelFlow(scanner);
                    break;
                case 3:
                    String code = InputHelper.getString(scanner, "Person code: ");
                    boolean active = InputHelper.getInt(scanner, "1-Activate, 0-Deactivate: ", 0, 1) == 1;
                    try {
                        site.setPersonActive(code, active);
                        view.ConsoleTheme.success("Personnel status saved.");
                    } catch (PersonNotFoundException e) {
                        view.ConsoleTheme.error("Person not found: " + e.getMessage());
                    }
                    break;
                case 4:
                    String deleted = InputHelper.getString(scanner, "Unreferenced person code to delete: ");
                    if (!InputHelper.confirm(scanner, "Are you sure you want to delete person '" + deleted + "'?")) {
                        view.ConsoleTheme.warning("Deletion cancelled.");
                        break;
                    }
                    try {
                        site.deletePerson(deleted);
                        view.ConsoleTheme.success("Person deleted.");
                    } catch (Exception e) {
                        view.ConsoleTheme.error("Could not delete person: " + e.getMessage());
                    }
                    break;
                case 0:
                    return;
            }
        }
    }

    private void addPersonFlow(Scanner scanner) {
        view.ConsoleTheme.breadcrumb("PERSONNEL / ADD NEW PERSON");
        System.out.println("Select Person Type:");
        System.out.println("1. Worker");
        System.out.println("2. Contractor");
        System.out.println("3. Visitor");
        System.out.println("4. Staff (Internal staff)");
        int type = InputHelper.getMenuChoice(scanner, "Choice (1-4, 0-Cancel): ", 4);
        if (type == 0) {
            return;
        }
        String id = site.suggestNextPersonId();
        String code = site.suggestNextPersonCode(type);
        System.out.println(view.ConsoleTheme.color("90", "  [Auto-generated] ID: ") + view.ConsoleTheme.color("1;37", id)
                + view.ConsoleTheme.color("90", " │ Code: ") + view.ConsoleTheme.color("1;37", code));

        String name = InputHelper.getPersonName(scanner, "Enter Full Name: ");
        String phone = InputHelper.getPhoneNumber(scanner, "Enter Phone Number (10 digits starting with 0): ");
        try {
            Person newPerson;
            switch (type) {
                case 1:
                    String trade = selectWorkerTrade(scanner);
                    String certId = selectSafetyCertificate(scanner, code);
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
                    String entryDate = InputHelper.getDate(
                            scanner,
                            "Enter Entry Date (" + util.DateTimeHelper.DATE_FORMAT + "): "
                    );
                    newPerson = new Visitor(id, code, name, phone, purpose, host, entryDate);
                    break;
                case 4:
                    String position = selectStaffPosition(scanner);
                    String certificate = selectSafetyCertificate(scanner, code);
                    newPerson = new model.person.Staff(id, code, name, phone, position, certificate);
                    break;
                default:
                    return;
            }
            if (newPerson.hasValidSafetyCredential()) {
                newPerson.setSafetyLevel(selectSafetyLevel(scanner, model.common.SafetyLevel.NONE));
            }
            if (site.addPerson(newPerson)) {
                view.ConsoleTheme.success("Success: Person '" + name + "' (" + code + ") has been added.");
                if (newPerson.shouldProvisionAccount()) {
                    try {
                        Account account = site.provisionAccount(newPerson);
                        view.ConsoleTheme.success("Account provisioned: " + account.getUsername()
                                + " (Initial password: 0, mustChangePassword: true)");
                    } catch (Exception e) {
                        view.ConsoleTheme.warning("Could not auto-provision account: " + e.getMessage());
                    }
                }
            } else {
                view.ConsoleTheme.warning("Could not add the person. Please check the entered data.");
            }
        } catch (DuplicatePersonCodeException e) {
            view.ConsoleTheme.error("Error: Person code '" + code + "' already exists. " + e.getMessage());
        } catch (InvalidPhoneNumberException e) {
            view.ConsoleTheme.error(
                    "Error: Invalid phone number. It must have 10 digits and start with 0. " + e.getMessage()
            );
        } catch (IllegalArgumentException e) {
            view.ConsoleTheme.error("Error: Invalid input data. " + e.getMessage());
        }
    }

    private void viewAndUpdatePersonnelFlow(Scanner scanner) {
        List<Person> people = site.people();
        view.ListView.showTable(
                scanner,
                "PERSONNEL",
                "View, search and update personnel records.",
                people,
                PersonnelView.TABLE_HEADERS,
                PersonnelView::tableRow,
                PersonnelView::formatSummary,
                java.util.Comparator.comparing(Person::getCode, util.IdentifierHelper.NATURAL_ORDER),
                person -> editPerson(scanner, person)
        );
    }

    private void editPerson(Scanner scanner, Person p) {
        PersonnelView.showDetails(p);
        System.out.println("Enter new details for " + p.getName() + " (" + p.getCode() + ") [press Enter to keep current value]:");
        String newName = getUpdatedPersonName(scanner, p.getName());
        String newPhone = getUpdatedPhoneNumber(scanner, p.getPhoneNumber());
        model.common.SafetyLevel newLevel = p.hasValidSafetyCredential()
                ? selectSafetyLevel(scanner, p.getSafetyLevel())
                : model.common.SafetyLevel.NONE;
        String newVisitDate = p instanceof Visitor ? getUpdatedVisitDate(scanner, ((Visitor) p).getEntryDate()) : "";
        try {
            site.updatePerson(p.getCode(), newName, newPhone);
            if (newLevel != p.getSafetyLevel()) {
                site.setPersonSafetyLevel(p.getCode(), newLevel);
            }
            if (p instanceof Visitor && !newVisitDate.equals(((Visitor) p).getEntryDate())) {
                site.setVisitDate(p.getCode(), newVisitDate);
            }
            view.ConsoleTheme.success("Success: Person '" + p.getCode() + "' has been updated.");
        } catch (PersonNotFoundException e) {
            view.ConsoleTheme.error("Error: Person not found. " + e.getMessage());
        } catch (InvalidPhoneNumberException e) {
            view.ConsoleTheme.error("Error: Invalid phone number. " + e.getMessage());
        }
    }

    private String getUpdatedPersonName(Scanner scanner, String currentName) {
        while (true) {
            String input = InputHelper.readLine(scanner, "New Name [" + currentName + "]: ").trim();
            if (input.isEmpty()) {
                return currentName;
            }
            if (input.length() < 2) {
                view.ConsoleTheme.warning("Full name must be at least 2 characters.");
                continue;
            }
            if (input.matches("^\\d+$")) {
                view.ConsoleTheme.warning("Full name cannot be pure numbers. Please enter a valid name.");
                continue;
            }
            if (input.equals(input.toLowerCase()) || input.equals(input.toUpperCase())) {
                return InputHelper.capitalizeName(input);
            }
            return input;
        }
    }

    private String getUpdatedPhoneNumber(Scanner scanner, String currentPhone) {
        while (true) {
            String input = InputHelper.readLine(scanner, "New Phone [" + currentPhone + "]: ").trim();
            if (input.isEmpty()) {
                return currentPhone;
            }
            if (!input.matches("^0\\d{9}$")) {
                view.ConsoleTheme.warning("Invalid phone number. It must have 10 digits and start with 0.");
                continue;
            }
            return input;
        }
    }

    private String getUpdatedVisitDate(Scanner scanner, String currentDate) {
        String shown = currentDate.isEmpty() ? "not set" : currentDate;
        while (true) {
            String input = InputHelper.readLine(
                    scanner, "New visit date (" + util.DateTimeHelper.DATE_FORMAT + ") [" + shown + "]: "
            ).trim();
            if (input.isEmpty()) {
                return currentDate;
            }
            try {
                InputHelper.validateDate(input, "visitDate");
                return input;
            } catch (exception.DataValidationException e) {
                view.ConsoleTheme.warning(e.getMessage());
            }
        }
    }

    private String selectWorkerTrade(Scanner scanner) {
        System.out.println("Select Worker Trade:");
        System.out.println("1. Welder");
        System.out.println("2. Electrician");
        System.out.println("3. Mason / Builder");
        System.out.println("4. Carpenter");
        System.out.println("5. Steel Fixer / Ironworker");
        System.out.println("6. Crane Operator");
        System.out.println("7. General Laborer");
        System.out.println("8. Other (Enter custom trade)");
        String input = InputHelper.readLine(scanner, "Choice (1-8 or custom trade): ").trim();
        switch (input) {
            case "1": return "Welder";
            case "2": return "Electrician";
            case "3": return "Mason";
            case "4": return "Carpenter";
            case "5": return "Steel Fixer";
            case "6": return "Crane Operator";
            case "7": return "General Laborer";
            case "8": return InputHelper.getString(scanner, "Enter Trade: ");
            default:
                if (!input.isEmpty()) return input;
                return InputHelper.getString(scanner, "Enter Trade/Skill (e.g., Welder, Electrician): ");
        }
    }

    private String selectStaffPosition(Scanner scanner) {
        System.out.println("Select Staff Position:");
        System.out.println("1. Project Manager");
        System.out.println("2. Site Engineer");
        System.out.println("3. Safety Officer");
        System.out.println("4. Site Supervisor");
        System.out.println("5. QA/QC Engineer");
        System.out.println("6. MEP Engineer");
        System.out.println("7. Other (Enter custom position)");
        String input = InputHelper.readLine(scanner, "Choice (1-7 or custom title): ").trim();
        switch (input) {
            case "1": return "Project Manager";
            case "2": return "Site Engineer";
            case "3": return "Safety Officer";
            case "4": return "Site Supervisor";
            case "5": return "QA/QC Engineer";
            case "6": return "MEP Engineer";
            case "7": return InputHelper.getString(scanner, "Enter Custom Position: ");
            default:
                if (!input.isEmpty()) return input;
                return InputHelper.getString(scanner, "Enter Position: ");
        }
    }

    private model.common.SafetyLevel selectSafetyLevel(Scanner scanner, model.common.SafetyLevel current) {
        model.common.SafetyLevel[] levels = model.common.SafetyLevel.values();
        System.out.println("Safety level for restricted zones: 0. NONE │ 1. LOW │ 2. MEDIUM │ 3. HIGH │ 4. CRITICAL");
        while (true) {
            String input = InputHelper.readLine(scanner, "Choice (0-4) [" + current + "]: ").trim();
            if (input.isEmpty()) {
                return current;
            }
            if (input.matches("[0-4]")) {
                return levels[Integer.parseInt(input)];
            }
            view.ConsoleTheme.warning("Choose a number from 0 to 4, or press Enter to keep " + current + ".");
        }
    }

    private String selectSafetyCertificate(Scanner scanner, String personCode) {
        System.out.println("Select Safety Certification Status:");
        System.out.println("1. Standard Safety Certified (Valid credential)");
        System.out.println("2. Uncertified / Pending Training (UNSAFE)");
        System.out.println("3. Custom Certificate ID (Type code)");
        System.out.println("4. None / Exempt (Leave blank)");
        String input = InputHelper.readLine(scanner, "Choice (1-4, blank-None): ").trim();
        switch (input) {
            case "1":
                return "CERT_" + personCode.toUpperCase() + "_SAFE";
            case "2":
                return "CERT_UNSAFE";
            case "3":
                return InputHelper.getString(scanner, "Enter Certificate ID: ");
            case "4":
            case "":
                return "";
            default:
                return input;
        }
    }
}
