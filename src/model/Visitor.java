package model;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Khách tham quan hoặc đoàn thanh tra vãng lai.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Chịu trách nhiệm quản lý thông tin mục đích thăm, người bảo lãnh tiếp đoàn và ngày đến thăm.
 * - Cài đặt các hành vi hiển thị chi tiết và chuẩn bị dữ liệu xuất báo cáo theo yêu cầu Workshop 1.
 */
public class Visitor extends Person {

    // ==================== OWN ATTRIBUTES ====================
    private String visitPurpose;   // e.g. Site tour, Inspection, Progress check...
    private String hostGuarantor;  // Person/department responsible for hosting the visit
    private String visitDate;      // Date the visitor arrives at the site
    private boolean approved;      // Whether the visitor/team has been approved to enter the site
 
    // ==================== CONSTRUCTORS ====================
 
   
    public Visitor() {
        super();
    }
 
    public Visitor(String id, String fullName, String dateOfBirth, String gender,
                    String phoneNumber, String visitPurpose, String hostGuarantor,
                    String visitDate, boolean approved) {
        super(id, fullName, dateOfBirth, gender, phoneNumber);
        this.visitPurpose = visitPurpose;
        this.hostGuarantor = hostGuarantor;
        this.visitDate = visitDate;
        this.approved = approved;
    }
 
    // ==================== GETTERS / SETTERS ====================
    public String getVisitPurpose() { return visitPurpose; }
    public void setVisitPurpose(String visitPurpose) { this.visitPurpose = visitPurpose; }
 
    public String getHostGuarantor() { return hostGuarantor; }
    public void setHostGuarantor(String hostGuarantor) { this.hostGuarantor = hostGuarantor; }
 
    public String getVisitDate() { return visitDate; }
    public void setVisitDate(String visitDate) { this.visitDate = visitDate; }
 
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
 
    // ==================== BUSINESS BEHAVIOR ====================
 

    @Override
    public void displayInfo() {
        System.out.println("===== VISITOR / INSPECTION TEAM INFORMATION =====");
        System.out.println("ID: " + getMaNguoi());
        System.out.println("Full name: " + getHoTen());
        System.out.println("Date of birth: " + getNgaySinh());
        System.out.println("Gender: " + getGioiTinh());
        System.out.println("Phone number: " + getSoDienThoai());
        System.out.println("Visit purpose: " + visitPurpose);
        System.out.println("Host/Guarantor: " + hostGuarantor);
        System.out.println("Visit date: " + visitDate);
        System.out.println("Approval status: " + (approved ? "Approved" : "Not approved"));
        System.out.println("==================================================");
    }
 
   
    @Override
    public List<String> prepareReportData() {
        List<String> row = new ArrayList<>();
        row.add(getMaNguoi());
        row.add(getHoTen());
        row.add(getNgaySinh());
        row.add(getGioiTinh());
        row.add(getSoDienThoai());
        row.add(visitPurpose);
        row.add(hostGuarantor);
        row.add(visitDate);
        row.add(approved ? "Approved" : "Not approved");
        return row;
    }
 
    @Override
    public String toString() {
        return "Visitor{" +
                "id='" + getMaNguoi() + '\'' +
                ", fullName='" + getHoTen() + '\'' +
                ", visitPurpose='" + visitPurpose + '\'' +
                ", hostGuarantor='" + hostGuarantor + '\'' +
                ", visitDate='" + visitDate + '\'' +
                ", approved=" + approved +
                '}';
    }
}
