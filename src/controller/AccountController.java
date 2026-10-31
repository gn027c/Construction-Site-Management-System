package controller;

import exception.AuthenticationException;
import exception.DataValidationException;
import exception.SiteManagementException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.Set;
import model.auth.Account;
import model.auth.Permission;
import service.AuthenticationService;
import util.InputHelper;
import view.ConsoleTheme;
import view.ConsoleScreen;

/**
 * Handles staff sign-in, password changes and administrator account management.
 * Account changes are authorized and saved by the authentication service.
 */
public final class AccountController {
    private final AuthenticationService authentication;

    public AccountController(AuthenticationService authentication) {
        this.authentication = authentication;
    }

    public boolean signIn(Scanner scanner) {
        ConsoleScreen.identityBoundary();
        ConsoleTheme.breadcrumb("SECURITY / STAFF SIGN IN");
        int attempt = 0;
        while (attempt < 3) {
            String username;
            try {
                username = InputHelper.readLine(scanner, "Username (0-Exit): ").trim();
            } catch (exception.InputCancelledException e) {
                return false;
            }
            if (username.isEmpty() || username.equals("0")) {
                return false;
            }
            try {
                String password = InputHelper.getPassword(scanner, "Password: ");
                Account account = authentication.login(username, password);
                ConsoleTheme.success("Signed in as " + account.getUsername() + " (" + account.getRole() + ").");
                return true;
            } catch (AuthenticationException e) {
                attempt++;
                ConsoleTheme.warning(e.getMessage());
            } catch (exception.InputCancelledException e) {
                ConsoleTheme.warning("Sign-in cancelled. Enter another username or 0 to exit.");
            }
        }
        ConsoleTheme.warning("Sign-in attempts exhausted. Session closed.");
        return false;
    }

    public void changeOwnPassword(Scanner scanner) throws IOException {
        authentication.currentAccount();
        String current = InputHelper.getPassword(scanner, "Current password: ");
        String next = confirmedPassword(scanner);
        authentication.changePassword(current, next);
        ConsoleTheme.success("Password changed.");
    }

    public void handleMenu(Scanner scanner) {
        while (authentication.isLoggedIn()) {
            authentication.require(Permission.MANAGE_ACCOUNTS);
            ConsoleTheme.menuWithDescription(
                    "ACCOUNT MANAGEMENT",
                    "Manage user accounts, system permissions and access credentials.",
                    "List accounts", "Create account", "Assign role",
                    "Edit permissions", "Enable or disable account", "Reset password", "Link personnel record"
            );
            int choice = InputHelper.getMenuChoice(scanner, "Choice (0-7): ", 7);
            ConsoleScreen.begin();
            if (choice == 0) {
                return;
            }
            try {
                if (choice == 1) {
                    view.ListView.show(scanner, "ACCOUNTS", authentication.listAccounts(),
                            a -> a.getUsername() + " | " + a.getRole() + " | " + (a.isEnabled() ? "Enabled" : "Disabled")
                                    + " | Person: " + (a.getPersonCode().isEmpty() ? "(None)" : a.getPersonCode())
                                    + " | " + (a.getRole() == Account.Role.ADMIN ? "ALL (17 permissions)" : a.getPermissions()),
                            Account::toString, java.util.Comparator.comparing(Account::getUsername),
                            event -> authentication.audit(event, "LIST", "", "SUCCESS", "Navigated list."));
                    continue;
                }
                if (choice == 2) {
                    String username = InputHelper.getString(scanner, "Account username: ");
                    if (authentication.listAccounts().stream().anyMatch(a -> a.getUsername().equalsIgnoreCase(username))) {
                        ConsoleTheme.error("Username already in use.");
                        continue;
                    }
                    Account.Role role = selectRole(scanner);
                    String code = "";
                    if (role != Account.Role.ADMIN) {
                        code = InputHelper.getString(scanner, "Linked person code: ");
                    }
                    authentication.createAccount(username, confirmedPassword(scanner), role, code);
                    ConsoleTheme.success("Account saved.");
                    continue;
                }
                String username = InputHelper.getString(scanner, "Account username: ");
                if (authentication.listAccounts().stream().noneMatch(a -> a.getUsername().equalsIgnoreCase(username))) {
                    ConsoleTheme.error("Account does not exist.");
                    continue;
                }
                switch (choice) {
                    case 3:
                        authentication.changeRole(username, selectRole(scanner));
                        break;
                    case 4:
                        authentication.changePermissions(username, selectPermissions(scanner));
                        break;
                    case 5:
                        int enabled = InputHelper.getInt(scanner, "1-Enable, 0-Disable: ", 0, 1);
                        authentication.setEnabled(username, enabled == 1);
                        break;
                    case 6:
                        authentication.resetPassword(username, confirmedPassword(scanner));
                        break;
                    case 7:
                        String linkedCode = InputHelper.getString(scanner, "Linked person code: ");
                        authentication.linkPerson(username, linkedCode);
                        break;
                    default:
                        throw new IllegalStateException("Unsupported account operation.");
                }
                ConsoleTheme.success("Account saved.");
                if (!authentication.allows(Permission.MANAGE_ACCOUNTS)) {
                    return;
                }
            } catch (SiteManagementException | IOException e) {
                authentication.audit("ACCOUNT_OPERATION", "ACCOUNT", "", "FAILED", e.getMessage());
                ConsoleTheme.error(e.getMessage());
            }
        }
    }

    private Account.Role selectRole(Scanner scanner) {
        Account.Role[] roles = Account.Role.values();
        for (int i = 0; i < roles.length; i++) {
            System.out.println((i + 1) + ". " + roles[i]);
        }
        int choice = InputHelper.getInt(scanner, "Role: ", 1, roles.length);
        return roles[choice - 1];
    }

    private Set<Permission> selectPermissions(Scanner scanner) {
        Permission[] permissions = Permission.values();
        for (int i = 0; i < permissions.length; i++) {
            System.out.println((i + 1) + ". " + permissions[i]);
        }
        String input = InputHelper.getString(scanner, "Permission numbers, separated by commas (0-None): ");
        Set<Permission> selected = EnumSet.noneOf(Permission.class);
        if (input.equals("0")) {
            return selected;
        }
        try {
            for (String token : input.split(",", -1)) {
                int index = Integer.parseInt(token.trim()) - 1;
                if (index < 0 || index >= permissions.length) {
                    throw new NumberFormatException();
                }
                selected.add(permissions[index]);
            }
        } catch (NumberFormatException e) {
            throw new DataValidationException("permissions", "Choose valid permission numbers.");
        }
        return selected;
    }

    private String confirmedPassword(Scanner scanner) {
        String password = InputHelper.getPassword(scanner, "New password: ");
        String confirmation = InputHelper.getPassword(scanner, "Confirm password: ");
        if (!password.equals(confirmation)) {
            throw new DataValidationException("password", "Confirmation does not match.");
        }
        return password;
    }
}
