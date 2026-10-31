package exception;

public class ZoneNotFoundException extends SiteManagementException {
    public ZoneNotFoundException(String zoneId) {
        super("Construction zone not found with ID: " + zoneId);
    }
}
