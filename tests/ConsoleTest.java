import static support.Checks.*;
import state.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class ConsoleTest {
 static String launch(Path root, String input, int exit, boolean colors) throws Exception {
  Process p=new ProcessBuilder(StateStoreTest.java(),"-Dsmartsite.dataDir="+root,"-Dsmartsite.color="+(colors?"always":"never"),"-Dfile.encoding=UTF-8","-cp",System.getProperty("java.class.path"),"Main").redirectErrorStream(true).start();
  p.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8)); p.getOutputStream().close();
  if (!p.waitFor(10,TimeUnit.SECONDS)) { p.destroyForcibly(); throw new AssertionError("Console timeout"); }
  equal(exit,p.exitValue());
  ByteArrayOutputStream out=new ByteArrayOutputStream(); byte[] bytes=new byte[4096]; int n; while((n=p.getInputStream().read(bytes))!=-1) out.write(bytes,0,n);
  return new String(out.toByteArray(),StandardCharsets.UTF_8);
 }
 public static void main(String[] args) throws Exception {
  test("Console EOF exits and commits without looping", () -> { Path root=Files.createTempDirectory("smartsite-eof"); String out=launch(root,"",0,false); require(out.contains("Input closed. State saved.")); require(!out.contains("\u001b[")); try(StateStore store=new StateStore(root)) { equal(0,store.load().personnel.getAllPersons().size()); } });
  test("Console EOF inside submenu retains completed edits", () -> { Path root=Files.createTempDirectory("smartsite-edit"); launch(root,"1\n1\n1\nP1\nW1\nWorker A\n0901234567\nWelder\nX_SAFE\nTeam B\n",0,false); try(StateStore store=new StateStore(root)) { equal("Worker A",store.load().personnel.findPersonByCode("W1").getName()); } });
  test("Console startup fails without overwriting malformed data", () -> { Path root=Files.createTempDirectory("smartsite-bad"); Files.write(root.resolve("persons.csv"),Arrays.asList("bad")); String out=launch(root,"0\n",1,false); require(out.contains("Incomplete initial CSV set")); require(!Files.exists(root.resolve(".smartsite-state/CURRENT"))); });
  test("Console export menu writes separate report", () -> { Path root=Files.createTempDirectory("smartsite-export-ui"); Path dest=root.resolve("exports"); String out=launch(root,"5\n3\n"+dest+"\n0\n",0,false); require(out.contains("Export completed:")); try(java.util.stream.Stream<Path> files=Files.list(dest)) { equal(1L,files.count()); } });
  test("Console explicitly enabled colors", () -> { String out=launch(Files.createTempDirectory("smartsite-color"),"0\n",0,true); if(System.getenv("NO_COLOR")==null) require(out.contains("\u001b[1;36m")); });
  test("Date validation rejects impossible dates and suffixes", () -> { util.InputHelper.validateDate("29/02/2024","date"); throwsType(exception.DataValidationException.class, () -> util.InputHelper.validateDate("29/02/2026","date")); throwsType(exception.DataValidationException.class, () -> util.InputHelper.validateDate("28/10/2026 junk","date")); });
  test("Completed checkpoint survives forced process termination", () -> {
   Path root=Files.createTempDirectory("smartsite-kill"); Path log=root.resolve("console.txt");
   Process p=new ProcessBuilder(StateStoreTest.java(),"-Dsmartsite.dataDir="+root,"-Dsmartsite.color=never","-cp",System.getProperty("java.class.path"),"Main").redirectErrorStream(true).redirectOutput(log.toFile()).start();
   try {
    String input="1\n1\n1\nP1\nW1\nCheckpoint worker\n0901234567\nWelder\nX_SAFE\nTeam B\n";
    p.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8)); p.getOutputStream().flush();
    long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(10); boolean checkpointed=false;
    while(System.nanoTime()<deadline && p.isAlive()) {
     String text=new String(Files.readAllBytes(log),StandardCharsets.UTF_8);
     if(text.split("Enter option \\(0-3\\)",-1).length>=3) { checkpointed=true; break; }
     Thread.sleep(25);
    }
    require(checkpointed);
   } finally { p.destroyForcibly(); require(p.waitFor(10,TimeUnit.SECONDS)); }
   try(StateStore store=new StateStore(root)) { equal("Checkpoint worker",store.load().personnel.findPersonByCode("W1").getName()); }
  });
  finish();
 }
}
