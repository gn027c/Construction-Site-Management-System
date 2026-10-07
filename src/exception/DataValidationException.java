package exception;

/**
 * NGOẠI LỆ KIỂM TRA ĐỊNH DẠNG DỮ LIỆU ĐẦU VÀO (DATA VALIDATION)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Huỳnh Nguyễn Hoàng Khang (SE201461) - Nhóm trưởng
 * NHÁNH GIT: khanghnh-exception-core
 * 
 * NOTE DÀNH CHO KHANG:
 * - Ném ra khi dữ liệu đầu vào chuỗi rỗng, khoảng trắng, hoặc sai định dạng cơ bản.
 * - Kế thừa từ SiteManagementException.
 */
public class DataValidationException extends SiteManagementException {

    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(String fieldName, String reason) {
        super("Invalid value for field [" + fieldName + "]: " + reason);
    }
}
