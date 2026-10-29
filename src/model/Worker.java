package model;

import static model.ModelValidation.*;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
 * MỞ RỘNG ĐA HÌNH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp con mẫu đại diện cho đối tượng Công nhân thi công trên công trường.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Quản lý thông tin ngành nghề (trade), chứng chỉ an toàn (safetyCertId), và tổ đội thi công (assignedTeam).
 * - Cài đặt các hành vi displayDetails() và toCsvLine().
 */
public class Worker extends Person {
    private String trade;
    private String safetyCertId;
    private String assignedTeam;

    public Worker(String id, String code, String name, String phoneNumber, String trade, String safetyCertId, String assignedTeam) {
        super(id, code, name, phoneNumber, Role.WORKER);
        this.trade = requiredText(trade, "trade");
        this.safetyCertId = requiredText(safetyCertId, "safetyCertId");
        this.assignedTeam = requiredText(assignedTeam, "assignedTeam");
    }

    public String getTrade() { return trade; }
    public void setTrade(String trade) { this.trade = requiredText(trade, "trade"); }
    public String getSafetyCertId() { return safetyCertId; }
    public void setSafetyCertId(String safetyCertId) { this.safetyCertId = requiredText(safetyCertId, "safetyCertId"); }
    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = requiredText(assignedTeam, "assignedTeam"); }

    @Override
    public void displayDetails() {
        System.out.println("===== THÔNG TIN CÔNG NHÂN (WORKER) =====");
        System.out.println("ID: " + getId());
        System.out.println("Mã nhân viên: " + getCode());
        System.out.println("Họ tên: " + getName());
        System.out.println("Số điện thoại: " + getPhoneNumber());
        System.out.println("Vai trò: " + getRole());
        System.out.println("Ngành nghề / Tay nghề: " + trade);
        System.out.println("Mã chứng chỉ an toàn: " + safetyCertId);
        System.out.println("Tổ đội thi công: " + assignedTeam);
        System.out.println("========================================");
    }

    @Override public boolean hasValidSafetyCredential() { return safetyCertId.endsWith("_SAFE"); }

    @Override public String toCsvLine() {
        return baseCsv("WORKER") + "," + util.CsvHelper.toCsvLine(safetyCertId, trade, assignedTeam);
    }

    @Override public String toString() {
        return String.format("Worker{%s, safetyCertId='%s'}", super.toString(), safetyCertId);
    }
}
