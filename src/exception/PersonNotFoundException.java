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

    public class PersonnelService {
    private final List<Person> personList = new ArrayList<>();

    // Before: returned null when not found
    // Now: throws PersonNotFoundException
    public Person findPersonByCode(String code) throws PersonNotFoundException {
        for (Person p : personList) {
            if (p.getCode().equalsIgnoreCase(code)) {
                return p;
            }
        }
        throw new PersonNotFoundException(code);
    }

    // Before: returned false when not found
    // Now: void, throws if the code does not exist
    public void updatePerson(String code, String newName, String newPhone)
            throws SiteManagementException {
        Person p = findPersonByCode(code);   // throws PersonNotFoundException
        p.setName(newName);
        p.setPhoneNumber(newPhone);          // throws InvalidPhoneNumberException
    }

    // Before: returned false when not found
    // Now: void, throws if the code does not exist
    public void deletePerson(String code) throws PersonNotFoundException {
        Person p = findPersonByCode(code);   // throws PersonNotFoundException
        personList.remove(p);
    }
}
