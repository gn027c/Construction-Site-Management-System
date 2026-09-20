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

    public boolean addZone(Zone z) {
        return false;
    }

    public Zone findZoneById(String zoneId) {
        return null;
    }

    public boolean grantZoneAccess(String zoneId, String personCode) {
        return false;
    }

    public boolean revokeZoneAccess(String zoneId, String personCode) {
        return false;
    }

    public boolean verifyAccess(String zoneId, Person person) {
        return false;
    }

    public List<Zone> getAllZones() {
        return new ArrayList<>(zoneList);
    }
}
