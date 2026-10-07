package exception;

/**
 * ============================================================================
 * LỚP NGOẠI LỆ CƠ SỞ (BASE CHECKED EXCEPTION) CHO TOÀN BỘ HỆ THỐNG SMARTSITE
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG CHÍNH: Huỳnh Nguyễn Hoàng Khang (SE201461) - Nhóm trưởng
 * NHÁNH GIT: khanghnh-exception-core
 * 
 * PHÂN CÔNG TỔNG THỂ WORKSHOP 4 (EXCEPTION HANDLING & TESTING):
 * 1. Khang (SE201461): SiteManagementException, DataValidationException, UI Try-Catch
 * 2. Tuấn (SE201513): DuplicatePersonCodeException, PersonNotFoundException, InvalidPhoneNumberException
 * 3. Thiên (SE201852): ZoneNotFoundException, RestrictedZoneViolationException, DuplicateZoneIdException
 * 4. Lợi (SE211059): AttendanceOrderException, IncidentNotFoundException, InvalidIncidentStateException
 * ============================================================================
 * 
 * NOTE DÀNH CHO THÀNH VIÊN:
 * - Đây là lớp ngoại lệ cha (Checked Exception) kế thừa từ java.lang.Exception.
 * - Mọi custom exception đặc thù của các module khác đều kế thừa từ lớp này.
 */
public class SiteManagementException extends Exception {

    public SiteManagementException() {
        super("An error occurred in SmartSite Management System.");
    }

    public SiteManagementException(String message) {
        super(message);
    }

    public SiteManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
