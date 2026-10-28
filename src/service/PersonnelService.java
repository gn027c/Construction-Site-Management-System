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
    // Phone number: 10 digits, starts with 0
    private static final String PHONE_REGEX = "0\\d{9}";

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

        checkPhone(p.getPhoneNumber()); // throws InvalidPhoneNumberException
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
        checkPhone(newPhone); // throws InvalidPhoneNumberException

        if (newName != null && !newName.trim().isEmpty()) {
            p.setName(newName.trim());
        }
        p.setPhoneNumber(newPhone.trim());
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

    // Throw InvalidPhoneNumberException if the phone number is not valid
    private void checkPhone(String phone) throws InvalidPhoneNumberException {
        if (phone == null || !phone.trim().matches(PHONE_REGEX)) {
            throw new InvalidPhoneNumberException(phone);
        }
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
