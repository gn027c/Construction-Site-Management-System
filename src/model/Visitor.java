package model;

import static model.ModelValidation.*;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Khách tham quan hoặc đoàn thanh tra vãng lai.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Quản lý thông tin mục đích thăm (purpose), người bảo lãnh tiếp đoàn (hostPersonName), và ngày đến thăm (entryDate).
 * - Cài đặt các hành vi displayDetails() và toCsvLine().
 */
public class Visitor extends Person {
    private String purpose;
    private String hostPersonName;
    private String entryDate;

    public Visitor(String id, String code, String name, String phoneNumber, String purpose, String hostPersonName, String entryDate) {
        super(id, code, name, phoneNumber, Role.VISITOR);
        this.purpose = requiredText(purpose, "purpose");
        this.hostPersonName = requiredText(hostPersonName, "hostPersonName");
        this.entryDate = optionalDate(entryDate, "entryDate");
    }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = requiredText(purpose, "purpose"); }
    public String getHostPersonName() { return hostPersonName; }
    public void setHostPersonName(String hostPersonName) { this.hostPersonName = requiredText(hostPersonName, "hostPersonName"); }
    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = optionalDate(entryDate, "entryDate"); }

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
        System.out.println("Ngày đến thăm: " + (entryDate.isEmpty() ? "Chưa có thông tin" : entryDate));
        System.out.println("===============================================");
    }

    @Override public boolean hasValidSafetyCredential() { return false; }

    @Override public String toCsvLine() {
        return baseCsv("VISITOR") + "," + util.CsvHelper.toCsvLine(purpose, hostPersonName, entryDate);
    }

    @Override public String toString() {
        return "Visitor{" + super.toString() + "}";
    }
}
