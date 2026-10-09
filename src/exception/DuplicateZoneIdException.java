package exception;

/**
 * NGOẠI LỆ TRÙNG LẶP MÃ PHÂN VÙNG (DUPLICATE ZONE ID)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Lê Tấn Thiên (SE201852) - Core Dev
 * NHÁNH GIT: thientt-zone-exceptions
 * 
 * NOTE DÀNH CHO THIÊN:
 * - VỊ TRÍ CẦN LÀM:
 *   1. Sử dụng exception này trong ZoneRepository.java hoặc ZoneService.java (phương thức addZone).
 *   2. Kiểm tra nếu mã phân vùng (Zone.getZoneId()) đã tồn tại thì ném exception:
 *      throw new DuplicateZoneIdException(zoneId);
 */
public class DuplicateZoneIdException extends SiteManagementException {

    public DuplicateZoneIdException(String zoneId) {
        super("Zone ID already exists in the system: " + zoneId);
    }
}
