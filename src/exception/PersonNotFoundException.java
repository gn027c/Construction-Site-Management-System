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
 *   2. Thay vì chỉ trả về null hoặc false khi không tìm thấy mã nhân sự, Tuấn có thể ném exception này:
 *      throw new PersonNotFoundException("Không tìm thấy nhân sự với mã: " + code);
 */

import exception.InvalidPhoneNumberException;
import exception.PersonNotFoundException;
import exception.SiteManagementException;
import model.Person;
 
import java.util.ArrayList;
import java.util.List;
 
public class PersonNotFoundException extends SiteManagementException {

public class PersonnelService {
    private final List<Person> people = new ArrayList<>();
 
    public void addPerson(Person person) throws SiteManagementException {
        // Re-check on add (in case the object comes from another source)
        if (!Person.isValidPhone(person.getPhoneNumber())) {
            throw new InvalidPhoneNumberException(person.getPhoneNumber());
        }
        people.add(person);
    }
 
    /** Finds a person by code; throws PersonNotFoundException instead of returning null. */
    public Person findPersonByCode(String code) throws PersonNotFoundException {
        for (Person p : people) {
            if (p.getCode().equals(code)) {
                return p;
            }
        }
        throw new PersonNotFoundException(code);
    }
 
    /** Updates name and phone of an existing person. */
    public void updatePerson(String code, String newName, String newPhone)
            throws SiteManagementException {
        Person p = findPersonByCode(code);   // throws PersonNotFoundException if missing
        p.setPhoneNumber(newPhone);          // throws InvalidPhoneNumberException if invalid
        p.setName(newName);
    }
 
    /** Deletes a person by code; throws PersonNotFoundException instead of returning false. */
    public void deletePerson(String code) throws PersonNotFoundException {
        Person p = findPersonByCode(code);
        people.remove(p);
    }
 
    public List<Person> getAll() {
        return people;
    }
}
