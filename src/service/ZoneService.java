package service;

import exception.DuplicateZoneIdException;
import exception.RestrictedZoneViolationException;
import exception.ZoneNotFoundException;
import model.Person;
import model.RestrictedZone;
import model.Zone;
import repository.ZoneRepository;
import java.util.List;

/**
 * TẦNG NGHIỆP VỤ (SERVICE LAYER)
 * PHỤ TRÁCH: Thành viên 3 (Lê Tấn Thiên - SE201852)
 * MÔ TẢ: Xử lý các quy tắc nghiệp vụ phân vùng, kiểm soát quyền truy cập kết hợp Exception Handling.
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

    public boolean addZone(Zone z) throws DuplicateZoneIdException {
        if (z == null || z.getZoneId() == null || z.getZoneId().trim().isEmpty()) {
            System.out.println("Error: Invalid zone data!");
            return false;
        }
        if (zoneRepository.save(z)) {
            System.out.println("Successfully added zone: " + z.getZoneName());
            return true;
        }
        return false;
    }

    public Zone findZoneById(String zoneId) throws ZoneNotFoundException {
        Zone zone = zoneRepository.findById(zoneId);
        if (zone == null) {
            throw new ZoneNotFoundException(zoneId);
        }
        return zone;
    }

    public boolean grantZoneAccess(String zoneId, String personCode) throws ZoneNotFoundException {
        Zone zone = findZoneById(zoneId);
        if (zone instanceof RestrictedZone) {
            ((RestrictedZone) zone).grantAccess(personCode);
            return true;
        }
        return false;
    }

    public boolean revokeZoneAccess(String zoneId, String personCode) throws ZoneNotFoundException {
        Zone zone = findZoneById(zoneId);
        if (zone instanceof RestrictedZone) {
            ((RestrictedZone) zone).revokeAccess(personCode);
            return true;
        }
        return false;
    }

    public boolean verifyAccess(String zoneId, Person person) throws ZoneNotFoundException, RestrictedZoneViolationException {
        Zone zone = findZoneById(zoneId);
        if (person == null) {
            return false;
        }

        // Gọi phương thức checkAccess đã cài đặt đa hình ở model
        boolean hasAccess = zone.checkAccess(person);
        if (!hasAccess) {
            throw new RestrictedZoneViolationException(person.getCode(), zoneId, "Nhân sự không đủ quyền hoặc không có chứng chỉ an toàn hợp lệ.");
        }
        return hasAccess;
    }

    public boolean loadFromCsv() {
        return loadFromCsv("data/zones.csv");
    }

    public boolean loadFromCsv(String filePath) {
        return zoneRepository.loadFromCsv(filePath);
    }

    public boolean saveToCsv() {
        return saveToCsv("data/zones.csv");
    }

    public boolean saveToCsv(String filePath) {
        return zoneRepository.saveToCsv(filePath);
    }
}
