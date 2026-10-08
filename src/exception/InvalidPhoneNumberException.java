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
import exception.InvalidPhoneNumberException;
 
public class Person {
    // Valid format: exactly 10 digits, starting with 0
    private static final String PHONE_REGEX = "^0\\d{9}$";
 
    private String id;
    private String name;
    private String phoneNumber;
 
    public Person(String id, String name, String phoneNumber) throws InvalidPhoneNumberException {
        this.id = id;
        this.name = name;
        setPhoneNumber(phoneNumber); // always validated through the setter
    }
 
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches(PHONE_REGEX);
    }
 
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
 
    public void setPhoneNumber(String phone) throws InvalidPhoneNumberException {
        if (!isValidPhone(phone)) {
            throw new InvalidPhoneNumberException(phone);
        }
        this.phoneNumber = phone;
    }
 
    @Override
    public String toString() {
        return id + " | " + name + " | " + phoneNumber;
    }
}
