package service;

import exception.AppError;
import exception.NotFoundException;
import java.util.List;
import java.util.Set;
import model.Account;
import model.Permission;
import model.Person;
import repository.AccountRepository;

public class AuthenticationService {
    private final AccountRepository accountRepository;
    private final PersonnelService personnelService;
    private final String filePath;
    private final String personFilePath;
    private Account current;

    public AuthenticationService(
            AccountRepository accountRepository, PersonnelService personnelService, String filePath
    ) {
        this(accountRepository, personnelService, filePath, null);
    }

    public AuthenticationService(
            AccountRepository accountRepository, PersonnelService personnelService,
            String filePath, String personFilePath
    ) {
        this.accountRepository = accountRepository;
        this.personnelService = personnelService;
        this.filePath = filePath;
        this.personFilePath = personFilePath;
    }

    public void load() {
        if (personFilePath != null && new java.io.File(personFilePath).exists()) {
            personnelService.loadFromCsv(personFilePath);
        }
        if (!accountRepository.loadFromCsv(filePath)) {
            accountRepository.clear();
            accountRepository.save(new Account(
                    "admin", "admin", Account.Role.ADMIN, true, Account.Role.ADMIN.defaultPermissions(), "", true));
            persist();
        }
        persistPersonData();
        if (current != null) {
            current = accountRepository.findByUsername(current.getUsername());
        }
    }

    private void persist() {
        if (!accountRepository.saveToCsv(filePath)) {
            throw new AppError("Could not write the accounts file. Check the data directory and try again.");
        }
    }

    public void persistPersonData() {
        if (personFilePath != null && !personnelService.saveToCsv(personFilePath)) {
            throw new AppError("Could not write the personnel file. Check the data directory.");
        }
    }

