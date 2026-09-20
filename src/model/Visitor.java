package model;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Khách tham quan hoặc đoàn thanh tra vãng lai.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Quản lý thông tin mục đích thăm (purpose), người bảo lãnh tiếp đoàn (hostPersonName), và ngày đến thăm (entryDate).
 * - Cài đặt các hành vi displayDetails() và toCsvLine() theo yêu cầu Workshop 1.
 */
public class Visitor extends Person {
    private String purpose;
    private String hostPersonName;
    private String entryDate;

    public Visitor() {
        super();
        this.role = Role.VISITOR;
    }

    public Visitor(String id, String code, String name, String phoneNumber,
                   String purpose, String hostPersonName, String entryDate) {
        super(id, code, name, phoneNumber, Role.VISITOR);
        this.purpose = purpose;
        this.hostPersonName = hostPersonName;
        this.entryDate = entryDate;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getHostPersonName() {
        return hostPersonName;
    }

    public void setHostPersonName(String hostPersonName) {
        this.hostPersonName = hostPersonName;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    @Override
    public void displayDetails() {
        System.out.println("===== THÔNG TIN KHÁCH THAM QUAN (VISITOR) =====");
        System.out.println("ID: " + getId());
        System.out.println("Mã khách: " + getCode());
        System.out.println("Họ tên: " + getName());
        System.out.println("Số điện thoại: " + getPhoneNumber());
        System.out.println("Vai trò: " + getRole());
        System.out.println("Mục đích thăm: " + purpose);
        System.out.println("Người đón tiếp / Bảo lãnh: " + hostPersonName);
        System.out.println("Ngày đến thăm: " + entryDate);
        System.out.println("===============================================");
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                getId(), getCode(), getName(), getPhoneNumber(), getRole(),
                purpose, hostPersonName, entryDate);
    }

    @Override
    public String toString() {
        return String.format("Visitor{%s, purpose='%s', hostPersonName='%s', entryDate='%s'}",
                super.toString(), purpose, hostPersonName, entryDate);
    }
}
