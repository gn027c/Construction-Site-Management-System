package controller;

import exception.DuplicatePersonCodeException;
import exception.InvalidPhoneNumberException;
import exception.PersonNotFoundException;
import model.Contractor;
import model.Person;
import model.Visitor;
import model.Worker;
import service.PersonnelService;
import service.AuthenticationService;
import model.Permission;
import view.MenuView;
import util.InputHelper;
import java.util.List;
import java.util.Scanner;

/**
 * Handles console input and result messages for the personnel menu.
 *
 * <p>Operations are delegated to {@link service.PersonnelService}. The optional
 * save callback runs after a modifying menu choice, including choices that are
 * cancelled or report a handled error.
 */
public class PersonnelController {
    private final PersonnelService personnelService;
    private final AuthenticationService authentication;

    public PersonnelController(PersonnelService personnelService, AuthenticationService authentication) {
        this.personnelService = personnelService;
        this.authentication = authentication;
    }

    public void handleMenu(Scanner scanner) {
        handleMenu(scanner, () -> {
        });
    }

    /**
     * Runs the submenu and calls the supplied action after modifying menu choices.
     *
     * <p>The callback runs after the flow returns, including cancellation or handled
     * errors. Exceptions escaping the flow prevent the callback from running.
     */
    public void handleMenu(Scanner scanner, Runnable saveChanges) {
        authentication.require(Permission.MANAGE_PERSONNEL);
        boolean back = false;
        while (!back) {
            MenuView.showPersonMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-3): ", 0, 3);
            switch (choice) {
                case 1:
                    addPersonFlow(scanner);
                    break;
                case 2:
                    displayPersonListFlow();
                    break;
                case 3:
                    searchAndUpdatePersonFlow(scanner);
                    break;
                case 0:
                    back = true;
                    break;
            }
            if (choice == 1 || choice == 3) {
                saveChanges.run();
            }
        }
    }

    private void addPersonFlow(Scanner scanner) {
        System.out.println("\n--- ADD NEW PERSON ---");
        System.out.println("Select Person Type:");
        System.out.println("1. Worker (Công nhân thi công)");
        System.out.println("2. Contractor (Nhà thầu phụ)");
        System.out.println("3. Visitor (Khách tham quan / Thanh tra)");
        int type = InputHelper.getInt(scanner, "Choice (1-3, 0-Cancel): ", 0, 3);
        if (type == 0) {
            return;
        }
        String id = InputHelper.getString(scanner, "Enter ID (e.g., ID001): ");
        String code = InputHelper.getString(scanner, "Enter Person Code (e.g., W01, C01, V01): ");
        String name = InputHelper.getString(scanner, "Enter Full Name: ");
        String phone = InputHelper.getString(scanner, "Enter Phone Number (10 digits starting with 0): ");
        try {
            Person newPerson;
            switch (type) {
                case 1:
                    String trade = InputHelper.getString(scanner, "Enter Trade/Skill (e.g., Welder, Electrician): ");
                    String certId = InputHelper.getString(scanner, "Enter Safety Cert ID (e.g., CERT01_SAFE): ");
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
                default:
                    return;
            }
            if (personnelService.addPerson(newPerson)) {
                view.ConsoleTheme.success("Success: Person '" + name + "' (" + code + ") has been added.");
            } else {
                System.out.println("Could not add the person. Please check the entered data.");
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

    /**
     * Displays personnel through each subtype's own detail method.
     */
    private void displayPersonListFlow() {
        System.out.println("\n--- PERSONNEL LIST ---");
        List<Person> list = personnelService.getAllPersons();
        if (list.isEmpty()) {
            System.out.println("No personnel available.");
            return;
        }
        int index = 1;
        for (Person p : list) {
            System.out.println("\n#" + index++);
            p.displayDetails();
        }
    }

    /**
     * Finds personnel and accepts contact changes, retaining fields left blank by the user.
     */
    private void searchAndUpdatePersonFlow(Scanner scanner) {
        System.out.println("\n--- SEARCH & UPDATE PERSON ---");
        String code = InputHelper.getString(scanner, "Enter Person Code to search: ");
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
            view.ConsoleTheme.success("Success: Person '" + code + "' has been updated.");
        } catch (PersonNotFoundException e) {
            view.ConsoleTheme.error("Error: No person found with code '" + code + "'. " + e.getMessage());
        } catch (InvalidPhoneNumberException e) {
            view.ConsoleTheme.error(
                    "Error: Invalid phone number. It must have 10 digits and start with 0. " + e.getMessage()
            );
        }
    }
}
