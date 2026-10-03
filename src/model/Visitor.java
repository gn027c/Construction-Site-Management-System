package model;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Khách tham quan hoặc đoàn thanh tra vãng lai.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Chịu trách nhiệm quản lý thông tin mục đích thăm, người bảo lãnh tiếp đoàn và ngày đến thăm.
 * - Cài đặt các hành vi hiển thị chi tiết và chuẩn bị dữ liệu xuất báo cáo theo yêu cầu Workshop 1.
 */
public class Visitor extends Person {

    public Visitor(String id, String code, String name, String phoneNumber) {
        super(id, code, name, phoneNumber, Role.VISITOR);
    }

    // Visitors never hold a safety credential
    @Override
    public boolean hasValidSafetyCredential() {
        return false;
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s",
                getId(), getCode(), getName(), getPhoneNumber(), getRole());
    }

    @Override
    public String toString() {
        return String.format("Visitor{%s}", super.toString());
    }
