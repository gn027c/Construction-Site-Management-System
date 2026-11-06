package model;

import util.ValidationHelper;

public class Zone {
    protected String zoneId;
    protected String zoneName;

    public Zone(String zoneId, String zoneName) {
        this.zoneId = ValidationHelper.requireText(zoneId, "Mã khu vực (zoneId) không được để trống.");
        this.zoneName = ValidationHelper.requireText(zoneName, "Tên khu vực (zoneName) không được để trống.");
    }

    public static String normalizeId(String raw) {
        return util.IdHelper.normalize(raw);
    }

    public static boolean isSameId(String id1, String id2) {
        return util.IdHelper.isSame(id1, id2);
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void rename(String newName) {
        zoneName = ValidationHelper.requireText(newName, "zoneName", "Must not be empty.");
    }

    public String accessDenialReason(Person person) {
        if (person == null) {
            return "Person cannot be null";
        }
        if (!person.isActive()) {
            return "Person is inactive";
        }
        return null;
    }

    public boolean checkAccess(Person person) {
        return accessDenialReason(person) == null;
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine("REGULAR", zoneId, zoneName, "NONE", "NONE");
    }

    @Override
    public String toString() {
        return String.format("Zone{zoneId='%s', zoneName='%s'}", zoneId, zoneName);
    }
}
