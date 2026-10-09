package exception;

/**
 * NGOẠI LỆ KHÔNG TÌM THẤY HỒ SƠ SỰ CỐ (INCIDENT NOT FOUND)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Nguyễn Tấn Lợi (SE211059) - Data Specialist
 * NHÁNH GIT: loint-attendance-exceptions
 * 
 * NOTE DÀNH CHO LỢI:
 * - VỊ TRÍ CẦN LÀM:
 *   1. Sử dụng exception này trong IncidentRepository.java hoặc IncidentService.java.
 *   2. Ném ra khi tra cứu, gán người xử lý, giải quyết hoặc đóng sự cố mà mã sự cố không tồn tại:
 *      throw new IncidentNotFoundException(incidentId);
 */
public class IncidentNotFoundException extends SiteManagementException {

    public IncidentNotFoundException(String incidentId) {
        super("Incident record not found with ID: " + incidentId);
    }
}
