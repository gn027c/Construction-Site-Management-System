import static support.Checks.*;
import exception.*;
import model.*;
import repository.PersonRepository;
import java.util.*;

public class ModelValidationTest {
    private static Worker worker() {
        return new Worker(" P1 ", " W1 ", " Worker ", " 0901234567 ", " Welder ", " X_SAFE ", " A ");
    }

    public static void main(String[] args) throws Exception {
        test("Model construction normalizes required text once", () -> {
            Worker w = worker();
            equal("P1", w.getId()); equal("W1", w.getCode()); equal("Worker", w.getName());
            equal("0901234567", w.getPhoneNumber()); equal("Welder", w.getTrade());
            require(w.hasValidSafetyCredential());
        });
        test("Blank field errors consistently name the invalid field", () -> {
            Worker w = worker();
            try { w.setName("  "); throw new AssertionError("Blank name accepted"); }
            catch (DataValidationException e) { equal("Invalid value for field [name]: Must not be blank.", e.getMessage()); }
            equal("Worker", w.getName());
            throwsType(DataValidationException.class, () -> w.setTrade(null));
            equal("Welder", w.getTrade());
        });
        test("Model enforces phone format without a service", () -> {
            Worker w = worker();
            try { w.setPhoneNumber("123"); throw new AssertionError("Invalid phone accepted"); }
            catch (InvalidPhoneNumberException e) {
                equal("Invalid value for field [phoneNumber]: Must contain 10 digits and start with 0.", e.getMessage());
            }
            equal("0901234567", w.getPhoneNumber());
            throwsType(InvalidPhoneNumberException.class, () -> new Visitor("P1", "V1", "Visitor", "123", "Survey", "Host", ""));
        });
        test("Contact update validates all fields before changing either", () -> {
            Worker w = worker();
            throwsType(InvalidPhoneNumberException.class, () -> w.updateContactDetails("Changed", "bad"));
            equal("Worker", w.getName()); equal("0901234567", w.getPhoneNumber());
            w.updateContactDetails(" New name ", " 0987654321 ");
            equal("New name", w.getName()); equal("0987654321", w.getPhoneNumber());
        });
        test("Person codes cannot conflict with whitelist encoding", () -> {
            for (String code : Arrays.asList("NONE", "none", "W;1"))
                throwsType(DataValidationException.class, () -> new Worker("P1", code, "Worker", "0901234567", "Trade", "X_SAFE", "A"));
        });
        test("Whitelist validates and normalizes every code before replacing it", () -> {
            RestrictedZone z = new RestrictedZone(" Z1 ", " Zone ", RestrictedZone.SafetyLevel.HIGH, new HashSet<>(Arrays.asList(" W1 ")));
            require(z.checkAccess(worker()));
            throwsType(DataValidationException.class, () -> z.setAllowedPersonCodes(new HashSet<>(Arrays.asList("W2", " "))));
            equal(new HashSet<>(Arrays.asList("W1")), z.getAllowedPersonCodes());
            throwsType(DataValidationException.class, () -> z.grantAccess("NONE"));
            throwsType(DataValidationException.class, () -> z.revokeAccess(null));
        });
        test("Profile changes preserve the repository identity", () -> {
            Worker w = worker(); PersonRepository repo = new PersonRepository(); repo.save(w);
            w.updateContactDetails("Updated", "0987654321");
            equal(w, repo.findByCode("W1")); equal("P1", w.getId()); equal(Role.WORKER, w.getRole());
            require(repo.deleteByCode("W1")); equal(0, repo.findAll().size());
        });
        test("Required enums and subclass fields use the common validation error", () -> {
            throwsType(DataValidationException.class, () -> new RestrictedZone("Z1", "Zone", null, new HashSet<>()));
            throwsType(DataValidationException.class, () -> new Contractor("P1", "C1", "Contractor", "0901234567", null, "CTR-1"));
            throwsType(DataValidationException.class, () -> new Visitor("P1", "V1", "Visitor", "0901234567", " ", "Host", ""));
        });
        finish();
    }
}
