import static support.Checks.*;
import model.*;
import service.*;
import repository.*;
import exception.*;
import java.nio.file.*;
import java.util.*;
public class AttendanceIncidentTest {
 public static void main(String[] args) throws Exception {
  PersonnelService ps=new PersonnelService(); ZoneService zs=new ZoneService();
  ps.addPerson(new Worker("P1","W1","A","0901234567","T","X_SAFE","B")); zs.addZone(new Zone("Z1","Regular")); zs.addZone(new Zone("Z2","Other"));
  AttendanceService as=new AttendanceService(ps,zs); IncidentService is=new IncidentService(ps);
  test("TC-ATT-01 checkout before checkin", () -> throwsType(AttendanceOrderException.class, () -> as.processAttendance("W1","Z1",AttendanceRecord.CheckType.CHECK_OUT)));
  test("TC-ATT-02 checkin", () -> require(as.recordAttendance("W1","Z1",AttendanceRecord.CheckType.CHECK_IN)));
  test("TC-ATT-03 duplicate checkin", () -> throwsType(AttendanceOrderException.class, () -> as.recordAttendance("W1","Z1",AttendanceRecord.CheckType.CHECK_IN)));
  test("TC-ATT-04 wrong checkout zone", () -> throwsType(AttendanceOrderException.class, () -> as.recordAttendance("W1","Z2",AttendanceRecord.CheckType.CHECK_OUT)));
  test("TC-ATT-05 checkout", () -> require(as.recordAttendance("W1","Z1",AttendanceRecord.CheckType.CHECK_OUT)));
  test("TC-ATT-06 duplicate checkout", () -> throwsType(AttendanceOrderException.class, () -> as.recordAttendance("W1","Z1",AttendanceRecord.CheckType.CHECK_OUT)));
  test("Attendance public API enforces references", () -> throwsType(PersonNotFoundException.class, () -> as.processAttendance("MISSING1","Z1",AttendanceRecord.CheckType.CHECK_IN)));
  test("Attendance restart preserves FSM and next ID", () -> {
   Path f=Files.createTempFile("attendance", ".csv"); try {
    as.processAttendance("W1","Z1",AttendanceRecord.CheckType.CHECK_IN); require(as.saveToCsv(f.toString()));
    AttendanceService loaded=new AttendanceService(ps,zs); require(loaded.loadFromCsv(f.toString()));
    throwsType(AttendanceOrderException.class, () -> loaded.recordAttendance("W1","Z1",AttendanceRecord.CheckType.CHECK_IN));
    require(loaded.recordAttendance("W1","Z1",AttendanceRecord.CheckType.CHECK_OUT)); equal("REC4",loaded.getAllAttendanceRecords().get(3).getRecordId());
   } finally { Files.deleteIfExists(f); }
  });
  test("Attendance invalid enum does not default to checkin", () -> {
   Path f=Files.createTempFile("bad-att", ".csv"); try { util.CsvHelper.writeAllLines(f.toString(),Arrays.asList("recordId,personCode,zoneId,timestamp,checkType","X1,W1,Z1,2026-10-28 07:00:00,INVALID")); require(!as.loadFromCsv(f.toString())); equal(3,as.getAllAttendanceRecords().size()); } finally { Files.deleteIfExists(f); }
  });
  test("TC-INC-01 report and reject duplicate", () -> { require(is.reportIncident("I1","Title, quoted","Cap dut, nguy hiem! \"gap\"\nDòng 2",Incident.IncidentSeverity.CRITICAL)); require(!is.reportIncident("I1","Duplicate","Text",Incident.IncidentSeverity.LOW)); });
  test("TC-INC-02 assign existing person", () -> { require(!is.assignIncident("I1","MISSING1")); require(is.assignIncident("I1","W1")); equal(Incident.IncidentStatus.ASSIGNED,is.findIncidentById("I1").getStatus()); });
  test("TC-INC-03 resolve only assigned", () -> { require(is.reportIncident("J1","Other","Text",Incident.IncidentSeverity.LOW)); throwsType(InvalidIncidentStateException.class, () -> is.resolveIncident("J1")); require(is.resolveIncident("I1")); throwsType(InvalidIncidentStateException.class, () -> is.assignIncident("I1","W1")); });
  test("TC-IO-04 incident multiline complete round trip", () -> { Path f=Files.createTempFile("inc", ".csv"); try { require(is.saveToCsv(f.toString())); IncidentService loaded=new IncidentService(ps); require(loaded.loadFromCsv(f.toString())); equal(is.findIncidentById("I1").getDescription(),loaded.findIncidentById("I1").getDescription()); equal(Incident.IncidentStatus.RESOLVED,loaded.findIncidentById("I1").getStatus()); equal("W1",loaded.findIncidentById("I1").getAssignedTo()); } finally { Files.deleteIfExists(f); } });
  test("TC-INC-04 archive resolved while retaining history", () -> { equal(1,is.purgeResolvedIncidents()); equal(2,is.getAllIncidents().size()); require(is.findIncidentById("I1").isArchived()); });
  finish();
 }
}
