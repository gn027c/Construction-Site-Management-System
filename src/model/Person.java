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

}
