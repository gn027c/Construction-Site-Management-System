import controller.SiteManagerController;
import state.AppState;
import state.StateStore;
import util.InputHelper;
import view.ConsoleTheme;
import view.MenuView;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Console bootstrap and state session. Maintainer: Khang SE201461. */
public class Main {
    public static void main(String[] args) {
        Path directory = Paths.get(System.getProperty("smartsite.dataDir", "data"));
        try (StateStore store = new StateStore(directory); Scanner scanner = new Scanner(System.in)) {
            AppState state = store.load(); // Fail before any write if startup files are invalid.
            ConsoleTheme.success("Loaded state: " + store.getGeneration());
            SiteManagerController controller = controller(state, store);
            boolean exit = false;
            while (!exit) {
                try {
                    MenuView.showMainMenu();
                    int choice = InputHelper.getInt(scanner, "Choice (0-5): ", 0, 5);
                    switch (choice) {
                        case 1: controller.handlePersonMenu(scanner); break;
                        case 2: controller.handleZoneMenu(scanner); break;
                        case 3: controller.handleAttendanceMenu(scanner); break;
                        case 4: controller.handleIncidentMenu(scanner); break;
                        case 5:
                            MenuView.showDataMenu();
                            int action = InputHelper.getInt(scanner, "Choice (0-3): ", 0, 3);
                            if (action == 1) {
                                String answer = InputHelper.getString(scanner, "Discard unsaved changes and reload committed state? (yes/no): ");
                                if (answer.equalsIgnoreCase("yes")) {
                                    AppState loaded = store.load(); // Swap only after all modules validate.
                                    state = loaded;
                                    controller = controller(state, store);
                                    ConsoleTheme.success("Committed state reloaded.");
                                }
                            } else if (action == 2) {
                                store.save(state); ConsoleTheme.success("State saved.");
                            } else if (action == 3) {
                                String path = InputHelper.getString(scanner, "Export parent directory: ");
                                ConsoleTheme.success("Export completed: " + store.exportCsv(state, Paths.get(path)));
                            }
                            break;
                        case 0:
                            store.save(state);
                            ConsoleTheme.success("State saved. Goodbye.");
                            exit = true;
                            break;
                    }
                } catch (NoSuchElementException e) {
                    store.save(state);
                    ConsoleTheme.success("Input closed. State saved.");
                    exit = true;
                } catch (Exception e) {
                    ConsoleTheme.error(e.getMessage());
                }
            }
        } catch (Exception e) {
            ConsoleTheme.error("Session stopped: " + e.getMessage());
            System.exit(1);
        }
    }

    private static SiteManagerController controller(final AppState state, final StateStore store) {
        return new SiteManagerController(state, () -> {
            try { store.save(state); }
            catch (IOException e) { throw new exception.SiteManagementException("Checkpoint failed. Changes remain in memory; retry Save before exit.", e); }
        });
    }
}
