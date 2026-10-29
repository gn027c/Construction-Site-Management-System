package model;

import static model.ModelValidation.*;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Nhà thầu phụ hoặc đối tác thi công.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Quản lý thông tin công ty thầu (companyName), hợp đồng thi công (contractId).
 * - Cài đặt các hành vi displayDetails() và toCsvLine().
 */
public class Contractor extends Person {
    private String companyName;
    private String contractId;

    public Contractor(String id, String code, String name, String phoneNumber, String companyName, String contractId) {
        super(id, code, name, phoneNumber, Role.CONTRACTOR);
        this.companyName = requiredText(companyName, "companyName");
        this.contractId = requiredText(contractId, "contractId");
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = requiredText(companyName, "companyName"); }
    public String getContractId() { return contractId; }
    public void setContractId(String contractId) { this.contractId = requiredText(contractId, "contractId"); }

    @Override
    public void displayDetails() {
        System.out.println("===== THÔNG TIN NHÀ THẦU (CONTRACTOR) =====");
        System.out.println("ID: " + getId());
        System.out.println("Mã nhà thầu: " + getCode());
        System.out.println("Họ tên đại diện: " + getName());
        System.out.println("Số điện thoại: " + getPhoneNumber());
        System.out.println("Vai trò: " + getRole());
        System.out.println("Tên công ty: " + companyName);
        System.out.println("Mã hợp đồng: " + contractId);
        System.out.println("===========================================");
    }

    @Override public boolean hasValidSafetyCredential() { return contractId.startsWith("CTR-"); }

    @Override public String toCsvLine() {
        return baseCsv("CONTRACTOR") + "," + util.CsvHelper.toCsvLine(contractId, companyName, "");
    }

    @Override public String toString() {
        return String.format("Contractor{%s, companyName='%s', contractId='%s'}", super.toString(), companyName, contractId);
    }
}
