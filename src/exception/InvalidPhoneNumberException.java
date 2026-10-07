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
 *      throw new InvalidPhoneNumberException("Số điện thoại không hợp lệ: " + phone);
 */
public class InvalidPhoneNumberException extends SiteManagementException {

    public InvalidPhoneNumberException(String phone) {
        super("Invalid phone number format: " + phone + ". Expected 10 digits starting with 0.");
    }
}
