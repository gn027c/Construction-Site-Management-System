import state.*;
import model.*;
import static support.Checks.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class StateStoreTest {
 static AppState populated() {
  AppState s=new AppState(); s.personnel.addPerson(new Worker("1","W","=1+1","0901234567","Welder","CERT_SAFE","B")); s.zones.addZone(new Zone("Z","Regular")); return s;
 }
 static Path tmp() throws Exception { return Files.createTempDirectory("smartsite-state-test-"); }
 public static void main(String[] args) throws Exception {
  test("TC-IO-01 first run and restart", () -> { Path root=tmp(); try(StateStore store=new StateStore(root)) { equal(0,store.load().personnel.getAllPersons().size()); store.save(populated()); } try(StateStore store=new StateStore(root)) { equal("=1+1",store.load().personnel.findPersonByCode("W").getName()); } });
  test("TC-IO-02 full snapshot retains attendance FSM and incident state", () -> { Path root=tmp(); try(StateStore store=new StateStore(root)) { AppState s=populated(); s.attendance.recordAttendance("W","Z",AttendanceRecord.CheckType.CHECK_IN); s.incidents.reportIncident("I","Title","Description",Incident.IncidentSeverity.HIGH); s.incidents.assignIncident("I","W"); store.save(s); AppState loaded=store.load(); throwsType(exception.AttendanceOrderException.class, () -> loaded.attendance.recordAttendance("W","Z",AttendanceRecord.CheckType.CHECK_IN)); equal(Incident.IncidentStatus.ASSIGNED,loaded.incidents.findIncidentById("I").getStatus()); } });
  test("TC-IO-03 failed checkpoint retains CURRENT and previous generation", () -> { Path root=tmp(); try(StateStore store=new StateStore(root)) { AppState s=populated(); store.save(s); String previous=store.getGeneration(); s.incidents.reportIncident("I","Title","Description",Incident.IncidentSeverity.LOW); s.incidents.findIncidentById("I").setStatus(Incident.IncidentStatus.ASSIGNED); throwsType(IllegalStateException.class, () -> store.save(s)); equal(previous,store.getGeneration()); equal(0,store.load().incidents.getAllIncidents().size()); } });
  test("Snapshot checksum rejects corruption", () -> { Path root=tmp(); try(StateStore store=new StateStore(root)) { store.save(populated()); Files.write(root.resolve(".smartsite-state").resolve(store.getGeneration()).resolve("persons.csv"),new byte[]{1}); throwsType(java.io.IOException.class, () -> store.load()); } });
  test("Incomplete initial CSV fails without publishing state", () -> { Path root=tmp(); Files.write(root.resolve("persons.csv"),Arrays.asList("type,id"),StandardCharsets.UTF_8); try(StateStore store=new StateStore(root)) { throwsType(java.io.IOException.class, () -> store.load()); require(!Files.exists(root.resolve(".smartsite-state/CURRENT"))); } });
  test("Uncommitted snapshot directories are ignored", () -> { Path root=tmp(); try(StateStore store=new StateStore(root)) { store.save(populated()); String previous=store.getGeneration(); Files.createDirectories(root.resolve(".smartsite-state/unfinished")); equal(1,store.load().personnel.getAllPersons().size()); equal(previous,store.getGeneration()); } });
  test("Load legacy project data without modifying originals", () -> { Path root=tmp(); for(String f:Arrays.asList("persons.csv","zones.csv","attendance.csv","incidents.csv")) Files.copy(Paths.get("data",f),root.resolve(f)); byte[] original=Files.readAllBytes(root.resolve("persons.csv")); try(StateStore store=new StateStore(root)) { AppState s=store.load(); equal(10,s.personnel.getAllPersons().size()); store.save(s); require(Arrays.equals(original,Files.readAllBytes(root.resolve("persons.csv")))); } });
  test("CSV export is separate and spreadsheet-safe", () -> { Path root=tmp(); try(StateStore store=new StateStore(root)) { AppState s=populated(); store.save(s); String previous=store.getGeneration(); Path export=store.exportCsv(s,root.resolve("reports")); equal("'=1+1",util.CsvHelper.readRows(export.resolve("persons.csv").toString()).get(1)[3]); equal("=1+1",store.load().personnel.findPersonByCode("W").getName()); equal(previous,store.getGeneration()); require(Files.exists(export.resolve("README.txt"))); } });
  test("Second process fails promptly while session owns data", () -> { Path root=tmp(); try(StateStore store=new StateStore(root)) { Process process=new ProcessBuilder(java(),"-cp",System.getProperty("java.class.path"),"StateLockProbe",root.toString()).redirectErrorStream(true).start(); require(process.waitFor(10,TimeUnit.SECONDS)); equal(2,process.exitValue()); } try(StateStore store=new StateStore(root)) { store.load(); } });
  test("Closed session cannot save", () -> { StateStore store=new StateStore(tmp()); store.close(); throwsType(IllegalStateException.class, () -> store.save(populated())); });
  finish();
 }
 static String java() { return Paths.get(System.getProperty("java.home"),"bin","java.exe").toString(); }
}
