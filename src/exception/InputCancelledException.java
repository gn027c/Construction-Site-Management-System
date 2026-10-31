package exception;

public final class InputCancelledException extends RuntimeException {
    public InputCancelledException() {
        super("Input cancelled by the user.");
    }
}
