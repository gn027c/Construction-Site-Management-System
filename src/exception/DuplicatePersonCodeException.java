package exception;

/**
 * Reports an attempt to register a personnel code that already exists.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 *
 * <p>Responsibility: Tran Ngoc Anh Tuan (SE201513) - module exceptions.
 */
public class DuplicatePersonCodeException extends SiteManagementException {
    public DuplicatePersonCodeException(String code) {
        super("Personnel code already exists in the system: " + code);
    }
}
