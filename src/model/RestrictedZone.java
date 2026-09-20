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
 */
public class RestrictedZone extends Zone {
    private String requiredSafetyLevel;
    private Set<String> allowedPersonCodes;

    // Parameterized Constructor - Yêu cầu đầy đủ thông tin bắt buộc
    public RestrictedZone(String zoneId, String zoneName, String requiredSafetyLevel, Set<String> allowedPersonCodes) {
        super(zoneId, zoneName);
        if (requiredSafetyLevel == null || requiredSafetyLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Cấp độ an toàn yêu cầu không được để trống.");
        }
        if (allowedPersonCodes == null) {
            throw new IllegalArgumentException("Danh sách nhân sự được cấp phép không được null.");
        }
        this.requiredSafetyLevel = requiredSafetyLevel.trim();
        this.allowedPersonCodes = new HashSet<>(allowedPersonCodes);
    }

    public String getRequiredSafetyLevel() {
        return requiredSafetyLevel;
    }

    public void setRequiredSafetyLevel(String requiredSafetyLevel) {
        if (requiredSafetyLevel == null || requiredSafetyLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Cấp độ an toàn yêu cầu không được để trống.");
        }
        this.requiredSafetyLevel = requiredSafetyLevel.trim();
    }

    public Set<String> getAllowedPersonCodes() {
        return allowedPersonCodes;
    }

    public void setAllowedPersonCodes(Set<String> allowedPersonCodes) {
        if (allowedPersonCodes == null) {
            throw new IllegalArgumentException("Danh sách nhân sự được cấp phép không được null.");
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

    /**
     * Ghi đè kiểm tra quyền truy cập:
     * - Chỉ người có mã trong allowedPersonCodes mới được vào khu vực hạn chế.
     */
    @Override
    public boolean checkAccess(Person person) {
        if (person == null || person.getCode() == null) {
            return false;
        }
        return allowedPersonCodes.contains(person.getCode().trim());
    }

    @Override
    public String toString() {
        return String.format("RestrictedZone{%s, requiredSafetyLevel='%s', allowedCount=%d}",
                super.toString(), requiredSafetyLevel, allowedPersonCodes.size());
    }
}
