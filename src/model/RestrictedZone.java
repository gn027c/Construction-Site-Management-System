package model;

import static model.ModelValidation.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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
    public enum SafetyLevel { LOW, MEDIUM, HIGH, CRITICAL }

    private SafetyLevel requiredSafetyLevel;
    private Set<String> allowedPersonCodes;

    public RestrictedZone(String zoneId, String zoneName, SafetyLevel requiredSafetyLevel, Set<String> allowedPersonCodes) {
        super(zoneId, zoneName);
        this.requiredSafetyLevel = requiredValue(requiredSafetyLevel, "requiredSafetyLevel");
        this.allowedPersonCodes = validatedCodes(allowedPersonCodes);
    }

    private static Set<String> validatedCodes(Set<String> codes) {
        requiredValue(codes, "allowedPersonCodes");
        Set<String> normalized = new HashSet<>();
        for (String code : codes) normalized.add(personCode(code, "allowedPersonCodes"));
        return normalized;
    }

    public SafetyLevel getRequiredSafetyLevel() { return requiredSafetyLevel; }
    public void setRequiredSafetyLevel(SafetyLevel level) { requiredSafetyLevel = requiredValue(level, "requiredSafetyLevel"); }
    public Set<String> getAllowedPersonCodes() { return new HashSet<>(allowedPersonCodes); }
    public void setAllowedPersonCodes(Set<String> codes) { allowedPersonCodes = validatedCodes(codes); }

    public boolean grantAccess(String code) { return allowedPersonCodes.add(personCode(code, "personCode")); }
    public boolean revokeAccess(String code) { return allowedPersonCodes.remove(personCode(code, "personCode")); }

    // Hazard level is metadata until personnel credentials have comparable levels.
    @Override public boolean checkAccess(Person person) {
        return person != null && allowedPersonCodes.contains(person.getCode()) && person.hasValidSafetyCredential();
    }

    @Override public String toCsvLine() {
        String codes = allowedPersonCodes.isEmpty() ? "NONE" : String.join(";", new TreeSet<>(allowedPersonCodes));
        return util.CsvHelper.toCsvLine("RESTRICTED", getZoneId(), getZoneName(), requiredSafetyLevel.name(), codes);
    }

    @Override public String toString() {
        return String.format("RestrictedZone{%s, requiredSafetyLevel=%s, allowedCount=%d}", super.toString(), requiredSafetyLevel, allowedPersonCodes.size());
    }
}
