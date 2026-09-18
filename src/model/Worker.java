package model;

/**
 * NHIỆM VỤ: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ:
 * - Lớp đại diện cho đối tượng Công nhân thi công trên công trường.
 * - Kế thừa từ lớp trừu tượng Person.
 * - Chịu trách nhiệm quản lý thông tin ngành nghề, chứng chỉ an toàn và tổ đội thi công.
 * - Cài đặt các hành vi hiển thị chi tiết và chuẩn bị dữ liệu xuất báo cáo theo yêu cầu Workshop 1.
 */
public class Worker extends Person {
      private String maNguoi;
    private String hoTen;
    private String ngaySinh;
    private String gioiTinh;
    private String soDienThoai;
 
    public Person() {
      
    }
 
    public Person(String maNguoi, String hoTen, String ngaySinh, String gioiTinh, String soDienThoai) {
        this.maNguoi = maNguoi;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.soDienThoai = soDienThoai;
    }
 
    public String getMaNguoi() { return maNguoi; }
    public void setMaNguoi(String maNguoi) { this.maNguoi = maNguoi; }
 
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
 
    public String getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(String ngaySinh) { this.ngaySinh = ngaySinh; }
 
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
 
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
 
    
    public abstract void hienThiThongTin();
 
  
    public abstract List<String> chuanBiDuLieuXuatBaoCao();
}
