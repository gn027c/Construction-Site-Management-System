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
            boolean canChangeType = authentication.allows(Permission.MANAGE_ACCOUNTS);
            MenuView.showPersonMenu(people, canChangeType);
            int max = canChangeType ? 5 : 4;
            int choice = InputHelper.getMenuChoice(scanner, "Enter option (0-" + max + "): ", max);
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
                    try {
                        String account = site.linkedUsername(deleted);
                        String target = "person '" + deleted + "'"
                                + (account.isEmpty() ? "" : " and linked account '" + account + "'");
                        if (!InputHelper.confirm(scanner, "Are you sure you want to delete " + target + "?")) {
                            view.ConsoleTheme.warning("Deletion cancelled.");
                            break;
                        }
                        site.deletePerson(deleted);
                        view.ConsoleTheme.success("Deleted " + target + ".");
                    } catch (exception.InputCancelledException e) {
                        throw e;
                    } catch (Exception e) {
                        view.ConsoleTheme.error("Could not delete person: " + e.getMessage());
                    }
                    break;
                case 5:
                    changeTypeFlow(scanner);
                    break;
                case 0:
                    return;
            }
        }
    }

    private void changeTypeFlow(Scanner scanner) {
        authentication.require(Permission.MANAGE_ACCOUNTS);
        view.ConsoleTheme.breadcrumb("PERSONNEL / CHANGE TYPE AND ACCOUNT ROLE");
        Person p;
        try {
            p = site.person(InputHelper.getString(scanner, "Person code: "));
        } catch (PersonNotFoundException e) {
            view.ConsoleTheme.error(e.getMessage());
            return;
        }
        Account.Role currentRole = site.linkedAccountRole(p.getCode());
        System.out.println("Current: " + p.getCode() + " " + p.getName() + " is " + p.getRole()
                + (currentRole == null ? ", no account." : ", account role " + currentRole + "."));
        System.out.println("The code " + p.getCode() + " and the history stay unchanged.");
        System.out.println("New type: 1. Worker │ 2. Contractor │ 3. Staff");
        int type = InputHelper.getMenuChoice(scanner, "Choice (1-3, 0-Cancel): ", 3);
        if (type == 0) {
            return;
        }
        Person replacement;
        switch (type) {
            case 1:
                String trade = selectWorkerTrade(scanner);
                String certId = selectSafetyCertificate(scanner, p.getCode());
                String team = InputHelper.getString(scanner, "Enter Assigned Team: ");
                replacement = new Worker(p.getId(), p.getCode(), p.getName(), p.getPhoneNumber(), trade, certId, team);
                break;
            case 2:
                String company = InputHelper.getString(scanner, "Enter Subcontractor Company Name: ");
                String contractId = InputHelper.getString(scanner, "Enter Contract ID (e.g., CTR-2026-001): ");
                replacement = new Contractor(
                        p.getId(), p.getCode(), p.getName(), p.getPhoneNumber(), company, contractId
                );
                break;
            default:
                String position = selectStaffPosition(scanner);
                String certificate = selectSafetyCertificate(scanner, p.getCode());
                replacement = new model.person.Staff(
                        p.getId(), p.getCode(), p.getName(), p.getPhoneNumber(), position, certificate
                );
                break;
        }
        if (replacement.getRole() == p.getRole()) {
            view.ConsoleTheme.warning(p.getCode() + " is already " + p.getRole() + ".");
            return;
        }
        if (replacement.hasValidSafetyCredential()) {
            replacement.setSafetyLevel(selectSafetyLevel(scanner, model.common.SafetyLevel.NONE));
        }
        Account.Role role = selectAccountRole(scanner, currentRole);
        if (!InputHelper.confirm(scanner, "Change " + p.getCode() + " from " + p.getRole() + " to "
                + replacement.getRole() + " with account role " + role + "?")) {
            view.ConsoleTheme.warning("Type change cancelled.");
            return;
        }
        try {
            String username = site.changePersonType(p.getCode(), replacement, role);
            view.ConsoleTheme.success(p.getCode() + " is now " + replacement.getRole() + "; account '" + username
                    + "' has role " + role + " with its default permissions.");
            if (currentRole == null) {
                view.ConsoleTheme.success("Account provisioned: " + username
                        + " (Initial password: 0, mustChangePassword: true)");
            }
        } catch (exception.SiteManagementException e) {
            view.ConsoleTheme.error(e.getMessage());
        }
    }

    private Account.Role selectAccountRole(Scanner scanner, Account.Role current) {
        Account.Role[] roles = { Account.Role.SITE_USER, Account.Role.SAFETY_OFFICER, Account.Role.SITE_MANAGER };
        System.out.println("Account role (required" + (current == null ? "" : ", current " + current) + "):");
        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i]);
        }
        return roles[InputHelper.getInt(scanner, "Role (1-3): ", 1, 3) - 1];
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
        String[] labels = p.detailLabels();
        String[] details = p.detailValues();
        for (int i = 0; i < labels.length; i++) {
            details[i] = getUpdatedDetail(scanner, p, i, labels[i], details[i]);
        }
        Account.Role newRole = null;
        if (p instanceof model.person.Staff && !details[0].equals(((model.person.Staff) p).getPosition())) {
            Account.Role currentRole = site.linkedAccountRole(p.getCode());
            if (currentRole != null) {
                if (authentication.allows(Permission.MANAGE_ACCOUNTS)) {
                    System.out.println("A new position needs a matching account role.");
                    newRole = selectAccountRole(scanner, currentRole);
                } else {
                    view.ConsoleTheme.warning("A new position also changes the account role; ask an administrator."
                            + " Position kept: " + ((model.person.Staff) p).getPosition());
                    details[0] = ((model.person.Staff) p).getPosition();
                }
            }
        }
        model.common.SafetyLevel newLevel = p.hasValidSafetyCredential(details)
                ? selectSafetyLevel(scanner, p.getSafetyLevel())
                : model.common.SafetyLevel.NONE;
        if (!p.hasValidSafetyCredential(details) && p.getSafetyLevel() != model.common.SafetyLevel.NONE) {
            view.ConsoleTheme.warning("No valid safety credential: safety level returns to NONE.");
        }
        try {
            site.updatePersonProfile(p.getCode(), newName, newPhone, details, newLevel, newRole);
            view.ConsoleTheme.success("Success: Person '" + p.getCode() + "' has been updated."
                    + (newRole == null ? "" : " Account role: " + newRole + "."));
        } catch (PersonNotFoundException e) {
            view.ConsoleTheme.error("Error: Person not found. " + e.getMessage());
        } catch (exception.DataValidationException e) {
            view.ConsoleTheme.error("Error: " + e.getMessage());
        }
    }

    private String getUpdatedDetail(Scanner scanner, Person p, int index, String label, String current) {
        String shown = current.isEmpty() ? "not set" : current;
        boolean clearable;
        try {
            p.validateDetail(index, "");
            clearable = true;
        } catch (exception.DataValidationException e) {
            clearable = false;
        }
        String prompt = "New " + label + " [" + shown + "]" + (clearable ? " (- clears)" : "") + ": ";
        while (true) {
            String input = InputHelper.readLine(scanner, prompt).trim();
            if (input.isEmpty()) {
                return current;
            }
            try {
                return p.validateDetail(index, input.equals("-") ? "" : input);
            } catch (exception.DataValidationException e) {
                view.ConsoleTheme.warning(e.getMessage());
            }
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
