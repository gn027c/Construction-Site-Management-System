import static support.Checks.*;
import service.PersonnelService;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class ConsoleTest {
 static String javaExecutable() { return Paths.get(System.getProperty("java.home"),"bin","java.exe").toString(); }
 static String launch(Path root, String input, int exit, boolean colors) throws Exception {
  return launchRaw(root, "admin\nadmin\n" + input, exit, colors);
 }
 static String launchRaw(Path root, String input, int exit, boolean colors) throws Exception {
  return launchScreen(root, input, exit, colors, "never");
 }
 static String launchScreen(Path root, String input, int exit, boolean colors, String screenMode) throws Exception {
  Process process = new ProcessBuilder(
      javaExecutable(), "-Dsmartsite.dataDir=" + root,
      "-Dsmartsite.screen=" + screenMode,
      "-Dsmartsite.color=" + (colors ? "always" : "never"), "-Dfile.encoding=UTF-8",
      "-cp", System.getProperty("java.class.path"), "Main"
  ).redirectErrorStream(true).start();
  ByteArrayOutputStream output = new ByteArrayOutputStream();
  java.util.concurrent.atomic.AtomicReference<IOException> readFailure = new java.util.concurrent.atomic.AtomicReference<>();
  Thread reader = new Thread(() -> {
   try (InputStream stream = process.getInputStream()) {
    byte[] bytes = new byte[4096];
    int count;
    while ((count = stream.read(bytes)) != -1) {
     output.write(bytes, 0, count);
    }
   } catch (IOException e) {
    readFailure.set(e);
   }
  });
  reader.start();
  process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
  process.getOutputStream().close();
  if (!process.waitFor(15, TimeUnit.SECONDS)) {
   process.destroyForcibly();
   process.waitFor(5, TimeUnit.SECONDS);
   reader.join(2000);
   throw new AssertionError("Console timeout: " + new String(output.toByteArray(), StandardCharsets.UTF_8));
  }
  reader.join(2000);
  require(!reader.isAlive());
  if (readFailure.get() != null) {
   throw readFailure.get();
  }
  equal(exit, process.exitValue());
  return new String(output.toByteArray(), StandardCharsets.UTF_8);
 }
 public static void main(String[] args) throws Exception {
  test("Console EOF exits and commits without looping", () -> { Path root=Files.createTempDirectory("smartsite-eof"); String out=launch(root,"",0,false); require(out.contains("Input closed. CSV data saved.")); require(!out.contains("\u001b[")); PersonnelService ps=new PersonnelService(); require(ps.loadFromCsv(root.resolve("persons.csv").toString())); equal(0,ps.getAllPersons().size()); });
  test("Console EOF inside submenu retains completed edits", () -> { Path root=Files.createTempDirectory("smartsite-edit"); launch(root,"1\n1\n1\nWorker A\n0901234567\nWelder\nX_SAFE\nTeam B\n\n",0,false); PersonnelService ps=new PersonnelService(); require(ps.loadFromCsv(root.resolve("persons.csv").toString())); equal("Worker A",ps.findPersonByCode("W1").getName()); });
  test("Console startup allows admin review without overwriting malformed data", () -> { Path root=Files.createTempDirectory("smartsite-bad"); Files.write(root.resolve("persons.csv"),Arrays.asList("bad")); String out=launch(root,"0\n",0,false); require(out.contains("Incomplete CSV set")); require(!Files.exists(root.resolve("zones.csv"))); });
  test("Console export menu writes separate report", () -> { Path root=Files.createTempDirectory("smartsite-export-ui"); Path dest=root.resolve("exports"); String out=launch(root,"5\n3\n"+dest+"\n0\n",0,false); require(out.contains("Export completed:")); try(java.util.stream.Stream<Path> files=Files.list(dest)) { equal(1L,files.count()); } });
  test("Console explicitly enabled colors", () -> { String out=launch(Files.createTempDirectory("smartsite-color"),"0\n",0,true); if(System.getenv("NO_COLOR")==null) { require(out.contains("\u001b[1;36m")); require(out.contains("\u001b[90m  › " + view.ConsoleTheme.toSmallCaps("Site operations, reporting and workspace administration.") + "\u001b[0m")); } });
  test("Date validation rejects impossible dates and suffixes", () -> { util.InputHelper.validateDate("29/02/2024","date"); throwsType(exception.DataValidationException.class, () -> util.InputHelper.validateDate("29/02/2026","date")); throwsType(exception.DataValidationException.class, () -> util.InputHelper.validateDate("28/10/2026 junk","date")); });
  test("Saved CSV survives forced process termination", () -> {
   Path root=Files.createTempDirectory("smartsite-kill"); Path log=root.resolve("console.txt");
   Process p=new ProcessBuilder(javaExecutable(),"-Dsmartsite.dataDir="+root,"-Dsmartsite.color=never","-cp",System.getProperty("java.class.path"),"Main").redirectErrorStream(true).redirectOutput(log.toFile()).start();
   try {
    String input="admin\nadmin\n1\n1\n1\nCheckpoint worker\n0901234567\nWelder\nX_SAFE\nTeam B\n\n";
    p.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8)); p.getOutputStream().flush();
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10); boolean saved=false;
    while(System.nanoTime()<deadline && p.isAlive()) {
     String text=new String(Files.readAllBytes(log),StandardCharsets.UTF_8);
     if(text.split("Enter option \\(0-[45]\\)",-1).length>=3) { saved=true; break; }
     Thread.sleep(25);
    }
    require(saved);
   } finally { p.destroyForcibly(); require(p.waitFor(10,TimeUnit.SECONDS)); }
   PersonnelService ps=new PersonnelService(); require(ps.loadFromCsv(root.resolve("persons.csv").toString())); equal("Checkpoint worker",ps.findPersonByCode("W1").getName());
  });
  finish();
 }
}
