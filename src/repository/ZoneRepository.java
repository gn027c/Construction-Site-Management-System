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
 * TẦNG LƯU TRỮ (REPOSITORY LAYER)
 * PHỤ TRÁCH: Thành viên 3 (Lê Tấn Thiên - SE201852)
 * MÔ TẢ: Quản lý lưu trữ tập hợp đối tượng Zone trong bộ nhớ và xử lý File I/O (CSV).
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

    public Zone findByIdOrThrow(String zoneId) throws ZoneNotFoundException {
        Zone z = findById(zoneId);
        if (z == null) {
            throw new ZoneNotFoundException(zoneId);
        }
        return z;
    }

    public List<Zone> findAll() {
        return new ArrayList<>(zoneList);
    }

    public boolean loadFromCsv(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            zoneList.clear();

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    if (line.startsWith("﻿")) {
                        line = line.substring(1);
                    }
                    isFirstLine = false;
                    if (line.toLowerCase().startsWith("type") || line.toLowerCase().contains("zoneid")) {
                        continue;
                    }
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length < 3) {
                    continue;
                }

                String type = parts[0].trim();
                String zoneId = parts[1].trim();
                String zoneName = parts[2].trim();

                if (type.equalsIgnoreCase("REGULAR")) {
                    Zone zone = new Zone(zoneId, zoneName);
                    zoneList.add(zone);
                } else if (type.equalsIgnoreCase("RESTRICTED")) {
                    if (parts.length < 5) continue;

                    String levelStr = parts[3].trim();
                    RestrictedZone.SafetyLevel safetyLevel;
                    try {
                        safetyLevel = RestrictedZone.SafetyLevel.valueOf(levelStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        safetyLevel = RestrictedZone.SafetyLevel.MEDIUM;
                    }

                    String codesCol = parts[4].trim();
                    Set<String> allowedCodes = new HashSet<>();
                    if (!codesCol.equalsIgnoreCase("NONE") && !codesCol.isEmpty()) {
                        String[] codes = codesCol.split(";");
                        for (String code : codes) {
                            if (!code.trim().isEmpty()) {
                                allowedCodes.add(code.trim());
                            }
                        }
                    }

                    RestrictedZone rZone = new RestrictedZone(zoneId, zoneName, safetyLevel, allowedCodes);
                    zoneList.add(rZone);
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error reading zones CSV file: " + e.getMessage());
            return false;
        }
    }

    public boolean saveToCsv(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {

            writer.write("﻿");

            writer.write("type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes");
            writer.newLine();

            for (Zone z : zoneList) {
                writer.write(z.toCsvLine());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error writing zones CSV file: " + e.getMessage());
            return false;
        }
    }
}
