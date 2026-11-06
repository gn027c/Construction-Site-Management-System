package repository;

import exception.AppError;
import exception.DataValidationException;
import exception.NotFoundException;
import model.RestrictedZone;
import model.SafetyLevel;
import model.Zone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ZoneRepository {
    private static final String HEADER = "type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes";
    private static final int COLUMN_COUNT = 5;

    private final List<Zone> zoneList = new ArrayList<>();

    public boolean save(Zone zone) throws AppError {
        if (zone == null || zone.getZoneId() == null || zone.getZoneId().trim().isEmpty()) {
            return false;
        }
        if (findById(zone.getZoneId()) != null) {
            throw new exception.AppError("Zone ID already exists in the system: " + zone.getZoneId());
        }
        return zoneList.add(zone);
    }

    public Zone findById(String zoneId) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            return null;
        }
        for (Zone z : zoneList) {
            if (Zone.isSameId(z.getZoneId(), zoneId)) {
                return z;
            }
        }
        return null;
    }

    public Zone findByIdOrThrow(String zoneId) throws NotFoundException {
        Zone z = findById(zoneId);
        if (z == null) {
            throw new exception.NotFoundException("Construction zone", "ID", zoneId);
        }
        return z;
    }

    public List<Zone> findAll() {
        return new ArrayList<>(zoneList);
    }

    public boolean deleteById(String zoneId) throws NotFoundException {
        Zone zone = findByIdOrThrow(zoneId);
        return zoneList.remove(zone);
    }

    public boolean loadFromCsv(String filePath) {
        List<Zone> zones = util.CsvHelper.readTable(filePath, HEADER, COLUMN_COUNT, this::parse, Zone::getZoneId);
        if (zones == null) {
            return false;
        }
        zoneList.clear();
        zoneList.addAll(zones);
        return true;
    }

    private Zone parse(List<String> columns) {
        String type = columns.get(0).trim();
        String zoneId = columns.get(1).trim();
        String zoneName = columns.get(2).trim();
        if (type.equals("REGULAR")) {
            if (!columns.get(3).trim().equals("NONE") || !columns.get(4).trim().equals("NONE")) {
                throw new DataValidationException("A regular zone has no safety level or access list.");
            }
            return new Zone(zoneId, zoneName);
        }
        if (!type.equals("RESTRICTED")) {
            throw new DataValidationException("Unknown zone type " + type + ".");
        }
        Set<String> allowedCodes = new HashSet<>();
        String codes = columns.get(4).trim();
        if (!codes.equals("NONE") && !codes.isEmpty()) {
            for (String code : codes.split(";")) {
                if (!code.trim().isEmpty()) {
                    allowedCodes.add(code.trim());
                }
            }
        }
        return new RestrictedZone(zoneId, zoneName, SafetyLevel.valueOf(columns.get(3).trim()), allowedCodes);
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Zone zone : zoneList) {
            lines.add(zone.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }

}
