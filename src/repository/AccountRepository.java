package repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.auth.Account;
import model.auth.Permission;
import util.CsvHelper;

/**
 * Stores login accounts in a dedicated CSV, outside business-data exports.
 * Invalid input never replaces active accounts or silently recreates default credentials.
 */
public final class AccountRepository {
    private static final String HEADER = "username,password,role,enabled,permissions,personCode";
    private static final String LEGACY_HEADER = "username,passwordHash,role,enabled,permissions,personCode";
    private final Path file;
    private Map<String, Account> accounts = new LinkedHashMap<>();

    public AccountRepository(Path file) {
        this.file = file;
    }

    public boolean exists() {
        return Files.exists(file, java.nio.file.LinkOption.NOFOLLOW_LINKS);
    }

    public void load() throws IOException {
        try {
            List<String[]> rows = CsvHelper.readRows(file.toString());
            if (rows.isEmpty()
                    || (!Arrays.equals(rows.get(0), HEADER.split(","))
                    && !Arrays.equals(rows.get(0), LEGACY_HEADER.split(",")))) {
                throw new IllegalArgumentException("Invalid account header.");
            }
            List<Account> staged = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] fields = rows.get(i);
                if (CsvHelper.isBlankRow(fields)) {
                    continue;
                }
                if ((fields.length != 6 && fields.length != 7) || !(fields[3].equals("true") || fields[3].equals("false"))) {
                    throw new IllegalArgumentException("Invalid account row " + (i + 1) + ".");
                }
                Set<Permission> permissions = EnumSet.noneOf(Permission.class);
                if (!fields[4].isEmpty()) {
                    for (String name : fields[4].split(";", -1)) {
                        if (!permissions.add(Permission.valueOf(name))) {
                            throw new IllegalArgumentException("Duplicate permission.");
                        }
                    }
                }
                boolean mustChange = fields.length == 7 ? Boolean.parseBoolean(fields[6]) : false;
                staged.add(new Account(
                        fields[0], fields[1], Account.Role.valueOf(fields[2]),
                        Boolean.parseBoolean(fields[3]), permissions, fields[5], mustChange
                ));
            }
            accounts = validate(staged);
        } catch (RuntimeException e) {
            throw new IOException("Invalid accounts.csv; existing credentials were not reset.", e);
        }
    }

    /**
     * Persists a validated replacement before publishing it to callers.
     * At least one enabled administrator must remain available.
     */
    public void replaceAll(List<Account> replacement) throws IOException {
        Map<String, Account> staged;
        try {
            staged = validate(replacement);
        } catch (RuntimeException e) {
            throw new IOException(e.getMessage(), e);
        }
        List<String> rows = new ArrayList<>();
        rows.add(HEADER);
        for (Account account : staged.values()) {
            rows.add(account.toCsvLine());
        }
        if (!CsvHelper.writeAllLines(file.toString(), rows)) {
            throw new IOException("Could not save accounts.csv; account changes were not applied.");
        }
        accounts = staged;
    }

    private static Map<String, Account> validate(List<Account> records) {
        Map<String, Account> staged = new LinkedHashMap<>();
        boolean hasAdmin = false;
        Set<String> linked = new java.util.HashSet<>();
        Set<String> lowerUsernames = new java.util.HashSet<>();
        for (Account account : records) {
            if (account == null
                    || !lowerUsernames.add(account.getUsername().toLowerCase(java.util.Locale.ROOT))
                    || staged.putIfAbsent(account.getUsername(), account) != null) {
                throw new IllegalArgumentException("Duplicate or missing account.");
            }
            hasAdmin |= account.isEnabled() && account.getRole() == Account.Role.ADMIN;
            if (!account.getPersonCode().isEmpty() && !linked.add(account.getPersonCode())) {
                throw new IllegalArgumentException("A person may only be linked to one account.");
            }
        }
        if (!hasAdmin) {
            throw new IllegalArgumentException("At least one enabled administrator is required.");
        }
        return staged;
    }

    public Account find(String username) {
        if (username == null) {
            return null;
        }
        Account direct = accounts.get(username);
        if (direct != null) {
            return direct;
        }
        for (Account a : accounts.values()) {
            if (a.getUsername().equalsIgnoreCase(username)) {
                return a;
            }
        }
        return null;
    }

    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }
}
