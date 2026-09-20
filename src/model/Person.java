package model;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
 * MÔ TẢ:
 * - Lớp cha trừu tượng (abstract class) cơ sở đại diện cho mọi cá nhân trên công trường.
 * - Chịu trách nhiệm quản lý các thuộc tính chung (id, code, name, phoneNumber, role).
 * - Cung cấp các phương thức trừu tượng displayDetails() và toCsvLine() cho các lớp con kế thừa.
 */
public abstract class Person {
    protected String id;
    protected String code;
    protected String name;
    protected String phoneNumber;
    protected Role role;

    // Parameterized Constructor - Bắt buộc đầy đủ thông tin
    public Person(String id, String code, String name, String phoneNumber, Role role) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID không được để trống.");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã (code) không được để trống.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Vai trò (role) không được để trống.");
        }
        this.id = id.trim();
        this.code = code.trim();
        this.name = name.trim();
        this.phoneNumber = phoneNumber.trim();
        this.role = role;
    }

    // Getters and Setters with validation
    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID không được để trống.");
        }
        this.id = id.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã (code) không được để trống.");
        }
        this.code = code.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        this.name = name.trim();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
        this.phoneNumber = phoneNumber.trim();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Vai trò (role) không được để trống.");
        }
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
