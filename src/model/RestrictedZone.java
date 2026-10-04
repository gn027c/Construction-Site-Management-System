package model;

import java.util.HashSet;
import java.util.Set;

/**
 * NHIỆM VỤ: Thành viên 3 (Lê Tấn Thiên - SE201852)
 * MÔ TẢ:
 * - Lớp đại diện cho khu vực nguy hiểm / hạn chế tiếp cận trên công trường.
 * - Kế thừa từ lớp cơ sở Zone.
 * - Quản lý cấp độ an toàn yêu cầu (requiredSafetyLevel) và danh sách mã nhân sự được cấp phép (allowedPersonCodes).
 * - Ghi đè phương thức checkAccess(Person person) để kiểm tra quyền truy cập nghiêm ngặt.
 * - Khai báo Enum SafetyLevel bên trong để định nghĩa cấp độ an toàn.
 */
public class RestrictedZone extends Zone {


    public enum SafetyLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    private SafetyLevel requiredSafetyLevel;
    private Set<String> allowedPersonCodes;

    // Parameterized Constructor
    public RestrictedZone(String zoneId, String zoneName, SafetyLevel requiredSafetyLevel, Set<String> allowedPersonCodes) {
        super(zoneId, zoneName);
        if (requiredSafetyLevel == null) {
            throw new IllegalArgumentException("Cấp độ an toàn (SafetyLevel) không được để trống (null).");
        }
        if (allowedPersonCodes == null) {
            throw new IllegalArgumentException("Danh sách nhân sự được cấp phép không được null.");
        }
        this.requiredSafetyLevel = requiredSafetyLevel;
        this.allowedPersonCodes = new HashSet<>(allowedPersonCodes);
    }

    public SafetyLevel getRequiredSafetyLevel() {
        return requiredSafetyLevel;
    }

    public void setRequiredSafetyLevel(SafetyLevel requiredSafetyLevel) {
        if (requiredSafetyLevel == null) {
            throw new IllegalArgumentException("Cấp độ an toàn (SafetyLevel) không được để trống (null).");
        }
        this.requiredSafetyLevel = requiredSafetyLevel;
    }

    public Set<String> getAllowedPersonCodes() {
        return allowedPersonCodes;
    }

    public void setAllowedPersonCodes(Set<String> allowedPersonCodes) {
        if (allowedPersonCodes == null) {
            throw new IllegalArgumentException("Danh sách nhân sự không được null.");
        }
        this.allowedPersonCodes = new HashSet<>(allowedPersonCodes);
    }

    public void grantAccess(String personCode) {
        if (personCode != null && !personCode.trim().isEmpty()) {
            allowedPersonCodes.add(personCode.trim());
        }
    }

    public void revokeAccess(String personCode) {
        if (personCode != null) {
            allowedPersonCodes.remove(personCode.trim());
        }
    }

    @Override
    public boolean checkAccess(Person person) {
        if (person == null || person.getCode() == null) {
            return false;
        }
        boolean isAccessGranted = allowedPersonCodes.contains(person.getCode().trim());
        boolean hasValidSafety = person.hasValidSafetyCredential();

        return isAccessGranted && hasValidSafety;
    }

    @Override
    public String toString() {
        return String.format("RestrictedZone{%s, requiredSafetyLevel=%s, allowedCount=%d}",
                super.toString(), requiredSafetyLevel, allowedPersonCodes.size());
    }
}
