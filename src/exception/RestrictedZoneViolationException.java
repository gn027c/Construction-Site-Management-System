package exception;

public class RestrictedZoneViolationException extends SiteManagementException {
    public RestrictedZoneViolationException(String personCode, String zoneId, String reason) {
        super(
                "Access Denied: Person [" + personCode
                        + "] is not permitted to enter Restricted Zone [" + zoneId
                        + "]. Reason: " + reason
        );
    }
}
