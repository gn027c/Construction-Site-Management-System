package exception;

public class AttendanceOrderException extends SiteManagementException {
    public AttendanceOrderException(String personCode, String violationDetail) {
        super("Invalid attendance order for person [" + personCode + "]: " + violationDetail);
    }
}
