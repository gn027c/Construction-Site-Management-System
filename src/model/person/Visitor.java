package model.person;

import static model.common.ModelValidation.*;

import model.common.SafetyLevel;

public class Visitor extends Person {
    private String purpose;
    private String hostPersonName;
    private String entryDate;

    public Visitor(
            String id,
            String code,
            String name,
            String phoneNumber,
            String purpose,
            String hostPersonName,
            String entryDate
    ) {
        super(id, code, name, phoneNumber, Role.VISITOR);
        this.purpose = requiredText(purpose, "purpose");
        this.hostPersonName = requiredText(hostPersonName, "hostPersonName");
        this.entryDate = optionalDate(entryDate, "entryDate");
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = requiredText(purpose, "purpose");
    }

    public String getHostPersonName() {
        return hostPersonName;
    }

    public void setHostPersonName(String hostPersonName) {
        this.hostPersonName = requiredText(hostPersonName, "hostPersonName");
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = optionalDate(entryDate, "entryDate");
    }

    @Override
    public boolean hasValidSafetyCredential() {
        return false;
    }

    @Override
    public String checkInDenialReason(java.time.LocalDate day) {
        if (entryDate.isEmpty()) {
            return "Visit date is not set; record the visit date before check-in.";
        }
        if (!util.DateTimeHelper.parseDate(entryDate).equals(day)) {
            return "Visit is booked for " + entryDate + ", not " + util.DateTimeHelper.formatDate(day) + ".";
        }
        return "";
    }

    @Override
    public void setSafetyLevel(SafetyLevel safetyLevel) {
        if (safetyLevel != SafetyLevel.NONE) {
            throw new exception.DataValidationException("safetyLevel", "Visitors cannot hold a safety level.");
        }
        super.setSafetyLevel(safetyLevel);
    }

    @Override
    public boolean shouldProvisionAccount() {
        return false;
    }

    @Override
    public String toCsvLine() {
        return baseCsv("VISITOR") + "," + util.CsvHelper.toCsvLine(purpose, hostPersonName, entryDate) + csvStatus();
    }

    @Override
    public String toString() {
        return "Visitor{" + super.toString() + "}";
    }
}
