package exception;

public class AccessDeniedException extends SiteManagementException {
    public AccessDeniedException(String operation) {
        super("Access denied: " + operation + ".");
    }
}
