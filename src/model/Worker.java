package model;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
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

    // Parameterized Constructor - Bắt buộc đầy đủ thông tin
    public Worker(String id, String code, String name, String phoneNumber,
                  String trade, String safetyCertId, String assignedTeam) {
        super(id, code, name, phoneNumber, Role.WORKER);
        if (trade == null || trade.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngành nghề (trade) không được để trống.");
        }
        if (safetyCertId == null || safetyCertId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chứng chỉ an toàn không được để trống.");
        }
        if (assignedTeam == null || assignedTeam.trim().isEmpty()) {
            throw new IllegalArgumentException("Tổ đội thi công không được để trống.");
        }
        this.trade = trade.trim();
        this.safetyCertId = safetyCertId.trim();
        this.assignedTeam = assignedTeam.trim();
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        if (trade == null || trade.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngành nghề (trade) không được để trống.");
        }
        this.trade = trade.trim();
    }

    public String getSafetyCertId() {
        return safetyCertId;
    }

    public void setSafetyCertId(String safetyCertId) {
        if (safetyCertId == null || safetyCertId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chứng chỉ an toàn không được để trống.");
        }
        this.safetyCertId = safetyCertId.trim();
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(String assignedTeam) {
        if (assignedTeam == null || assignedTeam.trim().isEmpty()) {
            throw new IllegalArgumentException("Tổ đội thi công không được để trống.");
        }
        this.assignedTeam = assignedTeam.trim();
    }

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

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                getId(), getCode(), getName(), getPhoneNumber(), getRole(),
                trade, safetyCertId, assignedTeam);
    }

    @Override
    public String toString() {
        return String.format("Worker{%s, trade='%s', safetyCertId='%s', assignedTeam='%s'}",
                super.toString(), trade, safetyCertId, assignedTeam);
    }
}
