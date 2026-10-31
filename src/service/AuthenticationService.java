package service;

import exception.AccessDeniedException;
import exception.AuthenticationException;
import exception.DataValidationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import model.auth.Account;
import model.auth.Permission;
import repository.AccountRepository;
import util.PasswordHelper;

public final class AuthenticationService {
    private final AccountRepository repository;
    private String currentUsername;
    private PersonnelService personnel;

    public AuthenticationService(Path directory) throws IOException {
        repository = new AccountRepository(directory.resolve("accounts.csv"));
        if (repository.exists()) {
            repository.load();
        } else {
            List<Account> initial = new ArrayList<>();
            initial.add(new Account(
                    "admin", PasswordHelper.validate("admin"), Account.Role.ADMIN,
                    true, Account.Role.ADMIN.defaultPermissions(), "", false
            ));
            repository.replaceAll(initial);
        }
    }

    public Account login(String username, String password) {
        currentUsername = null;
        Account account = null;
        try {
            account = repository.find(Account.normalizeUsername(username));
        } catch (DataValidationException ignored) {
        }
        if (account == null || !account.isEnabled() || !PasswordHelper.matches(password, account.getPassword())) {
            throw new AuthenticationException();
        }
        currentUsername = account.getUsername();
        return account;
    }

    public void logout() {
        if (isLoggedIn()) {
        }
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
        boolean adminOnly = permission == Permission.MANAGE_ACCOUNTS;
        if (!allows(permission) || (adminOnly && currentAccount().getRole() != Account.Role.ADMIN)) {
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
        createAccount(username, password, role, "");
    }

    public void createAccount(String username, String password, Account.Role role, String personCode)
            throws IOException {
        createAccount(username, password, role, personCode, true);
    }

    public void createAccount(
            String username, String password, Account.Role role, String personCode, boolean mustChangePassword
    ) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        String normalized = Account.normalizeUsername(username);
        if (repository.find(normalized) != null) {
            throw new DataValidationException("username", "Already exists.");
        }
        if (role == null) {
            throw new DataValidationException("role", "Must not be null.");
        }
        List<Account> accounts = repository.findAll();
        Account added = new Account(
                normalized, PasswordHelper.validate(password), role, true,
                role.defaultPermissions(), personCode, mustChangePassword
        );
        validatePersonLink(added);
        accounts.add(added);
        repository.replaceAll(accounts);
    }

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
        replace(findRequired(username).withResetPassword(password));
    }

    public Account getAccount(String username) {
        return repository.find(Account.normalizeUsername(username));
    }

    public boolean isUsernameTaken(String username) {
        try {
            return repository.find(Account.normalizeUsername(username)) != null;
        } catch (DataValidationException e) {
            return false;
        }
    }

    public void changePassword(String currentPassword, String nextPassword) throws IOException {
        Account account = currentAccount();
        if (!PasswordHelper.matches(currentPassword, account.getPassword())) {
            throw new AuthenticationException();
        }
        if (currentPassword.equals(nextPassword)) {
            throw new DataValidationException("password", "The new password must differ from the current one.");
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
            if (accounts.get(i).getUsername().equalsIgnoreCase(replacement.getUsername())) {
                if (!accounts.get(i).getPersonCode().equals(replacement.getPersonCode())) {
                    validatePersonLink(replacement);
                }
                accounts.set(i, replacement);
                repository.replaceAll(accounts);
                return;
            }
        }
        throw new DataValidationException("username", "Account does not exist.");
    }

    public void bindPersonnel(PersonnelService service) {
        personnel = service;
    }

    public void validateLinks(PersonnelService service) {
        for (Account account : repository.findAll()) {
            if (!account.getPersonCode().isEmpty()) {
                service.findPersonByCode(account.getPersonCode());
            }
        }
    }

    private void validatePersonLink(Account account) {
        if (!account.getPersonCode().isEmpty()) {
            if (personnel == null) {
                throw new DataValidationException("personCode", "Personnel must be loaded before linking accounts.");
            }
            personnel.findPersonByCode(account.getPersonCode());
        }
    }

    public void linkPerson(String username, String personCode) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        replace(findRequired(username).withPersonCode(personCode));
    }

    public String currentPersonCode() {
        String code = currentAccount().getPersonCode();
        if (code.isEmpty()) {
            throw new DataValidationException("personCode", "Ask an administrator to link your personnel record.");
        }
        return code;
    }

    public boolean isPersonLinked(String code) {
        String normalized = util.IdentifierHelper.normalize(code);
        for (Account account : repository.findAll()) {
            if (normalized.equals(account.getPersonCode())) {
                return true;
            }
        }
        return false;
    }
}
