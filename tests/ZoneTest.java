import static support.Checks.*;
import java.nio.file.*;
import java.util.*;
import model.person.*;
import model.zone.*;
import model.attendance.*;
import model.incident.*;
import model.auth.*;
import model.common.*;
import service.*;
import exception.*;
public class ZoneTest {
 public static void main(String[] args) throws Exception {
  ZoneService zs=new ZoneService(); Worker w=new Worker("P1","W1","A","0901234567","T","X_SAFE","B"); w.setSafetyLevel(model.common.SafetyLevel.HIGH);
  RestrictedZone z=new RestrictedZone("Z1","Zone, test",model.common.SafetyLevel.HIGH,new HashSet<>());
  test("TC-ZONE-01 regular allows known person", () -> { require(zs.addZone(new Zone("R1","Regular"))); require(zs.verifyAccess("R1",w)); });
  test("TC-ZONE-02 grant and revoke access", () -> { require(zs.addZone(z)); require(zs.grantZoneAccess("Z1","W1")); require(zs.verifyAccess("Z1",w)); require(zs.revokeZoneAccess("Z1","W1")); throwsType(RestrictedZoneViolationException.class, () -> zs.verifyAccess("Z1",w)); });
  test("TC-ZONE-03 invalid credential denied", () -> { zs.grantZoneAccess("Z1","W1"); w.setSafetyCertId("UNSAFE"); throwsType(RestrictedZoneViolationException.class, () -> zs.verifyAccess("Z1",w)); });
  test("TC-ZONE-04 visitor denied despite whitelist", () -> { zs.grantZoneAccess("Z1","V1"); throwsType(RestrictedZoneViolationException.class, () -> zs.verifyAccess("Z1",new Visitor("P2","V1","V1","0901234567","P1","H","28/10/2026"))); });
  test("TC-ZONE-05 duplicate and unknown zone", () -> { throwsType(DuplicateZoneIdException.class, () -> zs.addZone(new Zone("z1","Duplicate"))); throwsType(ZoneNotFoundException.class, () -> zs.findZoneById("missing")); });
  test("Zone quoted name and whitelist round trip", () -> { Path f=Files.createTempFile("zone", ".csv"); try { require(zs.saveToCsv(f.toString())); ZoneService loaded=new ZoneService(); require(loaded.loadFromCsv(f.toString())); RestrictedZone a=(RestrictedZone)loaded.findZoneById("Z1"); equal("Zone, test",a.getZoneName()); equal(z.getAllowedPersonCodes(),a.getAllowedPersonCodes()); a.getAllowedPersonCodes().clear(); equal(2,a.getAllowedPersonCodes().size()); } finally { Files.deleteIfExists(f); } });
  test("Zone malformed level preserves memory", () -> { Path f=Files.createTempFile("zone-bad", ".csv"); try { util.CsvHelper.writeAllLines(f.toString(),Arrays.asList("type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes","RESTRICTED,Z1,BAD,INVALID,W1")); require(!zs.loadFromCsv(f.toString())); equal(2,zs.getAllZones().size()); } finally { Files.deleteIfExists(f); } });
  test("Safety level must cover the zone level", () -> {
   RestrictedZone high = new RestrictedZone("Z7", "High zone", SafetyLevel.HIGH, new HashSet<>(Arrays.asList("W7")));
   Worker worker = new Worker("P7", "W7", "Level worker", "0901234567", "Welder", "CERT_07_SAFE", "Team");
   worker.setSafetyLevel(SafetyLevel.MEDIUM);
   equal("Safety level MEDIUM is below the required HIGH.", high.accessDenialReason(worker));
   worker.setSafetyLevel(SafetyLevel.HIGH);
   require(high.checkAccess(worker));
   worker.setSafetyLevel(SafetyLevel.CRITICAL);
   require(high.checkAccess(worker));
  });
  test("Denial reasons follow list, credential and level order", () -> {
   RestrictedZone low = new RestrictedZone("Z8", "Low zone", SafetyLevel.LOW, new HashSet<>(Arrays.asList("W8")));
   Worker outsider = new Worker("P9", "W9", "Outsider", "0901234567", "Welder", "CERT_09_SAFE", "Team");
   outsider.setSafetyLevel(SafetyLevel.CRITICAL);
   equal("Not on the allowed list for this zone.", low.accessDenialReason(outsider));
   Worker uncertified = new Worker("P8", "W8", "Uncertified", "0901234567", "Welder", "CERT_UNSAFE", "Team");
   uncertified.setSafetyLevel(SafetyLevel.CRITICAL);
   equal("No valid safety credential.", low.accessDenialReason(uncertified));
   equal("", new Zone("Z9", "Regular").accessDenialReason(uncertified));
  });
  test("Restricted zones need a real level and visitors cannot hold one", () -> {
   throwsType(DataValidationException.class, () -> new RestrictedZone("Z6", "Bad", SafetyLevel.NONE, new HashSet<>()));
   Visitor visitor = new Visitor("P5", "V5", "Guest", "0901234567", "Tour", "Host", "");
   throwsType(DataValidationException.class, () -> visitor.setSafetyLevel(SafetyLevel.LOW));
   equal(SafetyLevel.NONE, visitor.getSafetyLevel());
  });
  test("Sample data denies W1 at the CRITICAL zone Z4 by level only", () -> {
   PersonnelService people = new PersonnelService();
   ZoneService zones = new ZoneService();
   require(people.loadFromCsv("data/persons.csv"));
   require(zones.loadFromCsv("data/zones.csv"));
   require(zones.verifyAccess("Z3", people.findPersonByCode("W1")));
   RestrictedZoneViolationException denied = null;
   try {
    zones.verifyAccess("Z4", people.findPersonByCode("W1"));
   } catch (RestrictedZoneViolationException e) {
    denied = e;
   }
   require(denied != null && denied.getMessage().contains("Safety level HIGH is below the required CRITICAL."));
  });
  test("Unknown safety levels in CSV are rejected", () -> {
   Path f = Files.createTempFile("person-level", ".csv");
   try {
    util.CsvHelper.writeAllLines(f.toString(), Arrays.asList(
      "type,id,code,name,phoneNumber,role,extra1,extra2,extra3,active,safetyLevel",
      "WORKER,P1,W1,Worker,0901234567,WORKER,CERT_SAFE,Welder,Team,true,EXTREME"));
    require(!new PersonnelService().loadFromCsv(f.toString()));
    util.CsvHelper.writeAllLines(f.toString(), Arrays.asList(
      "type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes",
      "RESTRICTED,Z1,Zone,NONE,W1"));
    require(!new ZoneService().loadFromCsv(f.toString()));
   } finally {
    Files.deleteIfExists(f);
   }
  });
  finish();
 }
}
