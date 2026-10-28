import state.StateStore;
import java.nio.file.Paths;
public class StateLockProbe {
 public static void main(String[] args) {
  try(StateStore store=new StateStore(Paths.get(args[0]))) { store.load(); }
  catch(Exception e) { System.exit(2); }
 }
}
