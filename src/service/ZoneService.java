package service;

import exception.AppError;
import exception.NotFoundException;
import model.Person;
import model.RestrictedZone;
import model.Zone;
import model.SafetyLevel;
import repository.ZoneRepository;
import java.util.List;

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

    public boolean addZone(Zone z) throws AppError {
        return zoneRepository.save(z);
    }

    public void updateZone(String zoneId, String newName, SafetyLevel requiredSafetyLevel) {
        Zone zone = findZoneById(zoneId);
        if (zone instanceof RestrictedZone) {
            if (requiredSafetyLevel == null || requiredSafetyLevel == SafetyLevel.NONE) {
                throw new exception.DataValidationException(
                        "requiredSafetyLevel", "Choose LOW, MEDIUM, HIGH or CRITICAL.");
            }
            zone.rename(newName);
            ((RestrictedZone) zone).changeRequiredSafetyLevel(requiredSafetyLevel);
        } else {
            if (requiredSafetyLevel != null) {
            throw new AppError("A regular zone cannot have a safety level.");
            }
            zone.rename(newName);
        }
    }

    boolean deleteZone(String zoneId) {
        return zoneRepository.deleteById(zoneId);
    }

    public Zone findZoneById(String zoneId) throws NotFoundException {
        return zoneRepository.findByIdOrThrow(zoneId);
    }

    private static exception.AppError accessDenied(String personCode, String zoneId, String reason) {
        return new exception.AppError(
                "Access Denied: Person [" + personCode + "] is not permitted to enter Restricted Zone [" + zoneId
                        + "]. Reason: " + reason
        );
    }

    public boolean grantZoneAccess(String zoneId, Person person) throws NotFoundException, AppError {
        Zone zone = findZoneById(zoneId);
        if (!(zone instanceof RestrictedZone)) {
            return false;
        }
        ((RestrictedZone) zone).grant(person);
        return true;
    }

    public boolean revokeZoneAccess(String zoneId, Person person) throws NotFoundException {
        Zone zone = findZoneById(zoneId);
        return zone instanceof RestrictedZone && ((RestrictedZone) zone).revoke(person);
    }

    public boolean isListed(String personCode) {
        for (Zone zone : zoneRepository.findAll()) {
            if (zone instanceof RestrictedZone
                    && ((RestrictedZone) zone).getAllowedPersonCodes().contains(personCode)) {
                return true;
            }
        }
        return false;
    }

    public void verifyAccess(String zoneId, Person person) throws NotFoundException, AppError {
        Zone zone = findZoneById(zoneId);

        String denialReason = person.canEnter(zone);
        if (denialReason != null) {
            throw accessDenied(person.getCode(), zoneId, denialReason);
        }
    }

    public boolean loadFromCsv(String filePath) {
        return zoneRepository.loadFromCsv(filePath);
    }

    public boolean saveToCsv(String filePath) {
        return zoneRepository.saveToCsv(filePath);
    }
}
