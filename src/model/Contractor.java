package model;

public class Contractor extends Person {
    private String companyName;
    private String contractId;

    public Contractor(String id, String code, String name, String phoneNumber,
                      String companyName, String contractId) {
        super(id, code, name, phoneNumber);
        this.companyName = required(companyName, "companyName");
        this.contractId = required(contractId, "contractId");
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getContractId() {
        return contractId;
    }

    @Override
    public boolean hasValidSafetyCredential() {
        return contractId != null
                && !contractId.trim().isEmpty()
                && contractId.startsWith("CTR-");
    }

    @Override
    public String[] profileLabels() {
        return new String[] {"Company", "Contract"};
    }

    @Override
    public String[] profileValues() {
        return new String[] {companyName, contractId};
    }

    @Override
    protected void applyProfile(String[] values) {
        String nextCompany = required(values[0], "companyName");
        contractId = required(values[1], "contractId");
        companyName = nextCompany;
    }

    @Override
    public void displayDetails() {
        System.out.println("===== THÔNG TIN NHÀ THẦU (CONTRACTOR) =====");
        System.out.println("ID: " + getId());
        System.out.println("Mã nhà thầu: " + getCode());
        System.out.println("Họ tên đại diện: " + getName());
        System.out.println("Số điện thoại: " + getPhoneNumber());
        System.out.println("Chức vụ: " + (getPosition().isEmpty() ? "-" : getPosition()));
        System.out.println("Tên công ty: " + companyName);
        System.out.println("Mã hợp đồng: " + contractId);
        System.out.println("===========================================");
    }

    @Override
    public String toCsvLine() {
        return csvLine("CONTRACTOR", contractId, companyName, "");
    }

    @Override
    public String toString() {
        return String.format("Contractor{%s, companyName='%s', contractId='%s'}",
                super.toString(), companyName, contractId);
    }
}
