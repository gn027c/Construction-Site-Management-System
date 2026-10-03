package model;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Công nhân thi công trên công trường.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Chịu trách nhiệm quản lý thông tin ngành nghề, chứng chỉ an toàn và tổ đội thi công.
 * - Cài đặt các hành vi hiển thị chi tiết và chuẩn bị dữ liệu xuất báo cáo theo yêu cầu Workshop 1.
 */
public class Worker extends Person {
    private String safetyCertId;

    public Worker(String id, String code, String name, String phoneNumber, String safetyCertId) {
        super(id, code, name, phoneNumber, Role.WORKER);
        this.safetyCertId = safetyCertId;
    }

    public String getSafetyCertId() { return safetyCertId; }
    public void setSafetyCertId(String safetyCertId) { this.safetyCertId = safetyCertId; }

    // Valid when safetyCertId is not empty and ends with "_SAFE"
    @Override
    public boolean hasValidSafetyCredential() {
        return safetyCertId != null
                && !safetyCertId.trim().isEmpty()
                && safetyCertId.endsWith("_SAFE");
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s,%s",
                getId(), getCode(), getName(), getPhoneNumber(), getRole(),
                safetyCertId);
    }

    @Override
    public String toString() {
        return String.format("Worker{%s, safetyCertId='%s'}",
                super.toString(), safetyCertId);
    }
