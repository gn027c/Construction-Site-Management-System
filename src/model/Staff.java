package model;

import static model.ModelValidation.*;

/**
 * Represents an internal staff member independently of account permissions.
 * An optional safety certificate follows the same suffix rule as worker credentials.
 */
public class Staff extends Person {
    private String position;
    private String safetyCertId;

    public Staff(String id, String code, String name, String phoneNumber, String position, String safetyCertId) {
        super(id, code, name, phoneNumber, Role.STAFF);
        this.position = requiredText(position, "position");
        this.safetyCertId = optionalText(safetyCertId);
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = requiredText(position, "position");
    }

    public String getSafetyCertId() {
        return safetyCertId;
    }

    public void setSafetyCertId(String safetyCertId) {
        this.safetyCertId = optionalText(safetyCertId);
    }

    @Override
    public boolean hasValidSafetyCredential() {
        return safetyCertId.endsWith("_SAFE");
    }

    @Override
    public String toCsvLine() {
        return baseCsv("STAFF") + "," + util.CsvHelper.toCsvLine(safetyCertId, position, "") + csvStatus();
    }

    @Override
    public String toString() {
        return "Staff{" + super.toString() + ", position='" + position + "'}";
    }
}
