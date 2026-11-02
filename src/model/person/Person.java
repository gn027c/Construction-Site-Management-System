package model.person;

import static model.common.ModelValidation.*;

import model.common.SafetyLevel;

public abstract class Person {
    private final String id;
    private final String code;
    private final Role role;
    private String name;
    private String phoneNumber;
    private boolean active = true;
    private SafetyLevel safetyLevel = SafetyLevel.NONE;

    protected Person(String id, String code, String name, String phoneNumber, Role role) {
        this.id = identifier(id, "id");
        this.code = personCode(code, "code");
        this.name = requiredText(name, "name");
        this.phoneNumber = phoneNumber(phoneNumber);
        this.role = requiredValue(role, "role");
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Role getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setName(String name) {
        this.name = requiredText(name, "name");
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber(phoneNumber);
    }

    public void updateContactDetails(String name, String phoneNumber) {
        String validName = requiredText(name, "name");
        String validPhone = phoneNumber(phoneNumber);
        this.name = validName;
        this.phoneNumber = validPhone;
    }

    public void updateProfile(String name, String phoneNumber, String[] details, SafetyLevel safetyLevel) {
        String validName = requiredText(name, "name");
        String validPhone = phoneNumber(phoneNumber);
        SafetyLevel requestedLevel = requiredValue(safetyLevel, "safetyLevel");
        String[] validDetails = validatedDetails(details);
        SafetyLevel validLevel = hasValidSafetyCredential(validDetails) ? requestedLevel : SafetyLevel.NONE;
        applyDetails(validDetails);
        this.name = validName;
        this.phoneNumber = validPhone;
        this.safetyLevel = validLevel;
    }

    private String[] validatedDetails(String[] details) {
        requiredValue(details, "details");
        String[] labels = detailLabels();
        if (details.length != labels.length) {
            throw new exception.DataValidationException(
                    "details", "Expected " + labels.length + " values for " + role + "."
            );
        }
        String[] valid = new String[details.length];
        for (int i = 0; i < details.length; i++) {
            valid[i] = validateDetail(i, details[i]);
        }
        return valid;
    }

    protected static exception.DataValidationException unknownDetail(int index) {
        return new exception.DataValidationException("details", "No detail at position " + index + ".");
    }

    public abstract String[] detailLabels();

    public abstract String[] detailValues();

    public abstract String validateDetail(int index, String value);

    protected abstract void applyDetails(String[] validDetails);

    public abstract boolean hasValidSafetyCredential(String[] details);

    public boolean canHandleIncidents() {
        return active;
    }

    public abstract boolean hasValidSafetyCredential();

    public SafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(SafetyLevel safetyLevel) {
        this.safetyLevel = requiredValue(safetyLevel, "safetyLevel");
    }

    public String checkInDenialReason(java.time.LocalDate day) {
        return "";
    }

    public boolean shouldProvisionAccount() {
        return role != Role.VISITOR;
    }

    public abstract String toCsvLine();

    protected String baseCsv(String type) {
        return util.CsvHelper.toCsvLine(type, id, code, name, phoneNumber, role.name());
    }

    protected String csvStatus() {
        return "," + Boolean.toString(active) + "," + safetyLevel.name();
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Code: %s | Name: %s | Phone: %s | Role: %s", id, code, name, phoneNumber, role);
    }
}
