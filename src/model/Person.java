package model;

import static model.ModelValidation.*;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
 * MỞ RỘNG ĐA HÌNH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp cha trừu tượng (abstract class) cơ sở đại diện cho mọi cá nhân trên công trường.
 * - Chịu trách nhiệm quản lý các thuộc tính chung (id, code, name, phoneNumber, role).
 * - Bảo vệ định danh, xác thực thông tin liên hệ và cung cấp hành vi đa hình cho lớp con.
 */
public abstract class Person {
    private final String id;
    private final String code;
    private final Role role;
    private String name;
    private String phoneNumber;

    protected Person(String id, String code, String name, String phoneNumber, Role role) {
        this.id = requiredText(id, "id");
        this.code = personCode(code, "code");
        this.name = requiredText(name, "name");
        this.phoneNumber = phoneNumber(phoneNumber);
        this.role = requiredValue(role, "role");
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public Role getRole() { return role; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }

    public void setName(String name) { this.name = requiredText(name, "name"); }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber(phoneNumber); }

    /** Validate both values before changing either field. */
    public void updateContactDetails(String name, String phoneNumber) {
        String validName = requiredText(name, "name");
        String validPhone = phoneNumber(phoneNumber);
        this.name = validName;
        this.phoneNumber = validPhone;
    }

    public abstract boolean hasValidSafetyCredential();
    public abstract String toCsvLine();

    protected String baseCsv(String type) {
        return util.CsvHelper.toCsvLine(type, id, code, name, phoneNumber, role.name());
    }

    public void displayDetails() {
        System.out.println(this);
        System.out.println("   Valid safety credential: " + hasValidSafetyCredential());
    }

    @Override public String toString() {
        return String.format("ID: %s | Code: %s | Name: %s | Phone: %s | Role: %s", id, code, name, phoneNumber, role);
    }
}
