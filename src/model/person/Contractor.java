package model.person;

import static model.common.ModelValidation.*;

public class Contractor extends Person {
    private String companyName;
    private String contractId;

    public Contractor(String id, String code, String name, String phoneNumber, String companyName, String contractId) {
        super(id, code, name, phoneNumber, Role.CONTRACTOR);
        this.companyName = requiredText(companyName, "companyName");
        this.contractId = requiredText(contractId, "contractId");
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = requiredText(companyName, "companyName");
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = requiredText(contractId, "contractId");
    }

    @Override
    public boolean hasValidSafetyCredential() {
        return contractId.startsWith("CTR-");
    }

    @Override
    public boolean shouldProvisionAccount() {
        return true;
    }

    @Override
    public String toCsvLine() {
        return baseCsv("CONTRACTOR") + "," + util.CsvHelper.toCsvLine(contractId, companyName, "") + csvStatus();
    }

    @Override
    public String toString() {
        return String.format(
                "Contractor{%s, companyName='%s', contractId='%s'}",
                super.toString(),
                companyName,
                contractId
        );
    }
}
