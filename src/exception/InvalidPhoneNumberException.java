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

    public class Person {
    private String phoneNumber;
    // ... các field khác

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) throws InvalidPhoneNumberException {
        if (!isValidPhone(phoneNumber)) {
            throw new InvalidPhoneNumberException(phoneNumber);
        }
        this.phoneNumber = phoneNumber;
    }

    /** 10 chữ số, bắt đầu bằng 0, chỉ chứa chữ số */
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^0\\d{9}$");
    }

       public void addPerson(Person p) throws InvalidPhoneNumberException {
    if (!Person.isValidPhone(p.getPhoneNumber())) {
        throw new InvalidPhoneNumberException(p.getPhoneNumber());
    }
    // ... thêm vào danh sách
}

public void updatePhone(String id, String newPhone) throws InvalidPhoneNumberException {
    Person p = findById(id);
    if (p != null) {
        p.setPhoneNumber(newPhone); // setter tự ném exception nếu sai
    }
}
}
}
