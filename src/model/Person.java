package model;

import java.io.Serializable;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
 * MÔ TẢ:
 * - Lớp cha trừu tượng (abstract class) cơ sở đại diện cho mọi cá nhân trên công trường.
 * - Chịu trách nhiệm quản lý các thuộc tính chung (id, code, name, phoneNumber, role).
 * - Cung cấp các phương thức trừu tượng displayDetails() và toCsvLine() cho các lớp con kế thừa.
 */
public abstract class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String code;
    protected String name;
    protected String phoneNumber;
    protected Role role;

    // Default Constructor
    public Person() {
    }

    // Parameterized Constructor
    public Person(String id, String code, String name, String phoneNumber, Role role) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    // Getters and Setters with validation
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public abstract void displayDetails();

    public abstract String toCsvLine();

    @Override
    public String toString() {
        return String.format("ID: %s | Mã: %s | Tên: %s | SĐT: %s | Vai trò: %s",
                id, code, name, phoneNumber, role != null ? role.name() : "N/A");
    }
}
