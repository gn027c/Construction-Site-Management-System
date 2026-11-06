package repository;

import exception.DataValidationException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import model.Account;
import model.Permission;

public class AccountRepository {
    private static final String HEADER = "username,password,role,enabled,permissions,personCode,mustChangePassword";
    private static final int COLUMN_COUNT = 7;

    private final List<Account> accounts = new ArrayList<>();

    public void save(Account account) {
        accounts.add(account);
    }

    public void delete(Account account) {
        accounts.remove(account);
    }

    public void clear() {
        accounts.clear();
    }

    public Account findByUsername(String username) {
        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username == null ? "" : username.trim())) {
                return account;
            }
        }
        return null;
    }

    public Account findByUsernameOrThrow(String username) throws exception.NotFoundException {
        Account account = findByUsername(username);
        if (account == null) {
            throw new exception.NotFoundException("Account", "username", username);
        }
        return account;
    }

    public Account findByPersonCode(String personCode) {
        for (Account account : accounts) {
            if (account.isLinked() && account.getPersonCode().equalsIgnoreCase(personCode)) {
                return account;
            }
        }
        return null;
    }

    public List<Account> findAll() {
        return new ArrayList<>(accounts);
    }

    public boolean loadFromCsv(String filePath) {
        List<Account> rows = util.CsvHelper.readTable(
                filePath, HEADER, COLUMN_COUNT, this::parse, Account::getUsername);
        if (rows == null) {
            return false;
        }
        boolean hasAdministrator = false;
        for (Account account : rows) {
            hasAdministrator |= account.getRole() == Account.Role.ADMIN && account.isEnabled();
        }
        if (!hasAdministrator) {
            throw new DataValidationException("accounts.csv: at least one enabled ADMIN account is required.");
        }
        accounts.clear();
        accounts.addAll(rows);
        return true;
    }

    private Account parse(List<String> columns) {
        Set<Permission> permissions = EnumSet.noneOf(Permission.class);
        for (String name : columns.get(4).split(";")) {
            if (!name.trim().isEmpty()) {
                permissions.add(Permission.valueOf(name.trim()));
            }
        }
        return new Account(
                columns.get(0), columns.get(1), Account.Role.valueOf(columns.get(2).trim()),
                parseBoolean(columns.get(3)), permissions, columns.get(5), parseBoolean(columns.get(6)));
    }

    private static boolean parseBoolean(String value) {
        String clean = value.trim();
        if (!clean.equals("true") && !clean.equals("false")) {
            throw new DataValidationException("Expected true or false, found " + clean + ".");
        }
        return clean.equals("true");
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Account account : accounts) {
            lines.add(account.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }

}
