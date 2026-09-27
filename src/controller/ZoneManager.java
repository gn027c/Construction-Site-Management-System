package controller;

import model.Person;
import model.Zone;
import model.RestrictedZone;
import java.util.ArrayList;
import java.util.List;

/**
 * TASK: Member 3 (Le Tan Thien - SE201852)
 * DESCRIPTION:
 * - Manages construction zones and restricted zones.
 * - Handles access grant, revoke, and access verification logic.
 */
public class ZoneManager {
    private List<Zone> zoneList;

    public ZoneManager() {
        this.zoneList = new ArrayList<>();
    }

    public List<Zone> getZoneList() {
        return new ArrayList<>(zoneList);
    }

    // Add a new zone (Check for duplicate zoneId)
    public boolean addZone(Zone z) {
        if (z == null || z.getZoneId() == null || z.getZoneId().isEmpty()) {
            System.out.println("Error: Invalid zone data!");
            return false;
        }
        if (findZoneById(z.getZoneId()) != null) {
            System.out.println("Error: Zone ID '" + z.getZoneId() + "' already exists!");
            return false;
        }
        zoneList.add(z);
        System.out.println("Successfully added zone: " + z.getZoneName());
        return true;
    }

    // Find a zone by its ID
    public Zone findZoneById(String zoneId) {
        if (zoneId == null || zoneId.isEmpty()) return null;
        for (Zone z : zoneList) {
            if (z.getZoneId().equalsIgnoreCase(zoneId)) {
                return z;
            }
        }
        return null;
    }

    // Grant access permission for a restricted zone
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

    // Revoke access permission from a restricted zone
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

    // Verify access rights (Using Polymorphic checkAccess method)
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
