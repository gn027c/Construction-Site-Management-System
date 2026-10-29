import static support.Checks.*;
import java.nio.file.*;
import java.util.*;
import model.*;
import service.*;
import exception.*;
public class ZoneTest {
 public static void main(String[] args) throws Exception {
  ZoneService zs=new ZoneService(); Worker w=new Worker("P1","W1","A","0901234567","T","X_SAFE","B");
  RestrictedZone z=new RestrictedZone("Z1","Zone, test",RestrictedZone.SafetyLevel.HIGH,new HashSet<>());
  test("TC-ZONE-01 regular allows known person", () -> { require(zs.addZone(new Zone("R1","Regular"))); require(zs.verifyAccess("R1",w)); });
  test("TC-ZONE-02 grant and revoke access", () -> { require(zs.addZone(z)); require(zs.grantZoneAccess("Z1","W1")); require(zs.verifyAccess("Z1",w)); require(zs.revokeZoneAccess("Z1","W1")); throwsType(RestrictedZoneViolationException.class, () -> zs.verifyAccess("Z1",w)); });
  test("TC-ZONE-03 invalid credential denied", () -> { zs.grantZoneAccess("Z1","W1"); w.setSafetyCertId("UNSAFE"); throwsType(RestrictedZoneViolationException.class, () -> zs.verifyAccess("Z1",w)); });
  test("TC-ZONE-04 visitor denied despite whitelist", () -> { zs.grantZoneAccess("Z1","V1"); throwsType(RestrictedZoneViolationException.class, () -> zs.verifyAccess("Z1",new Visitor("P2","V1","V1","0901234567","P1","H","28/10/2026"))); });
  test("TC-ZONE-05 duplicate and unknown zone", () -> { throwsType(DuplicateZoneIdException.class, () -> zs.addZone(new Zone("z1","Duplicate"))); throwsType(ZoneNotFoundException.class, () -> zs.findZoneById("missing")); });
  test("Zone quoted name and whitelist round trip", () -> { Path f=Files.createTempFile("zone", ".csv"); try { require(zs.saveToCsv(f.toString())); ZoneService loaded=new ZoneService(); require(loaded.loadFromCsv(f.toString())); RestrictedZone a=(RestrictedZone)loaded.findZoneById("Z1"); equal("Zone, test",a.getZoneName()); equal(z.getAllowedPersonCodes(),a.getAllowedPersonCodes()); a.getAllowedPersonCodes().clear(); equal(2,a.getAllowedPersonCodes().size()); } finally { Files.deleteIfExists(f); } });
  test("Zone malformed level preserves memory", () -> { Path f=Files.createTempFile("zone-bad", ".csv"); try { util.CsvHelper.writeAllLines(f.toString(),Arrays.asList("type,zoneId,zoneName,requiredSafetyLevel,allowedPersonCodes","RESTRICTED,Z1,BAD,INVALID,W1")); require(!zs.loadFromCsv(f.toString())); equal(2,zs.getAllZones().size()); } finally { Files.deleteIfExists(f); } });
  finish();
 }
}
