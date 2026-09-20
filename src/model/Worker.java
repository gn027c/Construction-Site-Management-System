package model;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Công nhân thi công trên công trường.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Chịu trách nhiệm quản lý thông tin ngành nghề, chứng chỉ an toàn và tổ đội thi công.
 * - Cài đặt các hành vi hiển thị chi tiết và chuẩn bị dữ liệu xuất báo cáo theo yêu cầu Workshop 1.
 */
public class Worker {
      private String personId;
      private String fullName;
      private String dateOfBirth;
      private String gender;
      private String phoneNumber;

    public Worker() {
        this.maNguoi = "";
        this.hoTen = "";
        this.ngaySinh = "";
        this.gioiTinh = "";
        this.soDienThoai = "";
    }

    public Worker(String personId, String fullName, String dateOfBirth, String gender, String phoneNumber) {
        this.personId = personId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }


    /** Displays detailed information – each subclass defines its own display method. */
    public abstract void displayInfo();
 
    /** Prepares data (list of fields) for report generation. */
    public abstract List<String> prepareReportData();
}
