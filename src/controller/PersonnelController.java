package controller;

import exception.DuplicatePersonCodeException;
import exception.InvalidPhoneNumberException;
import exception.PersonNotFoundException;
import model.Contractor;
import model.Person;
import model.Visitor;
import model.Worker;
import service.PersonnelService;
import view.MenuView;
import util.InputHelper;
import java.util.List;
import java.util.Scanner;

/**
 * TẦNG ĐIỀU KHIỂN (CONTROLLER LAYER)
 * PHỤ TRÁCH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Điều hướng luồng tương tác Console cho Menu Quản lý Nhân sự.
 * - Thu thập input, gọi PersonnelService, bọc try-catch bắt ngoại lệ Workshop 4 và hiển thị kết quả.
 */
public class PersonnelController {
    private final PersonnelService personnelService;

    public PersonnelController(PersonnelService personnelService) {
        this.personnelService = personnelService;
    }

    public void handleMenu(Scanner scanner) {
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
        }
    }

    private void addPersonFlow(Scanner scanner) {
        System.out.println("\n--- ADD NEW PERSON ---");
        System.out.println("Select Person Type:");
        System.out.println("1. Worker (Công nhân thi công)");
        System.out.println("2. Contractor (Nhà thầu phụ)");
        System.out.println("3. Visitor (Khách tham quan / Thanh tra)");
        int type = InputHelper.getInt(scanner, "Choice (1-3, 0-Cancel): ", 0, 3);
        if (type == 0) return;

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
                    String entryDate = InputHelper.getString(scanner, "Enter Entry Date (dd/MM/yyyy): ");
                    newPerson = new Visitor(id, code, name, phone, purpose, host, entryDate);
                    break;
                default:
                    return;
            }

            personnelService.addPerson(newPerson);
        } catch (DuplicatePersonCodeException e) {
            System.out.println("Personnel Duplicate Error: " + e.getMessage());
        } catch (InvalidPhoneNumberException e) {
            System.out.println("Phone Validation Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Input Data Error: " + e.getMessage());
        }
    }

    private void displayPersonListFlow() {
        System.out.println("\n--- PERSONNEL LIST ---");
        List<Person> list = personnelService.getAllPersons();
        if (list.isEmpty()) {
            System.out.println("No personnel available.");
        } else {
            for (Person p : list) {
                p.displayDetails();
            }
        }
    }

    private void searchAndUpdatePersonFlow(Scanner scanner) {
        System.out.println("\n--- SEARCH & UPDATE PERSON ---");
        String code = InputHelper.getString(scanner, "Enter Person Code to search: ");
        try {
            Person p = personnelService.findPersonByCode(code);
            System.out.println("\n[Current Information]");
            p.displayDetails();

            System.out.println("\nEnter new details (or leave blank to keep unchanged):");
            System.out.print("Enter New Name [Press Enter to keep '" + p.getName() + "']: ");
            String newName = scanner.nextLine().trim();
            if (newName.isEmpty()) {
                newName = p.getName();
            }

            System.out.print("Enter New Phone [Press Enter to keep '" + p.getPhoneNumber() + "']: ");
            String newPhone = scanner.nextLine().trim();
            if (newPhone.isEmpty()) {
                newPhone = p.getPhoneNumber();
            }

            personnelService.updatePerson(code, newName, newPhone);
        } catch (PersonNotFoundException e) {
            System.out.println("Search Error: " + e.getMessage());
        } catch (InvalidPhoneNumberException e) {
            System.out.println("Phone Validation Error: " + e.getMessage());
        }
    }
}
