package service;

import exception.DuplicatePersonCodeException;
import exception.InvalidPhoneNumberException;
import exception.PersonNotFoundException;
import model.Person;
import repository.PersonRepository;
import java.util.List;

/**
 * TẦNG NGHIỆP VỤ (SERVICE LAYER)
 * PHỤ TRÁCH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ: Xử lý các quy tắc nghiệp vụ liên quan đến hồ sơ nhân sự có xử lý Exception.
 */
public class PersonnelService {
    public static final String PHONE_REGEX = "0\\d{9}";
    private final PersonRepository personRepository;

    public PersonnelService() {
        this.personRepository = new PersonRepository();
    }

    public PersonnelService(PersonRepository personRepository) {
        this.personRepository = (personRepository != null) ? personRepository : new PersonRepository();
    }

    public boolean addPerson(Person p) throws DuplicatePersonCodeException, InvalidPhoneNumberException {
        if (p == null || p.getCode() == null || p.getCode().trim().isEmpty()) {
            System.out.println("Error: Invalid person data!");
            return false;
        }

        // Kiểm tra hợp lệ số điện thoại (10 chữ số bắt đầu bằng 0)
        String phone = p.getPhoneNumber();
        if (phone == null || !phone.trim().matches(PHONE_REGEX)) {
            throw new InvalidPhoneNumberException(phone);
        }

        // Lưu vào repository (tự động ném DuplicatePersonCodeException nếu trùng mã)
        if (personRepository.save(p)) {
            System.out.println("Successfully added person: " + p.getName() + " (" + p.getCode() + ")");
            return true;
        }
        return false;
    }

    public Person findPersonByCode(String code) throws PersonNotFoundException {
        return personRepository.findByCodeOrThrow(code);
    }

    public boolean updatePerson(String code, String newName, String newPhone)
            throws PersonNotFoundException, InvalidPhoneNumberException {
        Person p = findPersonByCode(code);

        if (newPhone == null || !newPhone.trim().matches(PHONE_REGEX)) {
            throw new InvalidPhoneNumberException(newPhone);
        }

        if (newName != null && !newName.trim().isEmpty()) {
            p.setName(newName.trim());
        }
        p.setPhoneNumber(newPhone.trim());
        System.out.println("Successfully updated person: " + p.getCode());
        return true;
    }

    public boolean deletePerson(String code) throws PersonNotFoundException {
        return personRepository.deleteByCode(code);
    }

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }
}
