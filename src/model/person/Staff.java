package model.person;

import static model.common.ModelValidation.*;

/**
 * Represents an internal staff member independently of account permissions.
 * An optional safety certificate is encapsulated in a {@link SafetyCertificate} Value Object.
 */
public class Staff extends Person {
    private String position;
    private SafetyCertificate safetyCertificate;

    public Staff(String id, String code, String name, String phoneNumber, String position, String safetyCertId) {
        super(id, code, name, phoneNumber, Role.STAFF);
        this.position = requiredText(position, "position");
        this.safetyCertificate = new SafetyCertificate(optionalText(safetyCertId));
    }

    public Staff(String id, String code, String name, String phoneNumber, String position, SafetyCertificate safetyCertificate) {
        super(id, code, name, phoneNumber, Role.STAFF);
        this.position = requiredText(position, "position");
        this.safetyCertificate = (safetyCertificate != null) ? safetyCertificate : SafetyCertificate.EMPTY;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = requiredText(position, "position");
    }

    public String getSafetyCertId() {
        return safetyCertificate != null ? safetyCertificate.getCertNumber() : "";
    }

    public void setSafetyCertId(String safetyCertId) {
        this.safetyCertificate = new SafetyCertificate(optionalText(safetyCertId));
    }

    public SafetyCertificate getSafetyCertificate() {
        return safetyCertificate;
    }

    public void setSafetyCertificate(SafetyCertificate safetyCertificate) {
        this.safetyCertificate = (safetyCertificate != null) ? safetyCertificate : SafetyCertificate.EMPTY;
    }

    @Override
    public boolean hasValidSafetyCredential() {
        return safetyCertificate != null && safetyCertificate.isValid();
    }

    @Override
    public boolean shouldProvisionAccount() {
        return true;
    }

    @Override
    public String toCsvLine() {
        return baseCsv("STAFF") + "," + util.CsvHelper.toCsvLine(getSafetyCertId(), position, "") + csvStatus();
    }

    @Override
    public String toString() {
        return "Staff{" + super.toString() + ", position='" + position + "'}";
    }
}
