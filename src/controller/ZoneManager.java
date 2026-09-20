package controller;

import model.Person;
import model.Zone;
import java.util.ArrayList;
import java.util.List;

/**
 * NHIỆM VỤ: Thành viên 3 (Lê Tấn Thiên - SE201852)
 * MÔ TẢ:
 * - Module quản lý phân vùng và kiểm soát an toàn ra/vào cổng (Zone & Safety Access Control).
 * - Chịu trách nhiệm thực hiện các thuật toán: Quản lý khu vực, Cấp/Thu hồi quyền vào vùng hạn chế, Xác thực quyền ra/vào.
 */
public class ZoneManager {
    private final List<Zone> zoneList = new ArrayList<>();

    /**
     * Thêm khu vực mới vào hệ thống.
     * YÊU CẦU LOGIC (Thiên tự viết):
     * - Kiểm tra z != null.
     * - Kiểm tra trùng mã: Nếu z.getZoneId() đã tồn tại thì báo lỗi/trả về false.
     * - Ngược lại thêm vào zoneList và trả về true.
     */
    public boolean addZone(Zone z) {
        // TODO: [Thiên] Cài đặt thuật toán kiểm tra trùng mã và thêm khu vực tại đây
        return false;
    }

    /**
     * Tra cứu thông tin khu vực theo mã zoneId.
     */
    public Zone findZoneById(String zoneId) {
        // TODO: [Thiên] Cài đặt thuật toán tìm kiếm khu vực tại đây
        return null;
    }

    /**
     * Cấp quyền cho nhân sự vào khu vực nguy hiểm (RestrictedZone).
     * YÊU CẦU LOGIC (Thiên tự viết):
     * - Tìm khu vực theo zoneId, kiểm tra xem có phải instance của RestrictedZone hay không.
     * - Thêm personCode vào danh sách được phép (allowedPersonCodes).
     */
    public boolean grantZoneAccess(String zoneId, String personCode) {
        // TODO: [Thiên] Cài đặt thuật toán cấp quyền vào khu vực hạn chế tại đây
        return false;
    }

    /**
     * Thu hồi quyền vào khu vực nguy hiểm của nhân sự.
     */
    public boolean revokeZoneAccess(String zoneId, String personCode) {
        // TODO: [Thiên] Cài đặt thuật toán thu hồi quyền tại đây
        return false;
    }

    /**
     * Xác thực quyền ra/vào khu vực của một nhân sự.
     * YÊU CẦU LOGIC (Thiên tự viết):
     * - Gọi hàm đa hình z.checkAccess(person).
     */
    public boolean verifyAccess(String zoneId, Person person) {
        // TODO: [Thiên] Cài đặt thuật toán kiểm tra đa hình quyền truy cập tại đây
        return false;
    }

    /**
     * Trả về danh sách tất cả các khu vực.
     */
    public List<Zone> getAllZones() {
        return new ArrayList<>(zoneList);
    }
}
