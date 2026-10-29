package service;

import exception.DuplicatePersonCodeException;
import exception.InvalidPhoneNumberException;
import exception.PersonNotFoundException;
import model.Person;
import repository.PersonRepository;
import java.util.List;

/**
 * Coordinates personnel registration, lookup and contact updates.
 *
 * <p>The repository enforces unique personnel codes. Models validate their
 * fields and contact changes. This service performs no console input or output.
 *
 * <p>Responsibility: Tran Ngoc Anh Tuan (SE201513) - personnel operations.
 */
public class PersonnelService {
    private final PersonRepository personRepository;

    public PersonnelService() {
        this.personRepository = new PersonRepository();
    }

    public PersonnelService(PersonRepository personRepository) {
        this.personRepository = (personRepository != null) ? personRepository : new PersonRepository();
    }

    public boolean addPerson(Person p) throws DuplicatePersonCodeException, InvalidPhoneNumberException {
        if (p == null || p.getCode() == null || p.getCode().trim().isEmpty()) {
            return false;
        }
        return personRepository.save(p);
    }

    public Person findPersonByCode(String code) throws PersonNotFoundException {
        return personRepository.findByCodeOrThrow(code);
    }

    /**
     * Updates contact details through the model's validated operation.
     *
     * @param code the existing personnel code
     * @param newName the new name; null or blank retains the current name
     * @param newPhone the new phone number, which must satisfy the model's rule
     * @return true after both contact values are accepted
     * @throws PersonNotFoundException if the person is absent
     * @throws InvalidPhoneNumberException if the new phone number is invalid
     */
    public boolean updatePerson(String code, String newName, String newPhone)
            throws PersonNotFoundException, InvalidPhoneNumberException {
        Person p = findPersonByCode(code);
        String name = newName == null || newName.trim().isEmpty() ? p.getName() : newName;
        p.updateContactDetails(name, newPhone);
        return true;
    }

    public boolean deletePerson(String code) throws PersonNotFoundException {
        return personRepository.deleteByCode(code);
    }

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
