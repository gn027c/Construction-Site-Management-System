package controller;

import exception.AuthenticationException;
import exception.DataValidationException;
import exception.SiteManagementException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Scanner;
import java.util.Set;
import model.Account;
import model.Permission;
import service.AuthenticationService;
import util.InputHelper;
import view.ConsoleTheme;

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
        ConsoleTheme.title("STAFF SIGN IN");
        for (int attempt = 0; attempt < 3; attempt++) {
            String username = InputHelper.getString(scanner, "Username (0-Exit): ");
            if (username.equals("0")) {
                return false;
            }
            String password = InputHelper.getPassword(scanner, "Password: ");
            try {
                Account account = authentication.login(username, password);
                ConsoleTheme.success("Signed in as " + account.getUsername() + " (" + account.getRole() + ").");
                return true;
            } catch (AuthenticationException e) {
                ConsoleTheme.warning(e.getMessage());
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
            ConsoleTheme.menu(
                    "ACCOUNT ADMINISTRATION", "List accounts", "Create account", "Assign role",
                    "Edit permissions", "Enable or disable account", "Reset password"
            );
            int choice = InputHelper.getInt(scanner, "Choice (0-6): ", 0, 6);
            if (choice == 0) {
                return;
            }
            try {
                if (choice == 1) {
                    for (Account account : authentication.listAccounts()) {
                        System.out.println(account);
                    }
                    continue;
                }
                String username = InputHelper.getString(scanner, "Account username: ");
                switch (choice) {
                    case 2:
                        Account.Role role = selectRole(scanner);
                        authentication.createAccount(username, confirmedPassword(scanner), role);
                        break;
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
                    default:
                        throw new IllegalStateException("Unsupported account operation.");
                }
                ConsoleTheme.success("Account saved.");
                if (!authentication.allows(Permission.MANAGE_ACCOUNTS)) {
                    return;
                }
            } catch (SiteManagementException | IOException e) {
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
