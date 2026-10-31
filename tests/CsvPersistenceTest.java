import static support.Checks.*;
import controller.SiteManagerController;
import service.*;
import model.person.*;
import model.zone.*;
import model.attendance.*;
import model.incident.*;
import model.auth.*;
import model.common.*;
import util.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class CsvPersistenceTest {
 static final String[] FILES={"persons.csv","zones.csv","attendance.csv","incidents.csv"};
 static Path tmp() throws Exception { return Files.createTempDirectory("smartsite-csv-test-"); }
 static void addWorker(SiteManagerController c,String name) { c.handlePersonMenu(new Scanner("1\n1\n"+name+"\n0901234567\nWelder\nX_SAFE\nTeam B\n\n0\n")); }
 static SiteManagerController signedIn(Path root) throws Exception {
  SiteManagerController controller = new SiteManagerController(root);
  controller.getAuthentication().login("admin", "admin");
  return controller;
 }
 static Path populated() throws Exception {
  Path root=tmp();SiteManagerController c=signedIn(root);c.loadAllData();addWorker(c,"Worker");
  c.handleZoneMenu(new Scanner("1\n1\nZ1\nRegular\n0\n"));return root;
 }
 public static void main(String[] args) throws Exception {
  test("Business CSV set remains four files alongside separate account storage", () -> { Path root=tmp(); SiteManagerController c=signedIn(root);c.loadAllData();c.saveAllData();require(Files.isRegularFile(root.resolve("accounts.csv"))); for(String file:FILES)require(Files.isRegularFile(root.resolve(file))); });
  test("Editing a worker stores the chosen safety level in CSV", () -> { Path root=populated();SiteManagerController c=signedIn(root);c.loadAllData();c.handlePersonMenu(new Scanner("2\ne\nW1\n\n\n3\n0\n0\n"));PersonnelService ps=new PersonnelService();require(ps.loadFromCsv(root.resolve("persons.csv").toString()));equal(model.common.SafetyLevel.HIGH,ps.findPersonByCode("W1").getSafetyLevel()); });
  test("Direct CSV reloads personnel without snapshots", () -> { Path root=populated();PersonnelService ps=new PersonnelService();require(ps.loadFromCsv(root.resolve("persons.csv").toString()));equal("Worker",ps.findPersonByCode("W1").getName()); SiteManagerController c=signedIn(root);c.loadAllData();c.handlePersonMenu(new Scanner("2\ne\nW1\nUpdated name\n\n\n0\n0\n"));require(ps.loadFromCsv(root.resolve("persons.csv").toString()));equal("Updated name",ps.findPersonByCode("W1").getName()); });
  test("Direct CSV restart preserves attendance FSM and next REC id", () -> { Path root=populated();SiteManagerController c=signedIn(root);c.loadAllData();c.handleAttendanceMenu(new Scanner("1\nW1\nZ1\n1\n0\n"));c=signedIn(root);c.loadAllData();SiteManagerController repeated=c;throwsType(exception.AttendanceOrderException.class,()->repeated.handleAttendanceMenu(new Scanner("1\nW1\nZ1\n1\n0\n")));equal(2,CsvHelper.readRows(root.resolve("attendance.csv").toString()).size());c.handleAttendanceMenu(new Scanner("1\nW1\nZ1\n2\n0\n"));List<String[]> rows=CsvHelper.readRows(root.resolve("attendance.csv").toString());equal(3,rows.size());equal("REC2",rows.get(2)[0]); });
  test("Failed CSV reload keeps current in-memory data", () -> { Path root=populated();SiteManagerController c=signedIn(root);c.loadAllData();CsvHelper.writeAllLines(root.resolve("persons.csv").toString(),Arrays.asList("type,id","INVALID,row"));throwsType(IOException.class,()->c.loadAllData());c.saveAllData();PersonnelService ps=new PersonnelService();require(ps.loadFromCsv(root.resolve("persons.csv").toString()));equal("Worker",ps.findPersonByCode("W1").getName()); });
  test("Save failure identifies affected CSV", () -> { Path root=tmp();SiteManagerController c=signedIn(root);c.loadAllData();c.saveAllData();Files.delete(root.resolve("zones.csv"));Files.createDirectory(root.resolve("zones.csv"));try{c.saveAllData();throw new AssertionError("Expected failed save");}catch(IOException e){require(e.getMessage().contains("zones.csv"));}require(Files.isDirectory(root.resolve("zones.csv"))); });
  test("Incomplete CSV set is rejected", () -> { Path root=tmp();Files.write(root.resolve("persons.csv"),Arrays.asList("type,id"));throwsType(IOException.class,()->signedIn(root).loadAllData()); });
  test("Invalid historical fixtures are rejected without rewriting files", () -> { Path root=tmp();for(String f:FILES)Files.copy(Paths.get("data",f),root.resolve(f));CsvHelper.writeAllLines(root.resolve("attendance.csv").toString(),Arrays.asList("recordId,personCode,zoneId,timestamp,checkType","ATT1,W1,Z1,2026-10-12 07:30:00,CHECK_IN","ATT6,W1,Z3,2026-10-12 08:15:00,CHECK_IN"));byte[] before=Files.readAllBytes(root.resolve("persons.csv"));byte[] attendance=Files.readAllBytes(root.resolve("attendance.csv"));throwsType(IOException.class,()->signedIn(root).loadAllData());require(Arrays.equals(before,Files.readAllBytes(root.resolve("persons.csv"))));require(Arrays.equals(attendance,Files.readAllBytes(root.resolve("attendance.csv")))); });
  test("Export retains formula protection and original CSV", () -> { Path root=tmp();SiteManagerController c=signedIn(root);c.loadAllData();addWorker(c,"=1+1");Path export=c.exportCsv(root.resolve("reports"));equal("'=1+1",CsvHelper.readRows(export.resolve("persons.csv").toString()).get(1)[3]);equal("=1+1",CsvHelper.readRows(root.resolve("persons.csv").toString()).get(1)[3]);require(Files.exists(export.resolve("README.txt"))); });
  test("Read-only views do not write CSV", () -> { Path root=populated();SiteManagerController c=signedIn(root);c.loadAllData();FileTime old=FileTime.fromMillis(946684800000L);for(String file:FILES)Files.setLastModifiedTime(root.resolve(file),old);c.handlePersonMenu(new Scanner("2\n0\n2\n0\n0\n"));for(String file:FILES)equal(old,Files.getLastModifiedTime(root.resolve(file))); });
  finish();
 }
}
