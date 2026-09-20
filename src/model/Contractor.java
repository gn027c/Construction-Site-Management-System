package model;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Nhà thầu phụ hoặc đối tác thi công.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Quản lý thông tin công ty thầu (companyName), hợp đồng thi công (contractId).
 * - Cài đặt các hành vi displayDetails() và toCsvLine() theo yêu cầu Workshop 1.
 */
public class Contractor extends Person {
    private String companyName;
    private String contractId;

    public Contractor() {
        super();
        this.role = Role.CONTRACTOR;
    }

    public Contractor(String id, String code, String name, String phoneNumber,
                      String companyName, String contractId) {
        super(id, code, name, phoneNumber, Role.CONTRACTOR);
        this.companyName = companyName;
        this.contractId = contractId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

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
}
