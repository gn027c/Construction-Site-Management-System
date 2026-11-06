package service;

import exception.AppError;
import exception.DataValidationException;
import exception.NotFoundException;
import model.Person;
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

    public boolean addPerson(Person p) throws AppError, DataValidationException {
        util.ValidationHelper.requireNotNull(p, "person", "Must not be null.");
        return personRepository.save(p);
    }

    public Person findPersonByCode(String code) throws NotFoundException {
        return personRepository.findByCodeOrThrow(code);
    }

    public boolean updatePerson(String code, String newName, String newPhone)
            throws NotFoundException, DataValidationException {
        findPersonByCode(code).updateContactDetails(newName, newPhone);
        return true;
    }

    public void updateProfile(String code, String... values) {
        findPersonByCode(code).updateProfile(values);
    }

    public void setActive(String code, boolean active) {
        Person person = findPersonByCode(code);
        if (active) {
            person.activate();
        } else {
            person.deactivate();
        }
    }

    public void setSafetyLevel(String code, model.SafetyLevel level) {
        findPersonByCode(code).setSafetyLevel(level);
    }

    public void setVisitDate(String code, String date) {
        findPersonByCode(code).changeVisitDate(date);
    }

    // Đặt default để bên controllere không thể gọi bừa, mà phải dùng bên PersonReemovalService.
    boolean deletePerson(String code) throws NotFoundException {
        return personRepository.deleteByCode(code);
    }

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    public boolean loadFromCsv(String filePath) {
        return personRepository.loadFromCsv(filePath);
    }

    public boolean saveToCsv(String filePath) {
        return personRepository.saveToCsv(filePath);
    }
}
