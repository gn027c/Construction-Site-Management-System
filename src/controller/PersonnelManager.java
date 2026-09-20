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

    /**
     * Thêm mới nhân sự vào hệ thống.
     * YÊU CẦU LOGIC (Tuấn tự viết):
     * - Kiểm tra p != null.
     * - Kiểm tra trùng mã: Nếu mã p.getCode() đã tồn tại trong danh sách thì báo lỗi/trả về false.
     * - Ngược lại thêm vào personList và trả về true.
     */
    public boolean addPerson(Person p) {
        // TODO: [Tuấn] Cài đặt thuật toán kiểm tra trùng mã và thêm nhân sự tại đây
        return false;
    }

    /**
     * Tìm kiếm hồ sơ nhân sự theo mã (Sequential Search O(N)).
     * YÊU CẦU LOGIC (Tuấn tự viết):
     * - Duyệt qua danh sách personList.
     * - So khớp mã code không phân biệt hoa thường.
     * - Trả về đối tượng Person tìm thấy, hoặc null nếu không tồn tại.
     */
    public Person findPersonByCode(String code) {
        // TODO: [Tuấn] Cài đặt thuật toán tìm kiếm nhân sự theo mã tại đây
        return null;
    }

    /**
     * Cập nhật thông tin họ tên và số điện thoại của nhân sự.
     * YÊU CẦU LOGIC (Tuấn tự viết):
     * - Tìm nhân sự theo mã.
     * - Nếu tìm thấy, cập nhật newName và newPhone hợp lệ.
     */
    public boolean updatePerson(String code, String newName, String newPhone) {
        // TODO: [Tuấn] Cài đặt thuật toán cập nhật thông tin nhân sự tại đây
        return false;
    }

    /**
     * Xóa hồ sơ nhân sự theo mã.
     * YÊU CẦU LOGIC (Tuấn tự viết):
     * - Tìm và xóa nhân sự khỏi danh sách.
     */
    public boolean deletePerson(String code) {
        // TODO: [Tuấn] Cài đặt thuật toán xóa nhân sự tại đây
        return false;
    }

    /**
     * Trả về danh sách toàn bộ nhân sự trên công trường.
     */
    public List<Person> getAllPersons() {
        // TODO: [Tuấn] Trả về bản sao danh sách nhân sự
        return new ArrayList<>(personList);
    }
}
