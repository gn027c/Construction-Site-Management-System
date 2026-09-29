package repository;

import model.Zone;
import java.util.ArrayList;
import java.util.List;

/**
 * TẦNG LƯU TRỮ (REPOSITORY LAYER)
 * PHỤ TRÁCH: Thành viên 3 (Lê Tấn Thiên - SE201852)
 * MÔ TẢ: Quản lý lưu trữ và truy xuất tập hợp đối tượng Zone trong bộ nhớ.
 */
public class ZoneRepository {
    private final List<Zone> zoneList = new ArrayList<>();

    public boolean save(Zone zone) {
        if (zone == null || zone.getZoneId() == null || zone.getZoneId().trim().isEmpty()) {
            return false;
        }
        if (findById(zone.getZoneId()) != null) {
            return false;
        }
        return zoneList.add(zone);
    }

    public Zone findById(String zoneId) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            return null;
        }
        for (Zone z : zoneList) {
            if (z.getZoneId().equalsIgnoreCase(zoneId.trim())) {
                return z;
            }
        }
        return null;
    }

    public List<Zone> findAll() {
        return new ArrayList<>(zoneList);
    }
}
