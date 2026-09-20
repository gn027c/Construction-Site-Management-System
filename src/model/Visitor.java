package model;

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

    // Parameterized Constructor - Bắt buộc đầy đủ thông tin
    public Visitor(String id, String code, String name, String phoneNumber,
                   String purpose, String hostPersonName, String entryDate) {
        super(id, code, name, phoneNumber, Role.VISITOR);
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new IllegalArgumentException("Mục đích thăm (purpose) không được để trống.");
        }
        if (hostPersonName == null || hostPersonName.trim().isEmpty()) {
            throw new IllegalArgumentException("Người đón tiếp / bảo lãnh không được để trống.");
        }
        if (entryDate == null || entryDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày đến thăm không được để trống.");
        }
        this.purpose = purpose.trim();
        this.hostPersonName = hostPersonName.trim();
        this.entryDate = entryDate.trim();
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new IllegalArgumentException("Mục đích thăm (purpose) không được để trống.");
        }
        this.purpose = purpose.trim();
    }

    public String getHostPersonName() {
        return hostPersonName;
    }

    public void setHostPersonName(String hostPersonName) {
        if (hostPersonName == null || hostPersonName.trim().isEmpty()) {
            throw new IllegalArgumentException("Người đón tiếp / bảo lãnh không được để trống.");
        }
        this.hostPersonName = hostPersonName.trim();
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        if (entryDate == null || entryDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Ngày đến thăm không được để trống.");
        }
        this.entryDate = entryDate.trim();
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
