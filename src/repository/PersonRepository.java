package repository;

import exception.DuplicatePersonCodeException;
import exception.PersonNotFoundException;
import model.Person;
import java.util.ArrayList;
import java.util.List;

/**
 * TẦNG LƯU TRỮ (REPOSITORY LAYER)
 * PHỤ TRÁCH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ: Quản lý lưu trữ và truy xuất tập hợp đối tượng Person trong bộ nhớ có xử lý Exception.
 */
public class PersonRepository {
    private final Map<String, Person> store = new LinkedHashMap<>();
 
    /** Saves a person; throws DuplicatePersonCodeException if the code already exists. */
    public void save(Person person) {
        if (store.containsKey(person.getCode())) {
            throw new DuplicatePersonCodeException(person.getCode());
        }
        store.put(person.getCode(), person);
    }

    /** Looks up a person by code; throws PersonNotFoundException if not found. */
    public Person findByCode(String code) {
        Person person = store.get(code);
        if (person == null) {
            throw new PersonNotFoundException(code);
        }
        return person;
    }

    public boolean deleteByCode(String code) throws PersonNotFoundException {
        Person p = findByCodeOrThrow(code);
        return personList.remove(p);
    }

    public List<Person> findAll() {
        return new ArrayList<>(personList);
    }

    public boolean loadFromCsv(String filePath) {
        // TODO: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513) cài đặt đọc dữ liệu từ persons.csv vào personList (Workshop 5)
        return false;
    }

    public boolean saveToCsv(String filePath) {
        // TODO: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513) cài đặt ghi dữ liệu personList ra persons.csv với UTF-8 BOM (Workshop 5)
        return false;
    }
}
