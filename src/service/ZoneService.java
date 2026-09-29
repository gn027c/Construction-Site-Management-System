package service;

import model.Person;
import model.RestrictedZone;
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
        Zone z = findZoneById(zoneId);
        if (z == null) {
            System.out.println("Error: Zone ID '" + zoneId + "' not found!");
            return false;
        }
        if (z instanceof RestrictedZone) {
            RestrictedZone rZone = (RestrictedZone) z;
            rZone.grantAccess(personCode);
            System.out.println("Granted access to person '" + personCode + "' for restricted zone: " + rZone.getZoneName());
            return true;
        } else {
            System.out.println("Notice: Zone '" + z.getZoneName() + "' is a general zone. Access control is not required!");
            return false;
        }
    }

    public boolean revokeZoneAccess(String zoneId, String personCode) {
        Zone z = findZoneById(zoneId);
        if (z == null) {
            System.out.println("Error: Zone ID '" + zoneId + "' not found!");
            return false;
        }
        if (z instanceof RestrictedZone) {
            RestrictedZone rZone = (RestrictedZone) z;
            rZone.revokeAccess(personCode);
            System.out.println("Revoked access of person '" + personCode + "' from restricted zone: " + rZone.getZoneName());
            return true;
        } else {
            System.out.println("Notice: Zone '" + z.getZoneName() + "' is not a restricted zone!");
            return false;
        }
    }

    public boolean verifyAccess(String zoneId, Person person) {
        Zone z = findZoneById(zoneId);
        if (z == null) {
            System.out.println("Error: Zone ID '" + zoneId + "' does not exist!");
            return false;
        }
        if (person == null) {
            return false;
        }
        String personCode = person.getCode();
        boolean hasAccess = z.checkAccess(person);
        if (hasAccess) {
            System.out.println("ACCESS GRANTED: Person '" + personCode + "' is permitted to enter zone: " + z.getZoneName());
        } else {
            System.out.println("ACCESS DENIED: Person '" + personCode + "' DOES NOT have permission to enter restricted zone: " + z.getZoneName());
        }
        return hasAccess;
    }
}
