package exception;

/**
 * Reports a swipe that cannot follow the preceding attendance record.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 */
public class AttendanceOrderException extends SiteManagementException {
    public AttendanceOrderException(String personCode, String violationDetail) {
        super("Invalid attendance order for person [" + personCode + "]: " + violationDetail);
    }
}
