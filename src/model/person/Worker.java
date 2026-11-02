package model.person;

import static model.common.ModelValidation.*;

public class Worker extends Person {
    private String trade;
    private SafetyCertificate safetyCertificate;
    private String assignedTeam;

    public Worker(
            String id,
            String code,
            String name,
            String phoneNumber,
            String trade,
            String safetyCertId,
            String assignedTeam
    ) {
        super(id, code, name, phoneNumber, Role.WORKER);
        this.trade = requiredText(trade, "trade");
        this.safetyCertificate = new SafetyCertificate(optionalText(safetyCertId));
        this.assignedTeam = requiredText(assignedTeam, "assignedTeam");
    }

    public Worker(
            String id,
            String code,
            String name,
            String phoneNumber,
            String trade,
            SafetyCertificate safetyCertificate,
            String assignedTeam
    ) {
        super(id, code, name, phoneNumber, Role.WORKER);
        this.trade = requiredText(trade, "trade");
        this.safetyCertificate = (safetyCertificate != null) ? safetyCertificate : SafetyCertificate.EMPTY;
        this.assignedTeam = requiredText(assignedTeam, "assignedTeam");
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = requiredText(trade, "trade");
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

    public String getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = requiredText(assignedTeam, "assignedTeam");
    }

    @Override
    public boolean hasValidSafetyCredential() {
        return safetyCertificate != null && safetyCertificate.isValid();
    }

    @Override
    public String[] detailLabels() {
        return new String[] { "Trade", "Assigned team", "Safety certificate" };
    }

    @Override
    public String[] detailValues() {
        return new String[] { trade, assignedTeam, getSafetyCertId() };
    }

    @Override
    public String validateDetail(int index, String value) {
        switch (index) {
            case 0:
                return requiredText(value, "trade");
            case 1:
                return requiredText(value, "assignedTeam");
            case 2:
                return optionalText(value);
            default:
                throw unknownDetail(index);
        }
    }

    @Override
    protected void applyDetails(String[] validDetails) {
        trade = validDetails[0];
        assignedTeam = validDetails[1];
        safetyCertificate = new SafetyCertificate(validDetails[2]);
    }

    @Override
    public boolean hasValidSafetyCredential(String[] details) {
        return new SafetyCertificate(details[2]).isValid();
    }

    @Override
    public boolean shouldProvisionAccount() {
        return true;
    }

    @Override
    public String toCsvLine() {
        return baseCsv("WORKER") + "," + util.CsvHelper.toCsvLine(getSafetyCertId(), trade, assignedTeam) + csvStatus();
    }

    @Override
    public String toString() {
        return String.format("Worker{%s, safetyCertId='%s'}", super.toString(), getSafetyCertId());
    }
}
