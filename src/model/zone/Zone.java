package model.zone;

import static model.common.ModelValidation.*;
import model.person.Person;

public class Zone {
    private final String zoneId;
    private String zoneName;

    public Zone(String zoneId, String zoneName) {
        this.zoneId = identifier(zoneId, "zoneId");
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

    public boolean checkAccess(Person person) {
        return accessDenialReason(person).isEmpty();
    }

    public String accessDenialReason(Person person) {
        return person == null ? "No person was given." : "";
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine("REGULAR", zoneId, zoneName, "NONE", "NONE");
    }

    @Override
    public String toString() {
        return String.format("Zone{zoneId='%s', zoneName='%s'}", zoneId, zoneName);
    }
}
