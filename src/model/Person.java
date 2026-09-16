package model;

import java.io.Serializable;

/**
 * Lớp cha trừu tượng cơ sở cho nhân sự công trường.
 * @author Huỳnh Nguyễn Hoàng Khang - SE201461
 */
public abstract class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String id;
    protected String code;
    protected String name;
    protected String phoneNumber;
    protected Role role;

    public Person() {
    }

    public Person(String id, String code, String name, String phoneNumber, Role role) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role != null ? role : Role.WORKER;
    }

    // Các hàm getter setter cơ bản
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
