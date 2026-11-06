package model;

import exception.AppError;
import exception.DataValidationException;
import java.util.EnumSet;
import java.util.Set;
import util.ValidationHelper;

public final class Account {
    private static final String SIGN_IN_FAILED = "Invalid username or password.";

    public enum Role {
        ADMIN,
        SITE_MANAGER,
        SAFETY_OFFICER,
        SITE_USER;

        public boolean requiresPosition() {
            return this == SITE_MANAGER || this == SAFETY_OFFICER;
        }

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
                    return EnumSet.of(
                            Permission.RECORD_OWN_ATTENDANCE,
                            Permission.VIEW_OWN_ATTENDANCE
                    );
                default:
                    throw new IllegalStateException("Unsupported account role.");
            }
        }
    }

    private final String username;
    private String password;
    private Role role;
    private boolean enabled;
    private Set<Permission> permissions;
    private String personCode;
    private boolean mustChangePassword;

    public Account(
            String username, String password, Role role, boolean enabled,
            Set<Permission> permissions, String personCode, boolean mustChangePassword
    ) {
        this.username = normalizeUsername(username);
        validatePassword(password);
        this.password = password;
        this.personCode = personCode == null ? "" : personCode.trim();
        if (role == null || permissions == null || permissions.contains(null)) {
            throw new DataValidationException("account", "Role and permissions must not be null.");
        }
        validateRole(role, permissions);
        this.role = role;
        this.enabled = enabled;
        this.permissions = copyOf(permissions);
        this.mustChangePassword = mustChangePassword;
    }

    public static String normalizeUsername(String username) {
        return ValidationHelper.requireUsername(username);
    }

    public static AppError signInFailure() {
        return new AppError(SIGN_IN_FAILED);
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public String getPersonCode() {
        return personCode;
    }

    public boolean isLinked() {
        return !personCode.isEmpty();
    }

    public boolean mustChangePassword() {
        return mustChangePassword;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<Permission> getPermissions() {
        return copyOf(permissions);
    }

    public boolean allows(Permission permission) {
        return enabled && permissions.contains(permission);
    }

    public void signIn(String enteredPassword, boolean linkedPersonActive) {
        if (!enabled || !password.equals(enteredPassword) || (isLinked() && !linkedPersonActive)) {
            throw new AppError(SIGN_IN_FAILED);
        }
    }

    public void changePassword(String currentPassword, String newPassword) {
        if (!password.equals(currentPassword)) {
            throw new AppError("Current password is incorrect.");
        }
        validatePassword(newPassword);
        if (password.equals(newPassword)) {
            throw new AppError("New password must be different from current password.");
        }
        password = newPassword;
        mustChangePassword = false;
    }

    public void requirePasswordChange() {
        mustChangePassword = true;
    }

    public void resetPassword(String newPassword) {
        validatePassword(newPassword);
        password = newPassword;
        mustChangePassword = true;
    }

    public boolean canChangeRoleBy(Account actor) {
        return actor != null && !isSameAccount(actor);
    }

    public boolean canBeDisabledBy(Account actor) {
        return actor != null && !isSameAccount(actor);
    }

    public void changeRole(Account actor, Role nextRole) {
        ValidationHelper.requireNotNull(nextRole, "role", "Must not be null.");
        if (nextRole == role) {
            return;
        }
        if (!canChangeRoleBy(actor)) {
            throw new AppError("An account cannot change its own role.");
        }
        Set<Permission> defaults = nextRole.defaultPermissions();
        validateRole(nextRole, defaults);
        role = nextRole;
        permissions = defaults;
    }

    public void grantPermissions(Set<Permission> added) {
        Set<Permission> next = copyOf(permissions);
        next.addAll(added);
        validateRole(role, next);
        permissions = next;
    }

    public void revokePermissions(Set<Permission> removed) {
        Set<Permission> next = copyOf(permissions);
        next.removeAll(removed);
        validateRole(role, next);
        permissions = next;
    }

    public void enable() {
        enabled = true;
    }

    public void disable(Account actor) {
        if (!canBeDisabledBy(actor)) {
            throw new AppError("An account cannot disable itself.");
        }
        enabled = false;
    }

    public void linkTo(Person person) {
        ValidationHelper.requireNotNull(person, "person", "Must not be null.");
        personCode = person.getCode();
    }

    public void unlink() {
        if (role == Role.SITE_USER) {
            throw new DataValidationException("personCode", "A site user must be linked to a person.");
        }
        personCode = "";
    }

    private static void validatePassword(String password) {
        ValidationHelper.requirePassword(password);
    }

    private static Set<Permission> copyOf(Set<Permission> source) {
        Set<Permission> copy = EnumSet.noneOf(Permission.class);
        copy.addAll(source);
        return copy;
    }

    private void validateRole(Role nextRole, Set<Permission> nextPermissions) {
        if (nextRole == Role.ADMIN && !nextPermissions.containsAll(Role.ADMIN.defaultPermissions())) {
            throw new DataValidationException("permissions", "Administrators must retain all permissions.");
        }
        if (nextRole != Role.ADMIN && nextPermissions.contains(Permission.MANAGE_ACCOUNTS)) {
            throw new DataValidationException("permissions", "Account administration requires the ADMIN role.");
        }
        if (nextRole == Role.SITE_USER && personCode.isEmpty()) {
            throw new DataValidationException("personCode", "A site user must be linked to a person.");
        }
        if (nextRole == Role.SITE_USER && !Role.SITE_USER.defaultPermissions().containsAll(nextPermissions)) {
            throw new DataValidationException("permissions", "Site users may only receive personal permissions.");
        }
    }

    private boolean isSameAccount(Account other) {
        return username.equalsIgnoreCase(other.username);
    }

    public String toCsvLine() {
        StringBuilder names = new StringBuilder();
        for (Permission permission : permissions) {
            if (names.length() > 0) {
                names.append(';');
            }
            names.append(permission.name());
        }
        return util.CsvHelper.toCsvLine(
                username, password, role.name(), Boolean.toString(enabled), names.toString(), personCode,
                Boolean.toString(mustChangePassword)
        );
    }

    @Override
    public String toString() {
        return username + " | " + role + " | " + (enabled ? "Enabled" : "Disabled")
                + " | Person: " + (isLinked() ? personCode : "-") + " | " + permissions;
    }
}
