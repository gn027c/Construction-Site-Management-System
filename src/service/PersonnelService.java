package service;

import exception.DuplicatePersonCodeException;
import exception.InvalidPhoneNumberException;
import exception.PersonNotFoundException;
import model.person.Person;
import repository.PersonRepository;
import java.util.List;

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

    public boolean shouldProvisionAccount(Person p) {
        return p != null && p.shouldProvisionAccount();
    }

    public Person findPersonByCode(String code) throws PersonNotFoundException {
        return personRepository.findByCodeOrThrow(code);
    }

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
