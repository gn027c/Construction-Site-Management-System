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
    private final ActivityLogService activityLog;
    private final NotificationService notifications;
    private PersonnelService personnel;

    public AuthenticationService(Path directory) throws IOException {
        activityLog = new ActivityLogService(directory);
        notifications = new NotificationService(directory);
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
        currentUsername = null;
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
            recordAudit("ANONYMOUS", "", "LOGIN", "ACCOUNT", "", "FAILED", "Invalid credentials.");
            throw new AuthenticationException();
        }
        currentUsername = account.getUsername();
        audit("LOGIN", "ACCOUNT", currentUsername, "SUCCESS", "Signed in.");
        return account;
    }

    public void logout() {
        if (isLoggedIn()) {
            audit("LOGOUT", "ACCOUNT", currentUsername, "SUCCESS", "Signed out.");
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
        boolean adminOnly = permission == Permission.MANAGE_ACCOUNTS
                || permission == Permission.VIEW_ACTIVITY_LOG || permission == Permission.REVIEW_ATTENDANCE;
        if (!allows(permission) || (adminOnly && currentAccount().getRole() != Account.Role.ADMIN)) {
            audit("ACCESS", "PERMISSION", permission.name(), "FAILED", "Permission denied.");
            throw new AccessDeniedException(permission.name());
        }
    }

    public void requireAny(Permission... permissions) {
        for (Permission permission : permissions) {
            if (allows(permission)) {
                return;
            }
        }
        audit("ACCESS", "MENU", "", "FAILED", "No permission for this menu.");
        throw new AccessDeniedException("no permission for this menu");
    }

    public List<Account> listAccounts() {
        require(Permission.MANAGE_ACCOUNTS);
        audit("LIST_ACCOUNTS", "ACCOUNT", "", "SUCCESS", "Listed accounts without credentials.");
        return repository.findAll();
    }

    public void createAccount(String username, String password, Account.Role role) throws IOException {
        createAccount(username, password, role, "");
    }

    public void createAccount(String username, String password, Account.Role role, String personCode)
            throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        accountChange("CREATE_ACCOUNT", username, "Created account with role " + role + ".", () -> {
            String normalized = Account.normalizeUsername(username);
            if (repository.find(normalized) != null) {
                throw new DataValidationException("username", "Already exists.");
            }
            if (role == null) {
                throw new DataValidationException("role", "Must not be null.");
            }
            List<Account> accounts = repository.findAll();
            Account added = new Account(
                    normalized, PasswordHelper.hash(password), role, true, role.defaultPermissions(), personCode
            );
            validatePersonLink(added);
            accounts.add(added);
            repository.replaceAll(accounts);
        });
    }

    /**
     * Assigns a role and resets custom permissions to that role's defaults.
     */
    public void changeRole(String username, Account.Role role) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        accountChange("CHANGE_ROLE", username, "Assigned role " + role + ".", () -> {
            replace(findRequired(username).withRole(role));
        });
    }

    public void changePermissions(String username, Set<Permission> permissions) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        accountChange("CHANGE_PERMISSIONS", username, "Updated account permissions.", () -> {
            replace(findRequired(username).withPermissions(permissions));
        });
    }

    public void setEnabled(String username, boolean enabled) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        accountChange("SET_ACCOUNT_ENABLED", username, enabled ? "Enabled account." : "Disabled account.", () -> {
            Account account = findRequired(username);
            if (!enabled && account.getUsername().equals(currentUsername)) {
                throw new DataValidationException("account", "Cannot disable your own signed-in account.");
            }
            replace(account.withEnabled(enabled));
        });
    }

    public void resetPassword(String username, String password) throws IOException {
        require(Permission.MANAGE_ACCOUNTS);
        accountChange("RESET_PASSWORD", username, "Reset account password; credentials excluded.", () -> {
            replace(findRequired(username).withPassword(password));
        });
    }

    public void changePassword(String currentPassword, String nextPassword) throws IOException {
        accountChange("CHANGE_PASSWORD", currentUsername, "Changed own password; credentials excluded.", () -> {
            Account account = currentAccount();
            if (!PasswordHelper.verify(currentPassword, account.getPasswordHash())) {
                throw new AuthenticationException();
            }
            replace(account.withPassword(nextPassword));
        });
    }

    private Account findRequired(String username) {
        Account account = repository.find(Account.normalizeUsername(username));
        if (account == null) {
            throw new DataValidationException("username", "Account does not exist.");
        }
        return account;
    }

    private void replace(Account replacement) throws IOException {
        validatePersonLink(replacement);
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
        accountChange("LINK_PERSON", username, "Updated account personnel link.", () -> {
            replace(findRequired(username).withPersonCode(personCode));
        });
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

    public ActivityLogService getActivityLog() {
        return activityLog;
    }

    public NotificationService getNotifications() {
        return notifications;
    }

    public void audit(String action, String targetType, String targetId, String status, String reason) {
        String actor = isLoggedIn() ? currentUsername : "SYSTEM";
        String role = isLoggedIn() ? currentAccount().getRole().name() : "";
        if (targetType.equals("BUSINESS") || targetType.equals("PERSON")
                || targetType.equals("ATTENDANCE") || targetType.equals("NOTIFICATION")) {
            String[] parts = targetId.split("/");
            List<String> normalized = new ArrayList<>();
            for (String part : parts) {
                String code = util.IdentifierHelper.normalizeForLookup(part.trim());
                if (code != null) {
                    normalized.add(code);
                }
            }
            targetId = String.join(" / ", normalized);
        }
        recordAudit(actor, role, action, targetType, targetId, status, reason);
    }

    private String recordAudit(
            String actor, String role, String action, String targetType, String targetId, String status, String reason
    ) {
        boolean saved = activityLog.record(actor, role, action, targetType, targetId, status, reason);
        String operationId = activityLog.getLastOperationId();
        if (!saved) {
            String warning = "Activity log could not be saved; the business result is unchanged.";
            notifyAdmins(operationId, warning);
        }
        if (status.equals("FAILED")) {
            Account actorAccount = repository.find(actor);
            if (actorAccount != null && actorAccount.getRole() != Account.Role.ADMIN) {
                notifications.notifyUser(actor, operationId, action + ": Operation could not be completed.");
            }
            notifyAdmins(operationId, actor + " / " + action + ": " + reason);
        }
        return operationId;
    }

    private interface AccountChange {
        void run() throws IOException;
    }

    /**
     * Records the pre-change actor and the persisted result without exposing password values.
     * Log or notification failures do not turn a saved account change into a failed operation.
     */
    private void accountChange(String action, String username, String description, AccountChange change)
            throws IOException {
        String actor = isLoggedIn() ? currentUsername : "SYSTEM";
        String actorRole = isLoggedIn() ? currentAccount().getRole().name() : "";
        String target = "";
        try {
            target = Account.normalizeUsername(username);
        } catch (DataValidationException ignored) {
            // Invalid account input is represented by an empty target rather than copied into the log.
        }
        try {
            change.run();
        } catch (IOException | RuntimeException e) {
            recordAudit(actor, actorRole, action, "ACCOUNT", target, "FAILED", e.getMessage());
            throw e;
        }
        String operationId = recordAudit(actor, actorRole, action, "ACCOUNT", target, "SUCCESS", description);
        String message = action + " / " + target + ": " + description;
        if (repository.find(actor) != null) {
            notifications.notifyUser(actor, operationId, message);
        }
        if (!target.equals(actor) && repository.find(target) != null) {
            notifications.notifyUser(target, operationId, message);
        }
    }

    public void notifyAdmins(String operationId, String message) {
        for (Account account : repository.findAll()) {
            if (account.getRole() == Account.Role.ADMIN && account.isEnabled()) {
                notifications.notifyUser(account.getUsername(), operationId, message);
            }
        }
    }
}
