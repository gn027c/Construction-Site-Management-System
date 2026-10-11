package service;

import model.Person;
import repository.PersonRepository;
import java.util.List;
import com.example.exception.DuplicatePersonCodeException;   
import com.example.exception.PersonNotFoundException;   

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

   public boolean addPerson(Person p) throws DuplicatePersonException {
    if (p == null || p.getCode() == null) {
        return false;
    }

   // Check for duplicate code
if (isDuplicate(p.getCode())) {
    throw new DuplicatePersonCodeException(p.getCode());
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

   public boolean updatePerson(String code, String newName, String newPhone)
        throws PersonNotFoundException {
    if (newName == null || newName.isBlank() || newPhone == null || newPhone.isBlank()) {
        throw new IllegalArgumentException("Name and phone must not be empty");
    }

    Person p = findPersonByCode(code);

    p.setName(newName);
    p.setPhoneNumber(newPhone);
    return true;
}

   public boolean deletePerson(String code) throws PersonNotFoundException {
    if (!personRepository.deleteByCode(code)) {
        throw new PersonNotFoundException(code);
    }
    return true;
}

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }
    
