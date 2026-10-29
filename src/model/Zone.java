package model;

import static model.ModelValidation.*;

/**
 * Represents a regular site zone with a fixed identifier and a validated name.
 *
 * <p>A regular zone accepts any non-null person. Subclasses may override
 * {@link #checkAccess(Person)} to apply stricter access rules.
 *
 * <p>Responsibility: Le Tan Thien (SE201852) - zone models and access policy.
 */
public class Zone {
    private final String zoneId;
    private String zoneName;

    public Zone(String zoneId, String zoneName) {
        this.zoneId = requiredText(zoneId, "zoneId");
        this.zoneName = requiredText(zoneName, "zoneName");
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = requiredText(zoneName, "zoneName");
    }

    /**
     * Applies the access policy for a regular zone.
     *
     * @param person the person requesting access, possibly null
     * @return true when a person is supplied
     */
    public boolean checkAccess(Person person) {
        return person != null;
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine("REGULAR", zoneId, zoneName, "NONE", "NONE");
    }

    @Override
    public String toString() {
        return String.format("Zone{zoneId='%s', zoneName='%s'}", zoneId, zoneName);
    }
}
