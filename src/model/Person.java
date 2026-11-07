package model;

import exception.AppError;
import exception.DataValidationException;
import util.ValidationHelper;
import java.time.LocalDate;

public abstract class Person {
    protected String id;
    protected String code;
    protected String name;
    protected String phoneNumber;
    protected String position = "";
    protected boolean active = true;
    private SafetyLevel safetyLevel = SafetyLevel.NONE;

    public Person(String id, String code, String name, String phoneNumber) {
        String validId = ValidationHelper.requireText(id, "ID cannot be empty.");
        String validCode = ValidationHelper.requireCode(code, "code", "Use letters, digits, hyphens or underscores.");
        String validName = ValidationHelper.requireText(name, "name", "Must not be empty.");
        String validPhone = ValidationHelper.requirePhone(phoneNumber);
        this.id = validId;
        this.code = validCode;
        this.name = validName;
        this.phoneNumber = validPhone;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }

    public SafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public void updateContactDetails(String newName, String newPhone) {
        String validName = ValidationHelper.requireText(newName, "name", "Must not be empty.");
        String validPhone = ValidationHelper.requirePhone(newPhone);
        this.name = validName;
        this.phoneNumber = validPhone;
    }

    public void restorePosition(String savedPosition) {
        position = savedPosition == null ? "" : savedPosition.trim();
    }

    public boolean canHoldPosition() {
        return active;
    }

    public void changePosition(String newPosition, Account account, Account.Role newRole, Account actor) {
        String clean = newPosition == null ? "" : newPosition.trim();
        if (account == null) {
            if (!clean.isEmpty()) {
                throw new AppError("A position needs a linked account. Create the account first.");
            }
            position = "";
            return;
        }
        if (newRole == null) {
            throw new AppError("Choose the account role together with the position.");
        }
        if (newRole.requiresPosition() && clean.isEmpty()) {
            throw new AppError("Role " + newRole + " needs a position.");
        }
        if (!clean.isEmpty() && !canHoldPosition()) {
            throw new AppError("Person " + code + " cannot hold a position.");
        }
        account.changeRole(actor, newRole);
        position = clean;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void setSafetyLevel(SafetyLevel safetyLevel) {
        ValidationHelper.requireNotNull(safetyLevel, "Safety level cannot be null.");
        if (safetyLevel != SafetyLevel.NONE && !hasValidSafetyCredential()) {
            throw new AppError("Cannot set safety level without a valid safety credential.");
        }
        this.safetyLevel = safetyLevel;
    }

    public String checkInDenialReason(LocalDate today) {
        if (!active) {
            return "Person is inactive and cannot check in";
        }
        return null;
    }

    public void verifyCheckIn(LocalDate today) {
        String denial = checkInDenialReason(today);
        if (denial != null) {
            throw new AppError(denial);
        }
    }

    public boolean canCheckOut() {
        return true;
    }

    public boolean canHandleIncidents() {
        return active;
    }

    public boolean canHoldZoneAccess() {
        return true;
    }

    public void changeVisitDate(String date) {
        throw new AppError("Only a visitor has a visit date.");
    }

    public String canEnter(Zone zone) {
        if (zone == null) {
            return "Zone cannot be null";
        }
        return zone.accessDenialReason(this);
    }

    public abstract boolean hasValidSafetyCredential();

    public abstract String[] profileLabels();

    public abstract String[] profileValues();

    public final void updateProfile(String... values) {
        if (values == null || values.length != profileLabels().length) {
            throw new exception.DataValidationException("Expected " + profileLabels().length + " profile values.");
        }
        applyProfile(values);
        if (!hasValidSafetyCredential()) {
            safetyLevel = SafetyLevel.NONE;
        }
    }

    protected abstract void applyProfile(String[] values);

    protected static String required(String value, String field) {
        return ValidationHelper.requireText(value, field, "Must not be empty.");
    }

    public abstract String toCsvLine();

    protected String csvLine(String type, String extra1, String extra2, String extra3) {
        return util.CsvHelper.toCsvLine(
                type, id, code, name, phoneNumber, position, Boolean.toString(active), safetyLevel.name(),
                extra1, extra2, extra3);
    }

    public void displayDetails() {
        System.out.println(this);
        System.out.println("   Active: " + active);
        System.out.println("   Safety Level: " + safetyLevel);
        System.out.println("   Valid safety credential: " + hasValidSafetyCredential());
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Code: %s | Name: %s | Phone: %s | Position: %s | Active: %s | Safety: %s",
                id, code, name, phoneNumber, position.isEmpty() ? "-" : position, active, safetyLevel);
    }
}
