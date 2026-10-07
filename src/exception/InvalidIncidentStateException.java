package exception;

/**
 * NGOẠI LỆ CHUYỂN TRẠNG THÁI SỰ CỐ KHÔNG HỢP LỆ (INVALID INCIDENT STATE TRANSITION)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Nguyễn Tấn Lợi (SE211059) - Data Specialist
 * NHÁNH GIT: loint-attendance-exceptions
 * 
 * NOTE DÀNH CHO LỢI:
 * - VỊ TRÍ CẦN LÀM:
 *   1. Sử dụng exception này trong IncidentService.java (phương thức assignIncident, resolveIncident, closeIncident).
 *   2. Ném ra khi vi phạm luồng vòng đời sự cố (OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED):
 *      - Ví dụ: Đóng một sự cố đang ở trạng thái OPEN mà chưa từng được phân công giải quyết.
 *      throw new InvalidIncidentStateException(incidentId, currentStatus, "Không thể chuyển sang trạng thái mới");
 */
public class InvalidIncidentStateException extends SiteManagementException {

    public InvalidIncidentStateException(String incidentId, String currentState, String reason) {
        super("Invalid State Transition for Incident [" + incidentId + "] currently in state [" + currentState + "]: " + reason);
    }
}
