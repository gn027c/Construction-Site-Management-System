package support;

public final class Checks {
    private static int passed;
    private static int failed;

    private Checks() { }

    public interface Body { void run() throws Exception; }

    public static void test(String name, Body body) {
        try {
            body.run();
            passed++;
            System.out.println("PASS " + name);
        } catch (Throwable failure) {
            failed++;
            System.out.println("FAIL " + name + ": " + failure);
            failure.printStackTrace(System.out);
        }
    }

    public static void equal(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError("Expected <" + expected + ">, got <" + actual + ">");
        }
    }

    public static void require(boolean value) {
        if (!value) throw new AssertionError("Expected true");
    }

    public static void throwsType(Class<? extends Throwable> type, Body body) throws Exception {
        try {
            body.run();
        } catch (Throwable failure) {
            if (type.isInstance(failure)) return;
            throw new AssertionError("Expected " + type.getSimpleName() + ", got " + failure, failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    public static void finish() {
        System.out.println("RESULT " + passed + " passed, " + failed + " failed");
        if (failed != 0) throw new AssertionError("Regression tests failed");
    }
}
