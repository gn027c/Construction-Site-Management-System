package service;

import exception.DuplicateZoneIdException;
import exception.RestrictedZoneViolationException;
import exception.ZoneNotFoundException;
import model.person.Person;
import model.zone.RestrictedZone;
import model.zone.Zone;
import repository.ZoneRepository;
import java.util.List;

/**
 * Coordinates zone storage, permission changes and access verification.
 *
 * <p>Access checks call the zone's polymorphic policy. Permission changes apply
 * only to restricted zones; personnel registry checks belong to the caller.
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
            return false;
        }
        if (zoneRepository.save(z)) {
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

    /**
     * Looks up the zone and evaluates its polymorphic access policy.
     */
    public boolean verifyAccess(String zoneId, Person person)
            throws ZoneNotFoundException, RestrictedZoneViolationException {
        Zone zone = findZoneById(zoneId);
        if (person == null) {
            return false;
        }
        boolean hasAccess = zone.checkAccess(person);
        if (!hasAccess) {
            throw new RestrictedZoneViolationException(
                    person.getCode(),
                    zoneId,
                    "Nhân sự không đủ quyền hoặc không có chứng chỉ an toàn hợp lệ."
            );
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
