package model;

import static model.ModelValidation.*;

/**
 * Represents a worker with a trade, safety certificate and assigned team.
 *
 * <p>All worker fields are required. A certificate ending with {@code _SAFE}
 * satisfies the current safety credential rule.
 */
public class Worker extends Person {
    private String trade;
    private String safetyCertId;
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
        this.safetyCertId = requiredText(safetyCertId, "safetyCertId");
        this.assignedTeam = requiredText(assignedTeam, "assignedTeam");
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = requiredText(trade, "trade");
    }

    public String getSafetyCertId() {
        return safetyCertId;
    }

    public void setSafetyCertId(String safetyCertId) {
        this.safetyCertId = requiredText(safetyCertId, "safetyCertId");
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = requiredText(assignedTeam, "assignedTeam");
    }

    /**
     * Accepts certificate identifiers ending with the case-sensitive suffix {@code _SAFE}.
     */
    @Override
    public boolean hasValidSafetyCredential() {
        return safetyCertId.endsWith("_SAFE");
    }

    @Override
    public String toCsvLine() {
        return baseCsv("WORKER") + "," + util.CsvHelper.toCsvLine(safetyCertId, trade, assignedTeam) + csvStatus();
    }

    @Override
    public String toString() {
        return String.format("Worker{%s, safetyCertId='%s'}", super.toString(), safetyCertId);
    }
}
