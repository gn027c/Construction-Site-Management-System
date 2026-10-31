package model.person;

import static model.common.ModelValidation.*;

/**
 * Represents a worker with a trade, safety certificate and assigned team.
 *
 * <p>All worker fields are required. Safety credentials are encapsulated
 * in an immutable {@link SafetyCertificate} Value Object.
 */
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
        String certRaw = requiredText(safetyCertId, "safetyCertId");
        this.safetyCertificate = SafetyCertificate.fromRaw(certRaw);
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
        String certRaw = requiredText(safetyCertId, "safetyCertId");
        this.safetyCertificate = SafetyCertificate.fromRaw(certRaw);
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

    /**
     * Delegates domain safety qualification to the SafetyCertificate Value Object.
     */
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
        return baseCsv("WORKER") + "," + util.CsvHelper.toCsvLine(getSafetyCertId(), trade, assignedTeam) + csvStatus();
    }

    @Override
    public String toString() {
        return String.format("Worker{%s, safetyCertId='%s'}", super.toString(), getSafetyCertId());
    }
}
