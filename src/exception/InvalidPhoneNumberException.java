package exception;

/**
 * NGOẠI LỆ SỐ ĐIỆN THOẠI KHÔNG ĐÚNG ĐỊNH DẠNG (INVALID PHONE NUMBER)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Trần Ngọc Anh Tuấn (SE201513) - Core Dev
 * NHÁNH GIT: tuantna-person-exceptions
 * 
 * NOTE DÀNH CHO TUẤN:
 * - ModelValidation dùng lỗi này cho số điện thoại không có 10 chữ số bắt đầu bằng 0.
 * - Giữ subtype riêng để controller xử lý, dùng chung cấu trúc thông báo field/reason.
 */
public class InvalidPhoneNumberException extends DataValidationException {

    public InvalidPhoneNumberException(String phone) {
        super("phoneNumber", "Must contain 10 digits and start with 0.");
    }
}
