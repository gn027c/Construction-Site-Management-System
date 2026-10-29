package exception;

/**
 * Signals an explicit console cancellation independently of invalid data or closed input.
 */
public final class InputCancelledException extends RuntimeException {
    public InputCancelledException() {
        super("Input cancelled by the user.");
    }
}
