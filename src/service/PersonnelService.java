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
    private final PersonRepository personRepository;

    public PersonnelService() {
        this.personRepository = new PersonRepository();
    }

    public PersonnelService(PersonRepository personRepository) {
        this.personRepository = (personRepository != null) ? personRepository : new PersonRepository();
    }

    // Add a new person
    public boolean addPerson(Person p) throws DuplicatePersonCodeException, InvalidPhoneNumberException {
        if (p == null || p.getCode() == null || p.getCode().trim().isEmpty()) {

            return false;
        }

        return personRepository.save(p); // throws DuplicatePersonCodeException
    }

    // Find a person by code
    public Person findPersonByCode(String code) throws PersonNotFoundException {
        return personRepository.findByCodeOrThrow(code); // throws PersonNotFoundException
    }

    // Update name and phone number of a person
    public boolean updatePerson(String code, String newName, String newPhone)
            throws PersonNotFoundException, InvalidPhoneNumberException {
        Person p = findPersonByCode(code); // throws PersonNotFoundException
        String name = newName == null || newName.trim().isEmpty() ? p.getName() : newName;
        p.updateContactDetails(name, newPhone);
        return true;
    }

    // Delete a person by code
    public boolean deletePerson(String code) throws PersonNotFoundException {
        return personRepository.deleteByCode(code);
    }

    // Get all persons
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    public boolean loadFromCsv() {
        return loadFromCsv("data/persons.csv");
    }

    public boolean loadFromCsv(String filePath) {
        return personRepository.loadFromCsv(filePath);
    }

    public boolean saveToCsv() {
        return saveToCsv("data/persons.csv");
    }

    public boolean saveToCsv(String filePath) {
        return personRepository.saveToCsv(filePath);
    }
}
