package service;

import model.Person;
import model.Zone;
import repository.ZoneRepository;
import java.util.List;

/**
 * TẦNG NGHIỆP VỤ (SERVICE LAYER)
 * PHỤ TRÁCH: Thành viên 3 (Lê Tấn Thiên - SE201852)
 * MÔ TẢ: Xử lý các quy tắc nghiệp vụ phân vùng và kiểm soát quyền truy cập an toàn.
 */
public class ZoneService {
    private final ZoneRepository zoneRepository;

    public ZoneService() {
        this.zoneRepository = new ZoneRepository();
    }

    public ZoneService(ZoneRepository zoneRepository) {
        this.zoneRepository = (zoneRepository != null) ? zoneRepository : new ZoneRepository();
    }

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public boolean addZone(Zone z) {
        if (z == null || z.getZoneId() == null || z.getZoneId().trim().isEmpty()) {
            System.out.println("Error: Invalid zone data!");
            return false;
        }
        if (zoneRepository.save(z)) {
            System.out.println("Successfully added zone: " + z.getZoneName());
            return true;
        } else {
            System.out.println("Error: Zone ID '" + z.getZoneId() + "' already exists!");
            return false;
        }
    }

    public Zone findZoneById(String zoneId) {
        return zoneRepository.findById(zoneId);
    }

    public boolean grantZoneAccess(String zoneId, String personCode) {
        // TODO: Thành viên 3 (Lê Tấn Thiên - SE201852) cài đặt cấp quyền vào RestrictedZone
        return false;
    }

    public boolean revokeZoneAccess(String zoneId, String personCode) {
        // TODO: Thành viên 3 (Lê Tấn Thiên - SE201852) cài đặt thu hồi quyền khỏi RestrictedZone
        return false;
    }

    public boolean verifyAccess(String zoneId, Person person) {
        // TODO: Thành viên 3 (Lê Tấn Thiên - SE201852) cài đặt kiểm tra quyền truy cập đa hình qua z.checkAccess(person)
        return false;
    }
}
