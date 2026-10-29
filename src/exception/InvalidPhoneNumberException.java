package exception;

/**
 * NGOẠI LỆ SỐ ĐIỆN THOẠI KHÔNG ĐÚNG ĐỊNH DẠNG (INVALID PHONE NUMBER)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Trần Ngọc Anh Tuấn (SE201513) - Core Dev
 * NHÁNH GIT: tuantna-person-exceptions
 * 
 * NOTE DÀNH CHO TUẤN:
 * - VỊ TRÍ CẦN LÀM:
 *   1. Sử dụng exception này trong Person.java (setter setPhoneNumber) hoặc trong PersonnelService.java (khi add/update).
 *   2. Kiểm tra nếu chuỗi số điện thoại không thỏa mãn chuẩn 10 số (bắt đầu bằng 0, chỉ chứa chữ số) thì ném:
 *      throw new InvalidPhoneNumberException(phone);
 */
public class InvalidPhoneNumberException extends DataValidationException {

    public InvalidPhoneNumberException(String phone) {
        super("phoneNumber", "Must contain 10 digits and start with 0.");
    }
}
