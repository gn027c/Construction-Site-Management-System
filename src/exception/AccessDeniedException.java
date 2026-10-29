package exception;

/**
 * Reports a protected application operation without the required account permission.
 */
public class AccessDeniedException extends SiteManagementException {
    public AccessDeniedException(String operation) {
        super("Access denied: " + operation + ".");
    }
}
