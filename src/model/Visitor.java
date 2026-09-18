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

   // ==================== THUỘC TÍNH RIÊNG ====================
    private String mucDichTham;        // Ví dụ: Tham quan, Thanh tra, Kiểm tra tiến độ...
    private String nguoiBaoLanhTiepDoan; // Người/bộ phận chịu trách nhiệm tiếp đón
    private String ngayDenTham;        // Ngày khách đến công trường
    private boolean daDuyet;           // Đoàn/khách đã được duyệt vào công trường hay chưa

  // ==================== KHỞI TẠO ====================
public Visitor() {
        super();
    }

public Visitor(String maNguoi, String hoTen, String ngaySinh, String gioiTinh,
                    String soDienThoai, String mucDichTham, String nguoiBaoLanhTiepDoan,
                    String ngayDenTham, boolean daDuyet) {
        super(maNguoi, hoTen, ngaySinh, gioiTinh, soDienThoai);
        this.mucDichTham = mucDichTham;
        this.nguoiBaoLanhTiepDoan = nguoiBaoLanhTiepDoan;
        this.ngayDenTham = ngayDenTham;
        this.daDuyet = daDuyet;
    }


    public String getMucDichTham() { return mucDichTham; }
    public void setMucDichTham(String mucDichTham) { this.mucDichTham = mucDichTham; }
 
    public String getNguoiBaoLanhTiepDoan() { return nguoiBaoLanhTiepDoan; }
    public void setNguoiBaoLanhTiepDoan(String nguoiBaoLanhTiepDoan) { this.nguoiBaoLanhTiepDoan = nguoiBaoLanhTiepDoan; }
 
    public String getNgayDenTham() { return ngayDenTham; }
    public void setNgayDenTham(String ngayDenTham) { this.ngayDenTham = ngayDenTham; }
 
    public boolean isDaDuyet() { return daDuyet; }
    public void setDaDuyet(boolean daDuyet) { this.daDuyet = daDuyet; }

    @Override
    public void hienThiThongTin() {
        System.out.println("===== THÔNG TIN KHÁCH THAM QUAN / ĐOÀN THANH TRA =====");
        System.out.println("Mã: " + getMaNguoi());
        System.out.println("Họ tên: " + getHoTen());
        System.out.println("Ngày sinh: " + getNgaySinh());
        System.out.println("Giới tính: " + getGioiTinh());
        System.out.println("Số điện thoại: " + getSoDienThoai());
        System.out.println("Mục đích thăm: " + mucDichTham);
        System.out.println("Người bảo lãnh tiếp đoàn: " + nguoiBaoLanhTiepDoan);
        System.out.println("Ngày đến thăm: " + ngayDenTham);
        System.out.println("Tình trạng duyệt: " + (daDuyet ? "Đã duyệt" : "Chưa duyệt"));
        System.out.println("========================================================");
    }
    @Override
    public List<String> chuanBiDuLieuXuatBaoCao() {
        List<String> row = new ArrayList<>();
        row.add(getMaNguoi());
        row.add(getHoTen());
        row.add(getNgaySinh());
        row.add(getGioiTinh());
        row.add(getSoDienThoai());
        row.add(mucDichTham);
        row.add(nguoiBaoLanhTiepDoan);
        row.add(ngayDenTham);
        row.add(daDuyet ? "Đã duyệt" : "Chưa duyệt");
        return row;
    }
 
    @Override
    public String toString() {
        return "Visitor{" +
                "maNguoi='" + getMaNguoi() + '\'' +
                ", hoTen='" + getHoTen() + '\'' +
                ", mucDichTham='" + mucDichTham + '\'' +
                ", nguoiBaoLanhTiepDoan='" + nguoiBaoLanhTiepDoan + '\'' +
                ", ngayDenTham='" + ngayDenTham + '\'' +
                ", daDuyet=" + daDuyet +
                '}';
    }
  
}
