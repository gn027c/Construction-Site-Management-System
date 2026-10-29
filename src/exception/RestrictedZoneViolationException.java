package exception;

/**
 * Reports denial by a restricted zone's access policy.
 *
 * <p>The policy requires whitelist membership and a valid safety credential.
 * Role labels do not bypass these requirements.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 *
 * <p>Responsibility: Le Tan Thien (SE201852) - module exceptions.
 */
public class RestrictedZoneViolationException extends SiteManagementException {
    public RestrictedZoneViolationException(String personCode, String zoneId, String reason) {
        super(
                "Access Denied: Person [" + personCode
                        + "] is not permitted to enter Restricted Zone [" + zoneId
                        + "]. Reason: " + reason
        );
    }
}
