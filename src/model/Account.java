package model;

import exception.DataValidationException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import util.CsvHelper;
import util.PasswordHelper;

/**
 * Represents an immutable login account, independently of a personnel record.
 * Administrators manage accounts; staff roles start with their relevant application permissions.
 */
public final class Account {
    public enum Role {
        ADMIN,
        SITE_MANAGER,
        SAFETY_OFFICER,
        SITE_USER;

        public Set<Permission> defaultPermissions() {
            switch (this) {
                case ADMIN:
                    return EnumSet.allOf(Permission.class);
                case SITE_MANAGER:
                    return EnumSet.of(
                            Permission.MANAGE_PERSONNEL,
                            Permission.MANAGE_ZONES,
                            Permission.VIEW_ATTENDANCE,
                            Permission.VIEW_INCIDENTS,
                            Permission.RECORD_OWN_ATTENDANCE,
                            Permission.VIEW_OWN_ATTENDANCE,
                            Permission.VIEW_OCCUPANCY,
                            Permission.EXPORT_REPORTS
                    );
                case SAFETY_OFFICER:
                    return EnumSet.of(
                            Permission.REPORT_INCIDENTS,
                            Permission.VIEW_INCIDENTS,
                            Permission.ASSIGN_INCIDENTS,
                            Permission.RECORD_OWN_ATTENDANCE,
                            Permission.VIEW_OWN_ATTENDANCE,
                            Permission.VIEW_OCCUPANCY,
                            Permission.RESOLVE_INCIDENTS
                    );
                case SITE_USER:
                    return EnumSet.of(Permission.RECORD_OWN_ATTENDANCE, Permission.VIEW_OWN_ATTENDANCE);
                default:
                    throw new IllegalStateException("Unsupported account role.");
            }
        }
    }

    private final String username;
    private final String passwordHash;
    private final Role role;
    private final boolean enabled;
    private final Set<Permission> permissions;
    private final String personCode;

    public Account(String username, String passwordHash, Role role, boolean enabled, Set<Permission> permissions) {
        this(username, passwordHash, role, enabled, permissions, "");
    }

    public Account(
            String username, String passwordHash, Role role, boolean enabled,
            Set<Permission> permissions, String personCode
    ) {
        this.username = normalizeUsername(username);
        if (!PasswordHelper.isValidHash(passwordHash)) {
            throw new DataValidationException("passwordHash", "Must be a supported password hash.");
        }
        if (role == null || permissions == null || permissions.contains(null)) {
            throw new DataValidationException("account", "Role and permissions must not be null.");
        }
        Set<Permission> validated = EnumSet.noneOf(Permission.class);
        validated.addAll(permissions);
        if (role == Role.ADMIN && !validated.equals(role.defaultPermissions())) {
            throw new DataValidationException("permissions", "Administrators must retain all permissions.");
        }
        if (role != Role.ADMIN && (validated.contains(Permission.MANAGE_ACCOUNTS)
                || validated.contains(Permission.VIEW_ACTIVITY_LOG)
                || validated.contains(Permission.REVIEW_ATTENDANCE))) {
            throw new DataValidationException("permissions", "Account administration requires the ADMIN role.");
        }
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
        this.permissions = validated;
        this.personCode = personCode == null || personCode.trim().isEmpty()
                ? "" : util.IdentifierHelper.normalize(personCode);
        if (role == Role.SITE_USER && this.personCode.isEmpty()) {
            throw new DataValidationException("personCode", "A site user must be linked to a person.");
        }
        if (role == Role.SITE_USER && !Role.SITE_USER.defaultPermissions().containsAll(validated)) {
            throw new DataValidationException("permissions", "Site users may only receive personal permissions.");
        }
    }

    public static String normalizeUsername(String username) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9_.-]{3,32}")) {
            throw new DataValidationException("username", "Use 3 to 32 letters, digits, dots, underscores or hyphens.");
        }
        return normalized;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getPersonCode() {
        return personCode;
    }

    public Account withPersonCode(String code) {
        return new Account(username, passwordHash, role, enabled, permissions, code);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<Permission> getPermissions() {
        Set<Permission> copy = EnumSet.noneOf(Permission.class);
        copy.addAll(permissions);
        return copy;
    }

    public boolean allows(Permission permission) {
        return enabled && permissions.contains(permission);
    }

    public Account withRole(Role nextRole) {
        if (nextRole == null) {
            throw new DataValidationException("role", "Must not be null.");
        }
        return new Account(username, passwordHash, nextRole, enabled, nextRole.defaultPermissions(), personCode);
    }

    public Account withPermissions(Set<Permission> nextPermissions) {
        return new Account(username, passwordHash, role, enabled, nextPermissions, personCode);
    }

    public Account withEnabled(boolean nextEnabled) {
        return new Account(username, passwordHash, role, nextEnabled, permissions, personCode);
    }

    public Account withPassword(String password) {
        return new Account(username, PasswordHelper.hash(password), role, enabled, permissions, personCode);
    }

    public String toCsvLine() {
        StringBuilder names = new StringBuilder();
        for (Permission permission : permissions) {
            if (names.length() > 0) {
                names.append(';');
            }
            names.append(permission.name());
        }
        return CsvHelper.toCsvLine(
                username, passwordHash, role.name(), Boolean.toString(enabled), names.toString(), personCode
        );
    }

    @Override
    public String toString() {
        return username + " | " + role + " | " + (enabled ? "Enabled" : "Disabled")
                + " | Person: " + personCode + " | " + permissions;
    }
}
