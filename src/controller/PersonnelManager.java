package controller;

import model.Person;
import java.util.ArrayList;
import java.util.List;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Module quản lý nghiệp vụ hồ sơ nhân sự (Personnel Management).
 * - Chịu trách nhiệm thực hiện các thuật toán: Thêm mới, Tìm kiếm, Cập nhật, Xóa nhân sự.
 */
public class PersonnelManager {
    private final List<Person> personList = new ArrayList<>();

    public boolean addPerson(Person p) {
        return false;
    }

    public Person findPersonByCode(String code) {
        return null;
    }

    public boolean updatePerson(String code, String newName, String newPhone) {
        return false;
    }

    public boolean deletePerson(String code) {
        return false;
    }

    public List<Person> getAllPersons() {
        return new ArrayList<>(personList);
    }
}
