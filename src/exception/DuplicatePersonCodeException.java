package exception;

public class DuplicatePersonCodeException extends SiteManagementException {
    public DuplicatePersonCodeException(String code) {
        super("Personnel code already exists in the system: " + code);
    }
}
