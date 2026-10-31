package exception;

public class DuplicateZoneIdException extends SiteManagementException {
    public DuplicateZoneIdException(String zoneId) {
        super("Zone ID already exists in the system: " + zoneId);
    }
}
