package exception;

/**
 * NGOẠI LỆ VI PHẠM TRUY CẬP KHU VỰC HẠN CHẾ (RESTRICTED ZONE VIOLATION)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Lê Tấn Thiên (SE201852) - Core Dev
 * NHÁNH GIT: thientt-zone-exceptions
 * 
 * NOTE DÀNH CHO THIÊN:
 * - VỊ TRÍ CẦN LÀM:
 *   1. Sử dụng exception này trong ZoneService.java (phương thức verifyAccess) hoặc RestrictedZone.java.
 *   2. Khi đối tượng nhân sự không đủ quyền (không phải Safety Officer/Site Manager, không có trong whitelist,
 *      hoặc Worker không có chứng chỉ an toàn hợp lệ) mà cố tình truy cập vào RestrictedZone:
 *      throw new RestrictedZoneViolationException(personCode, zoneId, "Không có quyền truy cập khu vực hạn chế");
 */
public class RestrictedZoneViolationException extends SiteManagementException {

    public RestrictedZoneViolationException(String personCode, String zoneId, String reason) {
        super("Access Denied: Person [" + personCode + "] is not permitted to enter Restricted Zone [" + zoneId + "]. Reason: " + reason);
    }
}
