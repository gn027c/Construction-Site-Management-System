package repository;

import exception.DuplicatePersonCodeException;
import exception.PersonNotFoundException;
import model.Person;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TẦNG LƯU TRỮ (REPOSITORY LAYER)
 * PHỤ TRÁCH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ: Quản lý lưu trữ và truy xuất tập hợp đối tượng Person trong bộ nhớ bằng LinkedHashMap O(1).
 */
public class PersonRepository {
    private final Map<String, Person> store = new LinkedHashMap<>();

    /** Saves a person; throws DuplicatePersonCodeException if the code already exists. */
    public boolean save(Person person) throws DuplicatePersonCodeException {
        if (person == null || person.getCode() == null || person.getCode().trim().isEmpty()) {
            return false;
        }
        String code = person.getCode().trim();
        if (store.containsKey(code)) {
            throw new DuplicatePersonCodeException(code);
        }
        store.put(code, person);
        return true;
    }

    /** Looks up a person by code; returns null if not found. */
    public Person findByCode(String code) {
        if (code == null) {
            return null;
        }
        return store.get(code.trim());
    }

    /** Looks up a person by code; throws PersonNotFoundException if not found. */
    public Person findByCodeOrThrow(String code) throws PersonNotFoundException {
        Person person = findByCode(code);
        if (person == null) {
            throw new PersonNotFoundException(code != null ? code.trim() : "null");
        }
        return person;
    }

    public boolean deleteByCode(String code) throws PersonNotFoundException {
        Person p = findByCodeOrThrow(code);
        return store.remove(p.getCode()) != null;
    }

    public List<Person> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean loadFromCsv(String filePath) {
        // TODO: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513) cài đặt đọc dữ liệu từ persons.csv vào store (Workshop 5)
        return false;
    }

    public boolean saveToCsv(String filePath) {
        // TODO: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513) cài đặt ghi dữ liệu store ra persons.csv với UTF-8 BOM (Workshop 5)
        return false;
    }
}
