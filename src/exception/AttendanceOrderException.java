package exception;

/**
 * NGOẠI LỆ VI PHẠM THỨ TỰ ĐIỂM DANH (ATTENDANCE ORDER EXCEPTION - FSM VIOLATION)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Nguyễn Tấn Lợi (SE211059) - Data Specialist
 * NHÁNH GIT: loint-attendance-exceptions
 * 
 * NOTE DÀNH CHO LỢI:
 * - VỊ TRÍ CẦN LÀM:
 *   1. Sử dụng exception này trong AttendanceService.java (phương thức processAttendance / recordCheckIn / recordCheckOut).
 *   2. Ném ra khi:
 *      - Quẹt Check-in 2 lần liên tiếp mà chưa Check-out.
 *      - Quẹt Check-out khi chưa từng Check-in hoặc quẹt Check-out khác zone đang vào.
 *      throw new AttendanceOrderException(personCode, "Check-in liên tiếp mà chưa Check-out");
 */
public class AttendanceOrderException extends SiteManagementException {

    public AttendanceOrderException(String personCode, String violationDetail) {
        super("Attendance FSM Violation for Person [" + personCode + "]: " + violationDetail);
    }
}
