package controller;

import exception.SiteManagementException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import model.Account;
import model.Permission;
import service.AuthenticationService;
import util.InputHelper;
import view.ConsoleUi;
import view.Pagination;

public class AccountController {
    private final AuthenticationService authentication;

    public AccountController(AuthenticationService authentication) {
        this.authentication = authentication;
    }

    public void handleMenu(Scanner scanner) {
        authentication.require(Permission.MANAGE_ACCOUNTS);
        String filter = "";
        String statusMessage = null;
        List<Account> accounts = fetchAccounts();
        List<Account> filtered = filterAccounts(accounts, filter);
        Pagination<Account> pagination = new Pagination<>(filtered, 8);
        boolean back = false;

        while (!back) {
            ConsoleUi.clearScreen();
            String breadcrumb = "ACCOUNT MANAGEMENT | PAGE "
                    + (pagination.isEmpty() ? "1/1" : (pagination.pageNumber() + "/" + pagination.pageCount()));
            if (!filter.isEmpty()) {
                breadcrumb += " | Filter: \"" + filter + "\"";
            }
            ConsoleUi.breadcrumb(breadcrumb);

            showAccountsTable(pagination.currentPage());

            if (statusMessage != null) {
                System.out.println(statusMessage);
                statusMessage = null;
            }

            System.out.println("Actions: 1 (Create), 3 (Change Role), 4 (Enable/Disable), 5 (Reset Password), 6 (Link/Unlink Person), 7 (Grant Perm), 8 (Revoke Perm), 9 (Delete), 0 (Back)");
            String command = InputHelper.getString(
                    scanner, "Select [F (Filter), C (Clear filter), N (Next), P (Prev), L (Last)]: ");

            if (command.equalsIgnoreCase("1") || command.equalsIgnoreCase("2")) {
                statusMessage = createFlow(scanner);
                accounts = fetchAccounts();
                filtered = filterAccounts(accounts, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("3")) {
                statusMessage = changeRoleFlow(scanner);
                accounts = fetchAccounts();
                filtered = filterAccounts(accounts, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("4")) {
                statusMessage = toggleStatusFlow(scanner);
                accounts = fetchAccounts();
                filtered = filterAccounts(accounts, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("5")) {
                statusMessage = resetPasswordFlow(scanner);
                accounts = fetchAccounts();
                filtered = filterAccounts(accounts, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("6")) {
                statusMessage = linkPersonFlow(scanner);
                accounts = fetchAccounts();
                filtered = filterAccounts(accounts, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("7")) {
                statusMessage = grantPermissionFlow(scanner);
                accounts = fetchAccounts();
                filtered = filterAccounts(accounts, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("8")) {
                statusMessage = revokePermissionFlow(scanner);
                accounts = fetchAccounts();
                filtered = filterAccounts(accounts, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("9") || command.equalsIgnoreCase("delete")) {
                statusMessage = deleteAccountFlow(scanner);
                accounts = fetchAccounts();
                filtered = filterAccounts(accounts, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("F")) {
                String term = InputHelper.getString(scanner, "Filter by Username, Role, Status, or Person: ");
                filter = term.trim().toLowerCase();
                filtered = filterAccounts(accounts, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("C")) {
                filter = "";
                filtered = filterAccounts(accounts, filter);
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
                System.out.println("[WARN] Invalid command. Choose 1, 3-8, F, C, N, P, L or 0.");
            }
        }
    }

    private List<Account> fetchAccounts() {
        return authentication.listAccounts();
    }

    private List<Account> filterAccounts(List<Account> accounts, String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return new ArrayList<>(accounts);
        }
        String term = filter.trim().toLowerCase();
        List<Account> matches = new ArrayList<>();
        for (Account account : accounts) {
            boolean match = account.getUsername().toLowerCase().contains(term)
                    || account.getRole().name().toLowerCase().contains(term)
                    || (account.isEnabled() ? "enabled".contains(term) : "disabled".contains(term))
                    || (account.isLinked() && account.getPersonCode().toLowerCase().contains(term));
            if (match) {
                matches.add(account);
            }
        }
        return matches;
    }

    private void showAccountsTable(List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }
        String[] headers = { "USERNAME", "ROLE", "STATUS", "PERSON" };
        List<String[]> rows = new ArrayList<>();
        for (Account account : accounts) {
            rows.add(new String[] {
                    account.getUsername(),
                    account.getRole().name(),
                    account.isEnabled() ? "ENABLED" : "DISABLED",
                    account.isLinked() ? account.getPersonCode() : "-"
            });
        }
        ConsoleUi.table(headers, rows);
    }

    private String createFlow(Scanner scanner) {
        try {
            String username = username(scanner);
            String password = InputHelper.getString(scanner, "Initial password: ");
            Account.Role role = chooseRole(scanner);
            String personCode = optional(scanner, "Person code (Enter for none): ");
            String position = personCode.isEmpty()
                    ? "" : optional(scanner, "Position (needed for management roles, Enter to skip): ");
            authentication.createAccount(username, password, role, personCode, position);
            System.out.println("Success: Account created.");
            return "[OK] Account created: " + username;
        } catch (SiteManagementException e) {
            System.out.println("Error: " + e.getMessage());
            return "[ERROR] " + e.getMessage();
        }
    }

    private String changeRoleFlow(Scanner scanner) {
        try {
            String target = username(scanner);
            Account.Role role = chooseRole(scanner);
            authentication.changeRole(target, role);
            System.out.println("Success: Role updated.");
            return "[OK] Role updated for " + target + " to " + role;
        } catch (SiteManagementException e) {
            System.out.println("Error: " + e.getMessage());
            return "[ERROR] " + e.getMessage();
        }
    }

    private String toggleStatusFlow(Scanner scanner) {
        try {
            String target = username(scanner);
            int status = InputHelper.getInt(scanner, "1. Enable | 2. Disable: ", 1, 2);
            authentication.setEnabled(target, status == 1);
            System.out.println("Success: Status updated.");
            return "[OK] Status updated for " + target;
        } catch (SiteManagementException e) {
            System.out.println("Error: " + e.getMessage());
            return "[ERROR] " + e.getMessage();
        }
    }

    private String resetPasswordFlow(Scanner scanner) {
        try {
            String target = username(scanner);
            String newPassword = InputHelper.getString(scanner, "New password: ");
            authentication.resetPassword(target, newPassword);
            System.out.println("Success: The user must change this password at the next sign-in.");
            return "[OK] Password reset for " + target;
        } catch (SiteManagementException e) {
            System.out.println("Error: " + e.getMessage());
            return "[ERROR] " + e.getMessage();
        }
    }

    private String linkPersonFlow(Scanner scanner) {
        try {
            String target = username(scanner);
            Account account = authentication.findAccount(target);
            if (account == null) {
                return "[ERROR] Account '" + target + "' not found.";
            }
            if (account.isLinked()) {
                System.out.println("Account '" + account.getUsername() + "' is currently linked to person '"
                        + account.getPersonCode() + "'.");
                ConsoleUi.menu("SELECT LINK ACTION",
                        "1. Change linked person (Re-link to another person)",
                        "2. Unlink person (Remove link)");
                int choice = InputHelper.getInt(scanner, "Choice (1-2, 0-Cancel): ", 0, 2);
                if (choice == 0) {
                    return null;
                }
                if (choice == 1) {
                    String newPersonCode = InputHelper.getString(scanner, "New Person Code (0 to cancel): ");
                    if (newPersonCode.equals("0")) {
                        return null;
                    }
                    String position = account.getRole().requiresPosition()
                            ? optional(scanner, "Position (needed for management roles, Enter to skip): ") : "";
                    authentication.relinkPerson(target, newPersonCode, position);
                    return "[OK] Account '" + target + "' is now linked to '" + newPersonCode + "'.";
                } else if (choice == 2) {
                    authentication.unlinkPerson(target);
                    return "[OK] Person link removed for account '" + target + "'.";
                }
            } else {
                String personCode = InputHelper.getString(scanner, "Person code: ");
                String position = optional(scanner, "Position (needed for management roles, Enter to skip): ");
                authentication.linkPerson(target, personCode, position);
                return "[OK] Account linked: " + target + " -> " + personCode;
            }
        } catch (SiteManagementException e) {
            return "[ERROR] " + e.getMessage();
        }
        return null;
    }

    private String deleteAccountFlow(Scanner scanner) {
        try {
            String target = username(scanner);
            Account account = authentication.findAccount(target);
            if (account == null) {
                return "[ERROR] Account '" + target + "' not found.";
            }
            int confirm = InputHelper.getInt(
                    scanner, "Are you sure you want to delete account '" + account.getUsername() + "'? 1. Yes | 0. Cancel: ", 0, 1
            );
            if (confirm == 0) {
                return null;
            }
            authentication.deleteAccount(target);
            return "[OK] Account '" + target + "' has been deleted.";
        } catch (SiteManagementException e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    private String grantPermissionFlow(Scanner scanner) {
        try {
            String target = username(scanner);
            Permission perm = choosePermission(scanner);
            authentication.grantPermissions(target, EnumSet.of(perm));
            System.out.println("Success: Permission granted.");
            return "[OK] Granted " + perm + " to " + target;
        } catch (SiteManagementException e) {
            System.out.println("Error: " + e.getMessage());
            return "[ERROR] " + e.getMessage();
        }
    }

    private String revokePermissionFlow(Scanner scanner) {
        try {
            String target = username(scanner);
            Permission perm = choosePermission(scanner);
            authentication.revokePermissions(target, EnumSet.of(perm));
            System.out.println("Success: Permission revoked.");
            return "[OK] Revoked " + perm + " from " + target;
        } catch (SiteManagementException e) {
            System.out.println("Error: " + e.getMessage());
            return "[ERROR] " + e.getMessage();
        }
    }

    private static String username(Scanner scanner) {
        return InputHelper.getString(scanner, "Username: ");
    }

    private static String optional(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    static Account.Role chooseRole(Scanner scanner) {
        Account.Role[] roles = Account.Role.values();
        String[] choices = new String[roles.length];
        for (int i = 0; i < roles.length; i++) {
            choices[i] = (i + 1) + ". " + roles[i];
        }
        ConsoleUi.menu("SELECT ACCOUNT ROLE", choices);
        return roles[InputHelper.getInt(scanner, "Choose role (1-" + roles.length + "): ", 1, roles.length) - 1];
    }

    private static Permission choosePermission(Scanner scanner) {
        Permission[] permissions = Permission.values();
        String[] choices = new String[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            choices[i] = (i + 1) + ". " + permissions[i];
        }
        ConsoleUi.menu("SELECT PERMISSION", choices);
        return permissions[InputHelper.getInt(
                scanner, "Choose permission (1-" + permissions.length + "): ", 1, permissions.length) - 1];
    }
}
