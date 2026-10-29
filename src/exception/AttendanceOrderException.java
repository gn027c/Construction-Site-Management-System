package exception;

/** Invalid swipe transition detected by AttendanceRecord.assertCanFollow. Owner: Loi SE211059. */
public class AttendanceOrderException extends SiteManagementException {

    public AttendanceOrderException(String personCode, String violationDetail) {
        super("Invalid attendance order for person [" + personCode + "]: " + violationDetail);
    }
}