    public Account login(String username, String password) {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw Account.signInFailure();
        }
        account.signIn(password, isLinkedPersonActive(account));
        current = account;
        return account;
    }

    private boolean isLinkedPersonActive(Account account) {
        if (!account.isLinked()) {
            return true;
        }
        try {
            return personnelService.findPersonByCode(account.getPersonCode()).isActive();
        } catch (NotFoundException e) {
            return false;
        }
    }

    public void logout() {
        current = null;
    }

    public Account currentAccount() {
        return current;
    }

    public boolean allows(Permission permission) {
        return current != null && current.allows(permission);
    }

    public void require(Permission permission) {
        if (!allows(permission)) {
            throw new AppError("Permission denied: " + permission);
        }
    }

    public void requireAny(Permission... permissions) {
        for (Permission permission : permissions) {
            if (allows(permission)) {
                return;
            }
        }
        throw new AppError("Permission denied: " + java.util.Arrays.toString(permissions));
    }

    public String currentPersonCode() {
        if (current == null || !current.isLinked()) {
            throw new AppError("This account is not linked to a person. Ask an administrator to link it.");
        }
        return current.getPersonCode();
    }

    public void changeOwnPassword(String currentPassword, String newPassword) {
        if (current == null) {
            throw new AppError("Sign in first.");
        }
        current.changePassword(currentPassword, newPassword);
        persist();
    }

    public Account findAccount(String username) {
        return accountRepository.findByUsername(username);
    }

    public Account accountOf(String personCode) {
        return accountRepository.findByPersonCode(personCode);
    }

    private Account requireAccount(String username) {
        return accountRepository.findByUsernameOrThrow(username);
    }

    public List<Account> listAccounts() {
        require(Permission.MANAGE_ACCOUNTS);
        return accountRepository.findAll();
    }

    public int listAccountsForBootstrapCheck() {
        return accountRepository.findAll().size();
    }

    public void setPersonActive(String personCode, boolean active) {
        require(Permission.MANAGE_PERSONNEL);
        Person person = personnelService.findPersonByCode(personCode);
        if (!active && isLastUsableAdministrator(person)) {
            throw new AppError("The last usable administrator cannot be deactivated.");
        }
        personnelService.setActive(personCode, active);
        persistPersonData();
    }

    private boolean isLastUsableAdministrator(Person person) {
        int usableAdministrators = 0;
        for (Account account : accountRepository.findAll()) {
            if (account.getRole() == Account.Role.ADMIN && account.isEnabled()
                    && (!account.isLinked() || account.getPersonCode().equalsIgnoreCase(person.getCode())
                    || isLinkedPersonActive(account))) {
                usableAdministrators++;
            }
        }
        Account linked = accountRepository.findByPersonCode(person.getCode());
        return linked != null && linked.getRole() == Account.Role.ADMIN
                && linked.isEnabled() && usableAdministrators <= 1;
    }

    public void createAccount(
            String username, String password, Account.Role role, String personCode, String position
    ) {
        require(Permission.MANAGE_ACCOUNTS);
        String cleanUsername = Account.normalizeUsername(username);
        if (accountRepository.findByUsername(cleanUsername) != null) {
            throw new AppError("Username already exists: " + cleanUsername);
        }
        boolean linked = personCode != null && !personCode.trim().isEmpty();
        if (!linked) {
            accountRepository.save(new Account(cleanUsername, password, role, true, role.defaultPermissions(), "", true));
            persist();
            return;
        }
        Person person = personnelService.findPersonByCode(personCode);
        requireUnlinked(person);
        Account.Role startRole = Account.Role.SITE_USER;
        Account account = new Account(
                cleanUsername, password, startRole, true, startRole.defaultPermissions(), person.getCode(), true);
        boolean hasPosition = position != null && !position.trim().isEmpty();
        if (role.requiresPosition() || hasPosition) {
            person.changePosition(position, account, role, current);
        } else {
            account.changeRole(current, role);
        }
        accountRepository.save(account);
        persist();
        persistPersonData();
    }

    private void requireUnlinked(Person person) {
        if (accountRepository.findByPersonCode(person.getCode()) != null) {
            throw new AppError("Person " + person.getCode() + " already has an account.");
        }
    }

    public void changeRole(String username, Account.Role role) {
        require(Permission.MANAGE_ACCOUNTS);
        Account account = requireAccount(username);
        if (account.isLinked() && role != null && role.requiresPosition()
                && personnelService.findPersonByCode(account.getPersonCode()).getPosition().isEmpty()) {
            throw new AppError("Set the person's position together with a management role.");
        }
        account.changeRole(current, role);
        persist();
        persistPersonData();
    }

    public void changePosition(String personCode, String position, Account.Role role) {
        require(Permission.MANAGE_PERSONNEL);
        Person person = personnelService.findPersonByCode(personCode);
        Account account = accountRepository.findByPersonCode(person.getCode());
        if (account != null) {
            require(Permission.MANAGE_ACCOUNTS);
        }
        person.changePosition(position, account, role, current);
        persist();
        persistPersonData();
    }

    public void setEnabled(String username, boolean enabled) {
        require(Permission.MANAGE_ACCOUNTS);
        Account account = requireAccount(username);
        if (enabled) {
            account.enable();
        } else {
            account.disable(current);
        }
        persist();
    }

    public void resetPassword(String username, String password) {
        require(Permission.MANAGE_ACCOUNTS);
        requireAccount(username).resetPassword(password);
        persist();
    }

    public void linkPerson(String username, String personCode, String position) {
        require(Permission.MANAGE_ACCOUNTS);
        Account account = requireAccount(username);
        if (account.isLinked()) {
            throw new AppError("Account " + account.getUsername() + " is already linked to a person.");
        }
        Person person = personnelService.findPersonByCode(personCode);
        requireUnlinked(person);
        if (account.getRole() == Account.Role.ADMIN && !person.isActive() && !hasOtherUsableAdministrator(account)) {
            throw new AppError("The last usable administrator cannot be linked to an inactive person.");
        }
        if (account.getRole().requiresPosition()) {
            person.changePosition(position, account, account.getRole(), current);
        }
        account.linkTo(person);
        persist();
        persistPersonData();
    }

    private boolean hasOtherUsableAdministrator(Account excluded) {
        for (Account account : accountRepository.findAll()) {
            if (account == excluded || account.getRole() != Account.Role.ADMIN || !account.isEnabled()) {
                continue;
            }
            if (!account.isLinked() || isLinkedPersonActive(account)) {
                return true;
            }
        }
        return false;
    }

    public void grantPermissions(String username, Set<Permission> permissions) {
        require(Permission.MANAGE_ACCOUNTS);
        requireAccount(username).grantPermissions(permissions);
        persist();
    }

    public void revokePermissions(String username, Set<Permission> permissions) {
        require(Permission.MANAGE_ACCOUNTS);
        requireAccount(username).revokePermissions(permissions);
        persist();
    }

    public void removeLinkedAccount(Account account) {
        require(Permission.MANAGE_ACCOUNTS);
        if (current != null && current.getUsername().equalsIgnoreCase(account.getUsername())) {
            throw new AppError("The signed-in account cannot be deleted.");
        }
        accountRepository.delete(account);
        persist();
    }

    public void unlinkPerson(String username) {
        require(Permission.MANAGE_ACCOUNTS);
        Account account = requireAccount(username);
        if (!account.isLinked()) {
            throw new AppError("Account " + account.getUsername() + " is not linked to any person.");
        }
        if (account.getRole() == Account.Role.SITE_USER) {
            throw new AppError("A site user account must remain linked to a person.");
        }
        try {
            Person oldPerson = personnelService.findPersonByCode(account.getPersonCode());
            if (oldPerson != null && account.getRole().requiresPosition()) {
                oldPerson.changePosition("", account, Account.Role.SITE_USER, current);
            }
        } catch (NotFoundException ignored) {
        }
        account.unlink();
        persist();
        persistPersonData();
    }

    public void relinkPerson(String username, String newPersonCode, String position) {
        require(Permission.MANAGE_ACCOUNTS);
        Account account = requireAccount(username);
        Person newPerson = personnelService.findPersonByCode(newPersonCode);
        requireUnlinked(newPerson);
        if (account.getRole() == Account.Role.ADMIN && !newPerson.isActive() && !hasOtherUsableAdministrator(account)) {
            throw new AppError("The last usable administrator cannot be linked to an inactive person.");
        }
        if (account.isLinked()) {
            try {
                Person oldPerson = personnelService.findPersonByCode(account.getPersonCode());
                if (oldPerson != null && account.getRole().requiresPosition()) {
                    oldPerson.changePosition("", account, Account.Role.SITE_USER, current);
                }
            } catch (NotFoundException ignored) {
            }
        }
        if (account.getRole().requiresPosition()) {
            newPerson.changePosition(position, account, account.getRole(), current);
        }
        account.linkTo(newPerson);
        persist();
        persistPersonData();
    }

    public void deleteAccount(String username) {
        require(Permission.MANAGE_ACCOUNTS);
        Account account = requireAccount(username);
        if (current != null && current.getUsername().equalsIgnoreCase(account.getUsername())) {
            throw new AppError("The signed-in account cannot be deleted.");
        }
        if (account.getRole() == Account.Role.ADMIN && !hasOtherUsableAdministrator(account)) {
            throw new AppError("Cannot delete the last usable administrator account.");
        }
        if (account.isLinked()) {
            try {
                Person person = personnelService.findPersonByCode(account.getPersonCode());
                if (person != null && account.getRole().requiresPosition()) {
                    person.changePosition("", account, Account.Role.SITE_USER, current);
                }
            } catch (NotFoundException ignored) {
            }
        }
        accountRepository.delete(account);
        persist();
        persistPersonData();
    }
}
