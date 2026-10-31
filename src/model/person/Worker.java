package model.person;

import static model.common.ModelValidation.*;

/**
 * Represents a worker with a trade, safety certificate and assigned team.
 *
 * <p>Trade and team are required. The safety certificate is optional: a worker
 * waiting for training has none and cannot enter restricted zones. Credentials are
 * encapsulated in an immutable {@link SafetyCertificate} value object.
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
