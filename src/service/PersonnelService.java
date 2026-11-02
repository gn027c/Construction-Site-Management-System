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

    public boolean updateProfile(
            String code, String name, String phone, String[] details, model.common.SafetyLevel level
    ) throws PersonNotFoundException {
        findPersonByCode(code).updateProfile(name, phone, details, level);
        return true;
    }

    public Person changeType(String code, Person replacement) throws PersonNotFoundException {
        Person existing = findPersonByCode(code);
        if (replacement == null || !replacement.getCode().equals(existing.getCode())) {
            throw new exception.DataValidationException("code", "A type change must keep the person code.");
        }
        if (replacement.getRole() == existing.getRole()) {
            throw new exception.DataValidationException(
                    "type", "Choose a type different from " + existing.getRole() + "."
            );
        }
        if (!replacement.shouldProvisionAccount()) {
            throw new exception.DataValidationException(
                    "type", "A person cannot become a visitor; deactivate the record instead."
            );
        }
        replacement.setActive(existing.isActive());
        personRepository.replace(replacement);
        return existing;
    }

    public void restorePerson(Person original) throws PersonNotFoundException {
        personRepository.replace(original);
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
