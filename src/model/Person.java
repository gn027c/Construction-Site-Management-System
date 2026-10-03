package model;

import java.io.Serializable;

/**
 * NHIỆM VỤ: Thành viên 1 (Huỳnh Nguyễn Hoàng Khang - SE201461)
 * MÔ TẢ:
 * - Lớp cha trừu tượng (abstract class) cơ sở đại diện cho mọi cá nhân trên công trường.
 * - Chịu trách nhiệm quản lý các thuộc tính chung (id, code, name, phoneNumber, role).
 * - Cung cấp các phương thức trừu tượng displayDetails() và toCsvLine() cho các lớp con kế thừa.
 */
public abstract boolean hasValidSafetyCredential();

    // CSV line used when saving to file
    public abstract String toCsvLine();

    // Polymorphic display: each subclass prints its own details via toString()
    public void displayDetails() {
        System.out.println(this);
        System.out.println("   Valid safety credential: " + hasValidSafetyCredential());
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Code: %s | Name: %s | Phone: %s | Role: %s",
                id, code, name, phoneNumber, role != null ? role.name() : "N/A");
    }
