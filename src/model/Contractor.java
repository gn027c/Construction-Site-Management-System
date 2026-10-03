package model;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Nhà thầu phụ hoặc đối tác thi công.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Chịu trách nhiệm quản lý thông tin công ty thầu, hợp đồng thi công và chức danh đại diện.
 * - Cài đặt các hành vi hiển thị chi tiết và chuẩn bị dữ liệu xuất báo cáo theo yêu cầu Workshop 1.
 */
public class Contractor extends Person {
    private String companyName;
    private String contractId;

    public Contractor(String id, String code, String name, String phoneNumber,
                      String companyName, String contractId) {
        super(id, code, name, phoneNumber, Role.CONTRACTOR);
        this.companyName = companyName;
        this.contractId = contractId;
    }

    public String getCompanyName() { return companyName; }
    public String getContractId() { return contractId; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setContractId(String contractId) { this.contractId = contractId; }

    // Valid when contractId is not empty and starts with "CTR-"
    @Override
    public boolean hasValidSafetyCredential() {
        return contractId != null
                && !contractId.trim().isEmpty()
                && contractId.startsWith("CTR-");
    }

    @Override
    public String toCsvLine() {
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                getId(), getCode(), getName(), getPhoneNumber(), getRole(),
                companyName, contractId);
    }

    @Override
    public String toString() {
        return String.format("Contractor{%s, companyName='%s', contractId='%s'}",
                super.toString(), companyName, contractId);
    }
