package model;

import exception.AppError;
import exception.DataValidationException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class Visitor extends Person {
    private static final DateTimeFormatter ENTRY_DATE = util.DateHelper.DATE_FORMATTER;

    private String purpose;
    private String hostPersonName;
    private String entryDate;

    public Visitor(String id, String code, String name, String phoneNumber,
                   String purpose, String hostPersonName, String entryDate) {
        super(id, code, name, phoneNumber);
        this.purpose = required(purpose, "purpose");
        this.hostPersonName = required(hostPersonName, "hostPersonName");
        this.entryDate = cleanDate(entryDate);
    }

    public String getPurpose() {
        return purpose;
    }

    public String getHostPersonName() {
        return hostPersonName;
    }

    public String getEntryDate() {
        return entryDate;
    }

    @Override
    public void changeVisitDate(String date) {
        this.entryDate = cleanDate(date);
    }

    @Override
    public boolean canHoldZoneAccess() {
        return false;
    }

    @Override
    public boolean canHoldPosition() {
        return false;
    }

    @Override
    public void setSafetyLevel(SafetyLevel safetyLevel) {
        if (safetyLevel != null && safetyLevel != SafetyLevel.NONE) {
            throw new AppError("Visitor cannot have safety level other than NONE.");
        }
        super.setSafetyLevel(SafetyLevel.NONE);
    }

    @Override
    public boolean canHandleIncidents() {
        return false;
    }

    @Override
    public String checkInDenialReason(LocalDate today) {
        if (!active) {
            return "Person is inactive and cannot check in";
        }
        if (entryDate.isEmpty()) {
            return "Record the visit date before this visitor checks in.";
        }
        if (!parseEntryDate(entryDate).equals(today)) {
            return "Visitor is only permitted to enter on " + entryDate;
        }
        return null;
    }

    @Override
    public boolean hasValidSafetyCredential() {
        return false;
    }

    @Override
    public String[] profileLabels() {
        return new String[] {"Purpose", "Host"};
    }

    @Override
    public String[] profileValues() {
        return new String[] {purpose, hostPersonName};
    }

    @Override
    protected void applyProfile(String[] values) {
        String nextPurpose = required(values[0], "purpose");
        hostPersonName = required(values[1], "hostPersonName");
        purpose = nextPurpose;
    }

    private static String cleanDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        parseEntryDate(text);
        return text.trim();
    }

    private static LocalDate parseEntryDate(String text) {
        try {
            return LocalDate.parse(text.trim(), ENTRY_DATE);
        } catch (DateTimeParseException e) {
            throw new DataValidationException("entryDate", "Must be a real date in dd/MM/yyyy format.");
        }
    }

    @Override
    public void displayDetails() {
        System.out.println("===== THÔNG TIN KHÁCH THAM QUAN (VISITOR) =====");
        System.out.println("ID: " + getId());
        System.out.println("Mã khách: " + getCode());
        System.out.println("Họ tên: " + getName());
        System.out.println("Số điện thoại: " + getPhoneNumber());
        System.out.println("Chức vụ: " + (getPosition().isEmpty() ? "-" : getPosition()));
        System.out.println("Mục đích thăm: " + purpose);
        System.out.println("Người đón tiếp / Bảo lãnh: " + hostPersonName);
        System.out.println("Ngày đến thăm: " + entryDate);
        System.out.println("===============================================");
    }

    @Override
    public String toCsvLine() {
        return csvLine("VISITOR", purpose, hostPersonName, entryDate);
    }

    @Override
    public String toString() {
        return String.format("Visitor{%s}", super.toString());
    }
}
