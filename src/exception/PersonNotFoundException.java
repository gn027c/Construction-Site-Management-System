package exception;

/**
 * NGOẠI LỆ KHÔNG TÌM THẤY HỒ SƠ NHÂN SỰ (PERSON NOT FOUND)
 * ----------------------------------------------------------------------------
 * PHÂN CÔNG: Trần Ngọc Anh Tuấn (SE201513) - Core Dev
 * NHÁNH GIT: tuantna-person-exceptions
 * 
 * NOTE DÀNH CHO TUẤN:
 * - VỊ TRÍ CẦN LÀM:
 *   1. Sử dụng exception này trong PersonnelService.java (phương thức findPersonByCode, updatePerson, deletePerson).
 *   2. Thay vì chỉ trả về null hoặc false khi không tìm thấy mã nhân sự, Tuấn ném exception này:
 *      throw new PersonNotFoundException(code);
 */
public class PersonNotFoundException extends SiteManagementException {

    public PersonNotFoundException(String code) {
        super("Personnel record not found with code: " + code);
    }
}
