package service;

import model.Person;
import repository.PersonRepository;
import java.util.List;

/**
 * TẦNG NGHIỆP VỤ (SERVICE LAYER)
 * PHỤ TRÁCH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ: Xử lý các quy tắc nghiệp vụ liên quan đến hồ sơ nhân sự.
 */
public class PersonnelService {
    private final PersonRepository personRepository;

    public PersonnelService() {
        this.personRepository = new PersonRepository();
    }

    public PersonnelService(PersonRepository personRepository) {
        this.personRepository = (personRepository != null) ? personRepository : new PersonRepository();
    }

   public boolean addPerson(Person p) {
    if (p == null || p.getCode() == null) {
        return false;
    }

    // Check for duplicate code
    if (isDuplicate(p.getCode())) {
        return false;
    }

    return personRepository.save(p);
}

private boolean isDuplicate(String code) {
    return personRepository.findByCode(code) != null;
}

    public Person findPersonByCode(String code) {
    Person person = personRepository.findByCode(code);
    if (person == null) {
        throw new PersonNotFoundException(code);
    }
    return person;
}

   public boolean updatePerson(String code, String newName, String newPhone) {
    Person p = findPersonByCode(code); // throws PersonNotFoundException if not found

    p.setName(newName);
    p.setPhoneNumber(newPhone);
    return true;
}

   public boolean deletePerson(String code) {
    Person existing = findPersonByCode(code); // throws if not found
    return personRepository.delete(existing);
}

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }
}
