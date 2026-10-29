package model;

import static model.ModelValidation.*;

/**
 * Represents a visitor with a required visit purpose and host name.
 *
 * <p>The entry date uses {@code dd/MM/yyyy}; an empty string means the date is
 * unknown. Visitors do not satisfy the current safety credential rule.
 */
public class Visitor extends Person {
    private String purpose;
    private String hostPersonName;
    private String entryDate;

    public Visitor(
            String id,
            String code,
            String name,
            String phoneNumber,
            String purpose,
            String hostPersonName,
            String entryDate
    ) {
        super(id, code, name, phoneNumber, Role.VISITOR);
        this.purpose = requiredText(purpose, "purpose");
        this.hostPersonName = requiredText(hostPersonName, "hostPersonName");
        this.entryDate = optionalDate(entryDate, "entryDate");
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = requiredText(purpose, "purpose");
    }

    public String getHostPersonName() {
        return hostPersonName;
    }

    public void setHostPersonName(String hostPersonName) {
        this.hostPersonName = requiredText(hostPersonName, "hostPersonName");
    }

    /**
     * Returns the visit date as {@code dd/MM/yyyy}, or an empty string when unknown.
     */
    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = optionalDate(entryDate, "entryDate");
    }

    @Override
    public void displayDetails() {
        System.out.println("===== THÔNG TIN KHÁCH THAM QUAN (VISITOR) =====");
        System.out.println("ID: " + getId());
        System.out.println("Mã khách: " + getCode());
        System.out.println("Họ tên: " + getName());
        System.out.println("Số điện thoại: " + getPhoneNumber());
        System.out.println("Vai trò: " + getRole());
        System.out.println("Mục đích thăm: " + purpose);
        System.out.println("Người đón tiếp / Bảo lãnh: " + hostPersonName);
        System.out.println("Ngày đến thăm: " + (entryDate.isEmpty() ? "Chưa có thông tin" : entryDate));
        System.out.println("===============================================");
    }

    @Override
    public boolean hasValidSafetyCredential() {
        return false;
    }

    @Override
    public String toCsvLine() {
        return baseCsv("VISITOR") + "," + util.CsvHelper.toCsvLine(purpose, hostPersonName, entryDate) + csvStatus();
    }

    @Override
    public String toString() {
        return "Visitor{" + super.toString() + "}";
    }
}
