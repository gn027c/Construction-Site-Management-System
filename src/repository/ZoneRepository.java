package repository;

import exception.DuplicateZoneIdException;
import exception.ZoneNotFoundException;
import model.RestrictedZone;
import model.Zone;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores regular and restricted zones and handles zone CSV.
 *
 * <p>Zone identifiers are compared without case. CSV loading validates a separate
 * collection before replacing active data. Restricted-zone permissions use
 * semicolon-separated codes; {@code NONE} represents an empty list.
 */
public class ZoneRepository {
    private final List<Zone> zoneList = new ArrayList<>();

    public boolean save(Zone zone) throws DuplicateZoneIdException {
        if (zone == null || zone.getZoneId() == null || zone.getZoneId().trim().isEmpty()) {
            return false;
        }
        if (findById(zone.getZoneId()) != null) {
            throw new DuplicateZoneIdException(zone.getZoneId());
        }
        return zoneList.add(zone);
    }

    public Zone findById(String zoneId) {
        String normalized = util.IdentifierHelper.normalizeForLookup(zoneId);
        if (normalized == null) {
            return null;
        }
        for (Zone z : zoneList) {
            if (z.getZoneId().equals(normalized)) {
                return z;
            }
        }
        return null;
    }

    public Zone findByIdOrThrow(String zoneId) throws ZoneNotFoundException {
        Zone z = findById(zoneId);
        if (z == null) {
            throw new ZoneNotFoundException(zoneId);
        }
        return z;
    }

    /**
     * Returns a separate list in storage order containing the stored model objects.
     *
     * <p>Changing the returned list does not change this repository's collection.
     * The model objects themselves are shared and retain their validated operations.
     */
    public List<Zone> findAll() {
        return new ArrayList<>(zoneList);
    }

    /**
     * Loads CSV into a temporary repository before replacing active data.
     * A read or validation failure returns false and retains the current collection.
     */
    public boolean loadFromCsv(String filePath) {
        ZoneRepository staged = new ZoneRepository();
        try {
            List<String[]> rows = util.CsvHelper.readRows(filePath);
            if (rows.isEmpty() || !Arrays.equals(rows.get(0), new String[] {
                    "type", "zoneId", "zoneName", "requiredSafetyLevel", "allowedPersonCodes"
            })) {
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                if (util.CsvHelper.isBlankRow(p)) {
                    continue;
                }
                if (p.length != 5) {
                    return false;
                }
                if (p[0].equals("REGULAR")) {
                    staged.save(new Zone(p[1], p[2]));
                } else if (p[0].equals("RESTRICTED")) {
                    Set<String> codes = new HashSet<>();
                    if (!p[4].equals("NONE") && !p[4].isEmpty()) {
                        for (String code : p[4].split(";", -1)) {
                            if (code.trim().isEmpty()) {
                                return false;
                            }
                            codes.add(code.trim());
                        }
                    }
                    staged.save(new RestrictedZone(p[1], p[2], RestrictedZone.SafetyLevel.valueOf(p[3]), codes));
                } else {
                    return false;
                }
            }
        } catch (RuntimeException e) {
            return false;
        }
        zoneList.clear();
        zoneList.addAll(staged.zoneList);
        return true;
    }

    /**
     * Serializes current data and requests an atomic replacement of one CSV file.
     */
    public boolean saveToCsv(String filePath) {
        List<String> rows = new ArrayList<>();
        rows.add("type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes");
        for (Zone zone : zoneList) {
            rows.add(zone.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, rows);
    }
}
