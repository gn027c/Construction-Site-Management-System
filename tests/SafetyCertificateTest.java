import java.time.LocalDate;
import model.person.SafetyCertificate;
import model.person.Worker;
import model.person.Staff;

public class SafetyCertificateTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        test("SafetyCertificate value equality and hash code", () -> {
            LocalDate future = LocalDate.now().plusMonths(6);
            SafetyCertificate cert1 = new SafetyCertificate("CERT-001", "ELECTRICAL", future, false, true);
            SafetyCertificate cert2 = new SafetyCertificate("CERT-001", "ELECTRICAL", future, false, true);
            SafetyCertificate certDiff = new SafetyCertificate("CERT-002", "ELECTRICAL", future, false, true);

            require(cert1.equals(cert2));
            require(cert1.hashCode() == cert2.hashCode());
            require(!cert1.equals(certDiff));
        });

        test("SafetyCertificate validity rules", () -> {
            SafetyCertificate valid = SafetyCertificate.valid("CERT-SAFE");
            require(valid.isValid());
            require(!valid.isExpired());
            require(!valid.isRevoked());

            SafetyCertificate expired = new SafetyCertificate("CERT-OLD", "GENERAL", LocalDate.now().minusDays(1), false, true);
            require(!expired.isValid());
            require(expired.isExpired());

            SafetyCertificate revoked = new SafetyCertificate("CERT-REV", "GENERAL", LocalDate.now().plusYears(1), true, true);
            require(!revoked.isValid());
            require(revoked.isRevoked());

            SafetyCertificate uncertified = SafetyCertificate.uncertified("CERT-UNSAFE");
            require(!uncertified.isValid());
        });

        test("SafetyCertificate adapts from legacy raw strings", () -> {
            SafetyCertificate fromSafe = SafetyCertificate.fromRaw("CERT_W01_SAFE");
            require(fromSafe.isValid());
            equal("CERT_W01_SAFE", fromSafe.getCertNumber());

            SafetyCertificate fromUnsafe = SafetyCertificate.fromRaw("CERT_UNSAFE");
            require(!fromUnsafe.isValid());

            SafetyCertificate fromEmpty = SafetyCertificate.fromRaw("");
            require(!fromEmpty.isValid());

            SafetyCertificate fromNull = SafetyCertificate.fromRaw(null);
            require(!fromNull.isValid());
        });

        test("Worker integration with SafetyCertificate Value Object", () -> {
            Worker worker = new Worker("P1", "W1", "Worker A", "0901234567", "Welder", "CERT_W1_SAFE", "Team 1");
            require(worker.hasValidSafetyCredential());
            require(worker.getSafetyCertificate() != null);
            equal("CERT_W1_SAFE", worker.getSafetyCertificate().getCertNumber());

            // Updating with expired certificate
            worker.setSafetyCertificate(new SafetyCertificate("CERT-EXP", LocalDate.now().minusDays(2)));
            require(!worker.hasValidSafetyCredential());

            // Updating with legacy setSafetyCertId
            worker.setSafetyCertId("NEW_SAFE");
            require(worker.hasValidSafetyCredential());
            equal("NEW_SAFE", worker.getSafetyCertId());
        });

        test("Staff integration with SafetyCertificate Value Object", () -> {
            Staff staff = new Staff("P2", "S1", "Engineer", "0902345678", "Site Engineer", "STAFF_SAFE");
            require(staff.hasValidSafetyCredential());
            equal("STAFF_SAFE", staff.getSafetyCertId());

            staff.setSafetyCertId("UNSAFE");
            require(!staff.hasValidSafetyCredential());
        });

        System.out.println("RESULT " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void test(String name, Runnable runnable) {
        try {
            runnable.run();
            System.out.println("PASS " + name);
            passed++;
        } catch (Throwable t) {
            System.out.println("FAIL " + name + ": " + t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }
    }

    private static void require(boolean condition) {
        if (!condition) {
            throw new AssertionError("Requirement failed");
        }
    }

    private static void equal(Object expected, Object actual) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
