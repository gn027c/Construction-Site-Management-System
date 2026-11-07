package model;

public class Worker extends Person {
    private String trade;
    private String safetyCertId;
    private String assignedTeam;

    public Worker(String id, String code, String name, String phoneNumber,
                  String trade, String safetyCertId, String assignedTeam) {
        super(id, code, name, phoneNumber);
        this.trade = required(trade, "trade");
        this.safetyCertId = safetyCertId == null ? "" : safetyCertId.trim();
        this.assignedTeam = required(assignedTeam, "assignedTeam");
    }

    public String getTrade() {
        return trade;
    }

    public String getSafetyCertId() {
        return safetyCertId;
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    @Override
    public boolean hasValidSafetyCredential() {
        return safetyCertId != null
                && !safetyCertId.trim().isEmpty()
                && safetyCertId.endsWith("_SAFE");
    }

    @Override
    public String[] profileLabels() {
        return new String[] {"Trade", "Safety Certificate (- to clear)", "Team"};
    }

    @Override
    public String[] profileValues() {
        return new String[] {trade, safetyCertId, assignedTeam};
    }

    @Override
    protected void applyProfile(String[] values) {
        String nextTrade = required(values[0], "trade");
        String nextTeam = required(values[2], "assignedTeam");
        trade = nextTrade;
        safetyCertId = values[1] == null ? "" : values[1].trim();
        assignedTeam = nextTeam;
    }

    @Override
    public void displayDetails() {
        System.out.println("===== THÔNG TIN CÔNG NHÂN (WORKER) =====");
        System.out.println("ID: " + getId());
        System.out.println("Mã nhân viên: " + getCode());
        System.out.println("Họ tên: " + getName());
        System.out.println("Số điện thoại: " + getPhoneNumber());
        System.out.println("Chức vụ: " + (getPosition().isEmpty() ? "-" : getPosition()));
        System.out.println("Ngành nghề / Tay nghề: " + trade);
        System.out.println("Mã chứng chỉ an toàn: " + safetyCertId);
        System.out.println("Tổ đội thi công: " + assignedTeam);
        System.out.println("========================================");
    }

    @Override
    public String toCsvLine() {
        return csvLine("WORKER", trade, safetyCertId, assignedTeam);
    }

    @Override
    public String toString() {
        return String.format("Worker{%s, safetyCertId='%s'}",
                super.toString(), safetyCertId);
    }
}
