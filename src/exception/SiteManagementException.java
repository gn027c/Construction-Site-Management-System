package exception;

/**
 * Provides the unchecked base exception for SmartSite domain and application errors.
 *
 * <p>Subclasses describe specific failures. Models and services decide when to
 * raise them; console controllers decide how to present them to the user.
 *
 * <p>Responsibilities:
 * <ul>
 *     <li>Huynh Nguyen Hoang Khang (SE201461) - base exception, validation errors and UI handling.</li>
 *     <li>Tran Ngoc Anh Tuan (SE201513) - personnel exceptions.</li>
 *     <li>Le Tan Thien (SE201852) - zone and access exceptions.</li>
 *     <li>Nguyen Tan Loi (SE211059) - attendance and incident exceptions.</li>
 * </ul>
 */
public class SiteManagementException extends RuntimeException {
    public SiteManagementException() {
        super("An error occurred in SmartSite Management System.");
    }

    public SiteManagementException(String message) {
        super(message);
    }

    public SiteManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
