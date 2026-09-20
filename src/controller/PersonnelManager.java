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
        if (p == null || p.getId() == null) {
            return false;
        }
        if (findPersonByCode(p.getId()) != null) {
            // A person with this code already exists -> reject duplicate
            return false;
        }
        return personList.add(p);
    }
 
  
    public Person findPersonByCode(String code) {
        if (code == null) {
            return null;
        }
        for (Person p : personList) {
            if (code.equals(p.getId())) {
                return p;
            }
        }
        return null;
    }
 
    
    public boolean updatePerson(String code, String newName, String newPhone) {
        Person p = findPersonByCode(code);
        if (p == null) {
            return false;
        }
        p.setFullName(newName);
        p.setPhoneNumber(newPhone);
        return true;
    }
 
    
    public boolean deletePerson(String code) {
        Person p = findPersonByCode(code);
        if (p == null) {
            return false;
        }
        return personList.remove(p);
    }
 
    public List<Person> getAllPersons() {
        return new ArrayList<>(personList);
    }
}
