import static support.Checks.*;
import model.person.*;
import model.zone.*;
import model.attendance.*;
import model.incident.*;
import model.auth.*;
import model.common.*;
import repository.*;
import service.*;
import exception.*;
import java.nio.file.*;
import java.util.*;
public class PersonnelTest {
 public static void main(String[] args) throws Exception {
  PersonnelService ps = new PersonnelService();
  test("TC-PERS-01 add all personnel types", () -> {
   require(ps.addPerson(new Worker("P1","W1","An, Test","0901234567","Welder","X_SAFE","Team B")));
   require(ps.addPerson(new Contractor("P2","C1","B","0901234567","Company, Inc","CTR-1")));
   require(ps.addPerson(new Visitor("P3","V1","C1","0901234567","Survey","W1","28/10/2026")));
  });
  test("TC-PERS-02 duplicate rejected", () -> throwsType(DuplicatePersonCodeException.class, () -> ps.addPerson(new Worker("P4","W1","Other","0901234567","T","X_SAFE","B"))));
  test("TC-PERS-03 invalid phone rejected", () -> throwsType(InvalidPhoneNumberException.class, () -> ps.addPerson(new Visitor("P5","X1","X1","123","Survey","W1","28/10/2026"))));
  test("TC-PERS-04 lookup", () -> equal("An, Test", ps.findPersonByCode("W1").getName()));
  test("TC-PERS-05 unknown rejected", () -> throwsType(PersonNotFoundException.class, () -> ps.findPersonByCode("missing")));
  test("TC-PERS-06 update failure preserves data", () -> {
   throwsType(InvalidPhoneNumberException.class, () -> ps.updatePerson("W1","Wrong","123"));
   equal("An, Test",ps.findPersonByCode("W1").getName());
   require(ps.updatePerson("W1","New, Name","0987654321"));
  });
  test("Personnel complete round trip", () -> {
   Path f=Files.createTempFile("people", ".csv");
   try {
    require(ps.saveToCsv(f.toString())); PersonnelService loaded=new PersonnelService(); require(loaded.loadFromCsv(f.toString()));
    Worker w=(Worker)loaded.findPersonByCode("W1"); equal("Welder",w.getTrade()); equal("Team B",w.getAssignedTeam()); equal("New, Name",w.getName());
    equal("28/10/2026",((Visitor)loaded.findPersonByCode("V1")).getEntryDate());
    equal("Company, Inc",((Contractor)loaded.findPersonByCode("C1")).getCompanyName());
   } finally { Files.deleteIfExists(f); }
  });
  test("Fixture explicitly marks missing personnel information", () -> {
   PersonnelService fixture=new PersonnelService(); require(fixture.loadFromCsv("data/persons.csv"));
   equal("Unknown",((Worker)fixture.findPersonByCode("W01")).getTrade());
   equal("",((Visitor)fixture.findPersonByCode("V01")).getEntryDate());
  });
  test("Invalid reload preserves personnel", () -> {
   Path f=Files.createTempFile("people-bad", ".csv");
   try { util.CsvHelper.writeAllLines(f.toString(),Arrays.asList("type,id", "BAD,row")); require(!ps.loadFromCsv(f.toString())); equal(3,ps.getAllPersons().size()); }
   finally { Files.deleteIfExists(f); }
  }); finish();
 }
}
