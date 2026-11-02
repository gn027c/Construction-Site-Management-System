package model.person;

import static model.common.ModelValidation.*;

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
    public String[] detailLabels() {
        return new String[] { "Position", "Safety certificate" };
    }

    @Override
    public String[] detailValues() {
        return new String[] { position, getSafetyCertId() };
    }

    @Override
    public String validateDetail(int index, String value) {
        switch (index) {
            case 0:
                return requiredText(value, "position");
            case 1:
                return optionalText(value);
            default:
                throw unknownDetail(index);
        }
    }

    @Override
    protected void applyDetails(String[] validDetails) {
        position = validDetails[0];
        safetyCertificate = new SafetyCertificate(validDetails[1]);
    }

    @Override
    public boolean hasValidSafetyCredential(String[] details) {
        return new SafetyCertificate(details[1]).isValid();
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
