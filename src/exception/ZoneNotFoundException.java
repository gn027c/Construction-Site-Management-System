package exception;

/**
 * NGOẠI LỆ KHÔNG TÌM THẤY PHÂN VÙNG (ZONE NOT FOUND)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Lê Tấn Thiên (SE201852) - Core Dev
 * NHÁNH GIT: thientt-zone-exceptions
 * 
 * NOTE DÀNH CHO THIÊN:
 * - VỊ TRÍ CẦN LÀM:
 *   1. Sử dụng exception này trong ZoneRepository.java hoặc ZoneService.java (phương thức findZoneById, verifyAccess).
 *   2. Nếu người dùng nhập mã phân vùng không tồn tại trong danh sách:
 *      throw new ZoneNotFoundException("Không tìm thấy phân vùng: " + zoneId);
 */
public class ZoneNotFoundException extends SiteManagementException {

    public ZoneNotFoundException(String zoneId) {
        super("Construction zone not found with ID: " + zoneId);
    }
}
