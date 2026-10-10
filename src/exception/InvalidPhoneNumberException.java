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
    public abstract class Person {
    // Regex: starts with 0, followed by exactly 9 digits => 10 digits total
    private static final String PHONE_REGEX = "0\\d{9}";

    private String phoneNumber;
    // ... other fields

    public void setPhoneNumber(String phoneNumber) throws InvalidPhoneNumberException {
        if (phoneNumber == null || !phoneNumber.trim().matches(PHONE_REGEX)) {
            throw new InvalidPhoneNumberException(phoneNumber);
        }
        this.phoneNumber = phoneNumber.trim();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}

public void addPerson(Person p) throws SiteManagementException {
    // The phone was already validated when the Person was created,
    // but you can check again if the Person may come from elsewhere
    if (p == null) {
        throw new SiteManagementException("Person must not be null");
    }
    personList.add(p);
}

public void updatePhone(String id, String newPhone) throws SiteManagementException {
    Person p = findById(id);          // may throw PersonNotFoundException
    p.setPhoneNumber(newPhone);       // throws InvalidPhoneNumberException if invalid
}
}
