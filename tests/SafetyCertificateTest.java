import static support.Checks.*;

import model.person.SafetyCertificate;
import model.person.Staff;
import model.person.Worker;

public class SafetyCertificateTest {
    public static void main(String[] args) throws Exception {
        test("SafetyCertificate value equality and hash code", () -> {
            SafetyCertificate first = new SafetyCertificate("CERT-001_SAFE");
            SafetyCertificate same = new SafetyCertificate(" CERT-001_SAFE ");
            SafetyCertificate other = new SafetyCertificate("CERT-002_SAFE");
            require(first.equals(same));
            equal(first.hashCode(), same.hashCode());
            require(!first.equals(other));
        });
        test("A certificate number ending in _SAFE is the only valid credential", () -> {
            require(new SafetyCertificate("CERT_W01_SAFE").isValid());
            require(!new SafetyCertificate("CERT_UNSAFE").isValid());
            require(!new SafetyCertificate("").isValid());
            require(!new SafetyCertificate(null).isValid());
            equal("", SafetyCertificate.EMPTY.getCertNumber());
        });
        test("Worker integration with SafetyCertificate value object", () -> {
            Worker worker = new Worker("P1", "W1", "Worker A", "0901234567", "Welder", "CERT_W1_SAFE", "Team 1");
            require(worker.hasValidSafetyCredential());
            equal("CERT_W1_SAFE", worker.getSafetyCertificate().getCertNumber());
            worker.setSafetyCertificate(new SafetyCertificate("CERT_UNSAFE"));
            require(!worker.hasValidSafetyCredential());
            worker.setSafetyCertId("NEW_SAFE");
            require(worker.hasValidSafetyCredential());
            equal("NEW_SAFE", worker.getSafetyCertId());
        });
        test("A worker may have no certificate yet and then has no credential", () -> {
            Worker trainee = new Worker("P1", "W1", "Trainee", "0901234567", "Welder", "", "Team 1");
            equal("", trainee.getSafetyCertId());
            require(!trainee.hasValidSafetyCredential());
            require(trainee.toCsvLine().startsWith("WORKER,P1,W1,Trainee,0901234567,WORKER,,Welder,Team 1,"));
        });
        test("Staff integration with SafetyCertificate value object", () -> {
            Staff staff = new Staff("P2", "S1", "Engineer", "0902345678", "Site Engineer", "STAFF_SAFE");
            require(staff.hasValidSafetyCredential());
            equal("STAFF_SAFE", staff.getSafetyCertId());
            staff.setSafetyCertId("UNSAFE");
            require(!staff.hasValidSafetyCredential());
        });
        finish();
    }
}
