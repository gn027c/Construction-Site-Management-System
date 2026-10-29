import util.DataDirectoryLock;
import java.nio.file.Paths;
public class CsvLockProbe {
 public static void main(String[] args) {
  try(DataDirectoryLock lock=new DataDirectoryLock(Paths.get(args[0]))) { }
  catch(Exception e) { System.exit(2); }
 }
}
