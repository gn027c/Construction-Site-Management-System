package service;

import exception.AccessDeniedException;
import exception.AuthenticationException;
import exception.DataValidationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import model.Account;
import model.Permission;
import repository.AccountRepository;
import util.PasswordHelper;

/**
 * Authenticates a console session and administers persisted account permissions.
 * A new installation receives admin/admin once; existing credentials are never overwritten on startup.
 */
public final class AuthenticationService {
    private final AccountRepository repository;
    private String currentUsername;

    public AuthenticationService(Path directory) throws IOException {
        repository = new AccountRepository(directory.resolve("accounts.csv"));
        if (repository.exists()) {
            repository.load();
        } else {
            List<Account> initial = new ArrayList<>();
            initial.add(new Account(
                    "admin", PasswordHelper.hash("admin"), Account.Role.ADMIN,
                    true, Account.Role.ADMIN.defaultPermissions()
            ));
            repository.replaceAll(initial);
        }
    }

    public Account login(String username, String password) {
        logout();
        Account account = null;
        try {
            account = repository.find(Account.normalizeUsername(username));
        } catch (DataValidationException ignored) {
            // Invalid and unknown usernames use the same password check and login error.
        }
        String hash = account == null
                ? repository.findAll().get(0).getPasswordHash()
                : account.getPasswordHash();
        boolean passwordMatches = PasswordHelper.verify(password, hash);
        if (account == null || !account.isEnabled() || !passwordMatches) {
            throw new AuthenticationException();
        }
        currentUsername = account.getUsername();
        return account;
    }

    public void logout() {
        currentUsername = null;
    }

    public boolean isLoggedIn() {
        Account account = currentUsername == null ? null : repository.find(currentUsername);
        return account != null && account.isEnabled();
    }

    public Account currentAccount() {
        if (!isLoggedIn()) {
            throw new AccessDeniedException("sign in before using this operation");
        }
        return repository.find(currentUsername);
    }

    public boolean allows(Permission permission) {
        return isLoggedIn() && currentAccount().allows(permission);
    }

    public void require(Permission permission) {
        if (!allows(permission)) {
            throw new AccessDeniedException(permission.name());
        }
    }

    public void requireAny(Permission... permissions) {
        for (Permission permission : permissions) {
            if (allows(permission)) {
                return;
            }
        }
        throw new AccessDeniedException("no permission for this menu");
    }

    public List<Account> listAccounts() {
        require(Permission.MANAGE_ACCOUNTS);
        return repository.findAll();
    }

    public void createAccount(String username, String password, Account.Role role) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        String normalized = Account.normalizeUsername(username);
        if (repository.find(normalized) != null) {
            throw new DataValidationException("username", "Already exists.");
        }
        if (role == null) {
            throw new DataValidationException("role", "Must not be null.");
        }
        List<Account> accounts = repository.findAll();
        accounts.add(new Account(normalized, PasswordHelper.hash(password), role, true, role.defaultPermissions()));
        repository.replaceAll(accounts);
    }

    /**
     * Assigns a role and resets custom permissions to that role's defaults.
     */
    public void changeRole(String username, Account.Role role) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        replace(findRequired(username).withRole(role));
    }

    public void changePermissions(String username, Set<Permission> permissions) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        replace(findRequired(username).withPermissions(permissions));
    }

    public void setEnabled(String username, boolean enabled) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        Account account = findRequired(username);
        if (!enabled && account.getUsername().equals(currentUsername)) {
            throw new DataValidationException("account", "Cannot disable your own signed-in account.");
        }
        replace(account.withEnabled(enabled));
    }

    public void resetPassword(String username, String password) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        replace(findRequired(username).withPassword(password));
    }

    public void changePassword(String currentPassword, String nextPassword) throws IOException {
        Account account = currentAccount();
        if (!PasswordHelper.verify(currentPassword, account.getPasswordHash())) {
            throw new AuthenticationException();
        }
        replace(account.withPassword(nextPassword));
    }

    private Account findRequired(String username) {
        Account account = repository.find(Account.normalizeUsername(username));
        if (account == null) {
            throw new DataValidationException("username", "Account does not exist.");
        }
        return account;
    }

    private void replace(Account replacement) throws IOException {
        List<Account> accounts = repository.findAll();
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getUsername().equals(replacement.getUsername())) {
                accounts.set(i, replacement);
                repository.replaceAll(accounts);
                return;
            }
        }
        throw new DataValidationException("username", "Account does not exist.");
    }
}
