package controller;

import exception.DuplicatePersonCodeException;
import exception.InvalidPhoneNumberException;
import exception.PersonNotFoundException;
import model.Contractor;
import model.Person;
import model.Visitor;
import model.Worker;
import service.AuthenticationService;
import model.Permission;
import view.MenuView;
import view.PersonnelView;
import util.InputHelper;
import java.util.List;
import java.util.Scanner;

/**
 * Handles console input and result messages for the personnel menu.
 *
 * <p>Operations are delegated to the site facade and personnel rendering to the view.
 */
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
            MenuView.showPersonMenu();
            int choice = InputHelper.getMenuChoice(scanner, "Enter option (0-5): ", 5);
            view.ConsoleScreen.begin();
            switch (choice) {
                case 1:
                    addPersonFlow(scanner);
                    break;
                case 2:
                    List<Person> people = site.people();
                    view.ListView.show(
                            scanner,
                            "PERSONNEL",
                            people,
                            PersonnelView::formatSummary,
                            PersonnelView::formatSummary,
                            java.util.Comparator.comparing(Person::getCode, util.IdentifierHelper.NATURAL_ORDER),
                            event -> authentication.audit(event, "LIST", "", "SUCCESS", "Navigated list.")
                    );
                    break;
                case 3:
                    searchAndUpdatePersonFlow(scanner);
                    break;
                case 4:
                    String code = InputHelper.getString(scanner, "Person code: ");
                    boolean active = InputHelper.getInt(scanner, "1-Activate, 0-Deactivate: ", 0, 1) == 1;
                    site.setPersonActive(code, active);
                    view.ConsoleTheme.success("Personnel status saved.");
                    break;
                case 5:
                    String deleted = InputHelper.getString(scanner, "Unreferenced person code to delete: ");
                    site.deletePerson(deleted);
                    view.ConsoleTheme.success("Person deleted.");
                    break;
                case 0:
                    return;
            }
        }
    }

    private void addPersonFlow(Scanner scanner) {
        view.ConsoleTheme.title("ADD NEW PERSON");
        System.out.println("Select Person Type:");
        System.out.println("1. Worker");
        System.out.println("2. Contractor");
        System.out.println("3. Visitor");
        System.out.println("4. Staff (Internal staff)");
        int type = InputHelper.getMenuChoice(scanner, "Choice (1-4, 0-Cancel): ", 4);
        if (type == 0) {
            return;
        }
        String id = InputHelper.getString(scanner, "Enter ID (e.g., P1): ");
        String code = InputHelper.getString(scanner, "Enter Person Code (e.g., W1, C1, V1, S1): ");
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
                case 4:
                    String position = InputHelper.getString(scanner, "Position: ");
                    String certificate = InputHelper.readLine(
                            scanner, "Safety certificate (blank if unknown): "
                    ).trim();
                    newPerson = new model.Staff(id, code, name, phone, position, certificate);
                    break;
                default:
                    return;
            }
            if (site.addPerson(newPerson)) {
                view.ConsoleTheme.success("Success: Person '" + name + "' (" + code + ") has been added.");
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

    /**
     * Finds personnel and accepts contact changes, retaining fields left blank by the user.
     */
    private void searchAndUpdatePersonFlow(Scanner scanner) {
        view.ConsoleTheme.title("SEARCH AND UPDATE PERSON");
        String code = InputHelper.getString(scanner, "Enter Person Code to search: ");
        try {
            Person p = site.person(code);
            PersonnelView.showDetails(p);
            System.out.println("\nEnter new details (press Enter to keep the current value):");
            String newName = InputHelper.readLine(scanner, "New Name [" + p.getName() + "]: ").trim();
            if (newName.isEmpty()) {
                newName = p.getName();
            }
            String newPhone = InputHelper.readLine(scanner, "New Phone [" + p.getPhoneNumber() + "]: ").trim();
            if (newPhone.isEmpty()) {
                newPhone = p.getPhoneNumber();
            }
            site.updatePerson(code, newName, newPhone);
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
