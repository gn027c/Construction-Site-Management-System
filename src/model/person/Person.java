package model.person;

import static model.common.ModelValidation.*;

import model.common.SafetyLevel;

/**
 * Defines identity, contact details and polymorphic behavior for site personnel.
 *
 * <p>Identity, personnel code and role are fixed after construction. Contact
 * changes are validated by the model. Subclasses define their own safety
 * credentials and CSV representation.
 */
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

    /**
     * Validates both contact values before assigning either field.
     *
     * <p>If either value is invalid, both existing contact values are retained.
     */
    public void updateContactDetails(String name, String phoneNumber) {
        String validName = requiredText(name, "name");
        String validPhone = phoneNumber(phoneNumber);
        this.name = validName;
        this.phoneNumber = validPhone;
    }

    /**
     * Evaluates the subtype-specific credential rule used by restricted zones.
     */
    public abstract boolean hasValidSafetyCredential();

    /**
     * Returns the highest restricted-zone level this person is cleared for.
     * New personnel start at {@code NONE}.
     */
    public SafetyLevel getSafetyLevel() {
        return safetyLevel;
    }

    public void setSafetyLevel(SafetyLevel safetyLevel) {
        this.safetyLevel = requiredValue(safetyLevel, "safetyLevel");
    }

    /**
     * Determines whether an authentication account should be provisioned for this person.
     * By default, returns true for workforce personnel and false for temporary visitors.
     */
    public boolean shouldProvisionAccount() {
        return role != Role.VISITOR;
    }

    /**
     * Serializes common personnel columns, subtype data, active status and safety level.
     */
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
