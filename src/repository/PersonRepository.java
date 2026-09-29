package repository;

import model.Person;
import java.util.ArrayList;
import java.util.List;

/**
 * TẦNG LƯU TRỮ (REPOSITORY LAYER)
 * PHỤ TRÁCH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ: Quản lý lưu trữ và truy xuất tập hợp đối tượng Person trong bộ nhớ.
 */
public class PersonRepository {
    private final List<Person> personList = new ArrayList<>();

    public boolean save(Person person) {
        if (person == null || person.getCode() == null) {
            return false;
        }
        if (findByCode(person.getCode()) != null) {
            return false;
        }
        return personList.add(person);
    }

    public Person findByCode(String code) {
        if (code == null) {
            return null;
        }
        for (Person p : personList) {
            if (p.getCode().equalsIgnoreCase(code.trim())) {
                return p;
            }
        }
        return null;
    }

    public boolean deleteByCode(String code) {
        Person p = findByCode(code);
        if (p == null) {
            return false;
        }
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
