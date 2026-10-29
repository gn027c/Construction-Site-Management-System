import static support.Checks.*;
import controller.SiteManagerController;
import service.*;
import model.*;
import util.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class CsvPersistenceTest {
 static final String[] FILES={"persons.csv","zones.csv","attendance.csv","incidents.csv"};
 static Path tmp() throws Exception { return Files.createTempDirectory("smartsite-csv-test-"); }
 static void addWorker(SiteManagerController c,String name) { c.handlePersonMenu(new Scanner("1\n1\nP1\nW\n"+name+"\n0901234567\nWelder\nX_SAFE\nTeam B\n0\n")); }
 static Path populated() throws Exception {
  Path root=tmp();SiteManagerController c=new SiteManagerController(root);c.loadAllData();addWorker(c,"Worker");
  c.handleZoneMenu(new Scanner("1\n1\nZ\nRegular\n0\n"));return root;
 }
 public static void main(String[] args) throws Exception {
  test("Direct CSV creates exactly four data files", () -> { Path root=tmp(); SiteManagerController c=new SiteManagerController(root);c.loadAllData();c.saveAllData();try(java.util.stream.Stream<Path> paths=Files.list(root)){equal(4L,paths.count());} for(String file:FILES)require(Files.isRegularFile(root.resolve(file))); });
  test("Direct CSV reloads personnel without snapshots", () -> { Path root=populated();PersonnelService ps=new PersonnelService();require(ps.loadFromCsv(root.resolve("persons.csv").toString()));equal("Worker",ps.findPersonByCode("W").getName()); SiteManagerController c=new SiteManagerController(root);c.loadAllData();c.handlePersonMenu(new Scanner("3\nW\nUpdated name\n\n0\n"));require(ps.loadFromCsv(root.resolve("persons.csv").toString()));equal("Updated name",ps.findPersonByCode("W").getName()); });
  test("Direct CSV restart preserves attendance FSM and next REC id", () -> { Path root=populated();SiteManagerController c=new SiteManagerController(root);c.loadAllData();c.handleAttendanceMenu(new Scanner("1\nW\nZ\n1\n0\n"));c=new SiteManagerController(root);c.loadAllData();c.handleAttendanceMenu(new Scanner("1\nW\nZ\n1\n0\n"));equal(2,CsvHelper.readRows(root.resolve("attendance.csv").toString()).size());c.handleAttendanceMenu(new Scanner("1\nW\nZ\n2\n0\n"));List<String[]> rows=CsvHelper.readRows(root.resolve("attendance.csv").toString());equal(3,rows.size());equal("REC0002",rows.get(2)[0]); });
  test("Failed CSV reload keeps current in-memory data", () -> { Path root=populated();SiteManagerController c=new SiteManagerController(root);c.loadAllData();CsvHelper.writeAllLines(root.resolve("persons.csv").toString(),Arrays.asList("type,id","INVALID,row"));throwsType(IOException.class,()->c.loadAllData());c.saveAllData();PersonnelService ps=new PersonnelService();require(ps.loadFromCsv(root.resolve("persons.csv").toString()));equal("Worker",ps.findPersonByCode("W").getName()); });
  test("Save failure identifies affected CSV", () -> { Path root=tmp();Files.createDirectory(root.resolve("zones.csv"));SiteManagerController c=new SiteManagerController(root);try{c.saveAllData();throw new AssertionError("Expected failed save");}catch(IOException e){require(e.getMessage().contains("zones.csv"));}require(Files.isDirectory(root.resolve("zones.csv"))); });
  test("Incomplete CSV set is rejected", () -> { Path root=tmp();Files.write(root.resolve("persons.csv"),Arrays.asList("type,id"));throwsType(IOException.class,()->new SiteManagerController(root).loadAllData()); });
  test("Project fixture references load without rewriting files", () -> { Path root=tmp();for(String f:FILES)Files.copy(Paths.get("data",f),root.resolve(f));byte[] before=Files.readAllBytes(root.resolve("persons.csv"));new SiteManagerController(root).loadAllData();require(Arrays.equals(before,Files.readAllBytes(root.resolve("persons.csv")))); });
  test("Export retains formula protection and original CSV", () -> { Path root=tmp();SiteManagerController c=new SiteManagerController(root);c.loadAllData();addWorker(c,"=1+1");Path export=c.exportCsv(root.resolve("reports"));equal("'=1+1",CsvHelper.readRows(export.resolve("persons.csv").toString()).get(1)[3]);equal("=1+1",CsvHelper.readRows(root.resolve("persons.csv").toString()).get(1)[3]);require(Files.exists(export.resolve("README.txt"))); });
  test("Read-only views do not write CSV", () -> { Path root=populated();SiteManagerController c=new SiteManagerController(root);c.loadAllData();FileTime old=FileTime.fromMillis(946684800000L);for(String file:FILES)Files.setLastModifiedTime(root.resolve(file),old);c.handlePersonMenu(new Scanner("2\n2\n0\n"));for(String file:FILES)equal(old,Files.getLastModifiedTime(root.resolve(file))); });
  test("Concurrent console sessions are rejected without waiting", () -> { Path root=tmp();try(DataDirectoryLock lock=new DataDirectoryLock(root)){Process p=new ProcessBuilder(ConsoleTest.javaExecutable(),"-cp",System.getProperty("java.class.path"),"CsvLockProbe",root.toString()).start();if(!p.waitFor(10,TimeUnit.SECONDS)){p.destroyForcibly();throw new AssertionError("Lock timeout");}equal(2,p.exitValue());}try(DataDirectoryLock lock=new DataDirectoryLock(root)){} });
  finish();
 }
}
