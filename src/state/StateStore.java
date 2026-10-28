package state;

import java.io.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import util.CsvHelper;

/**
 * Versioned CSV snapshots. One non-blocking process lock per data directory.
 * Snapshot files are immutable after publication; CURRENT is the commit point.
 * Maintainer: Khang SE201461; integration: Tuan personnel, Thien zones, Loi attendance/incidents.
 */
public final class StateStore implements AutoCloseable {
    private static final String[] FILES = {"persons.csv", "zones.csv", "attendance.csv", "incidents.csv"};
    private final Path root;
    private final Path history;
    private final FileChannel channel;
    private final FileLock lock;
    private boolean closed;
    private String generation = "legacy";

    public StateStore(Path directory) throws IOException {
        Files.createDirectories(directory);
        root = directory.toRealPath();
        history = root.resolve(".smartsite-state");
        channel = FileChannel.open(root.resolve(".smartsite.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock acquired;
        try { acquired = channel.tryLock(); }
        catch (IOException | RuntimeException e) { channel.close(); throw new IOException("Data directory is busy: " + root, e); }
        if (acquired == null) { channel.close(); throw new IOException("Another SmartSite session is using " + root); }
        lock = acquired;
    }

    private void checkOpen() {
        if (closed || !lock.isValid()) throw new IllegalStateException("State session is closed");
    }

    public synchronized String getGeneration() { return generation; }

    public synchronized AppState load() throws IOException {
        checkOpen();
        Path pointer = history.resolve("CURRENT");
        if (Files.exists(pointer)) {
            List<String> lines = CsvHelper.readAllLines(pointer.toString());
            if (lines.size() != 1 || !lines.get(0).matches("[a-f0-9-]{36}")) throw new IOException("Invalid state pointer");
            String id = lines.get(0);
            Path snapshot = history.resolve(id);
            verifyManifest(snapshot);
            AppState staged = read(snapshot);
            generation = id;
            return staged;
        }
        int existing = 0;
        for (String file : FILES) if (Files.exists(root.resolve(file))) existing++;
        if (existing == 0) { generation = "new"; return new AppState(); }
        if (existing != FILES.length) throw new IOException("Incomplete initial CSV set. No state was replaced.");
        AppState staged = read(root);
        generation = "legacy";
        return staged;
    }

    private AppState read(Path folder) throws IOException {
        AppState staged = new AppState();
        if (!staged.personnel.loadFromCsv(folder.resolve(FILES[0]).toString())) throw new IOException("Invalid persons.csv in " + folder);
        if (!staged.zones.loadFromCsv(folder.resolve(FILES[1]).toString())) throw new IOException("Invalid zones.csv in " + folder);
        if (!staged.attendance.loadFromCsv(folder.resolve(FILES[2]).toString())) throw new IOException("Invalid attendance.csv in " + folder);
        if (!staged.incidents.loadFromCsv(folder.resolve(FILES[3]).toString())) throw new IOException("Invalid incidents.csv in " + folder);
        try { staged.validate(); } catch (RuntimeException e) { throw new IOException("Invalid state references in " + folder, e); }
        return staged;
    }

    private void write(AppState state, Path folder) throws IOException {
        state.validate();
        Files.createDirectories(folder);
        if (!state.personnel.saveToCsv(folder.resolve(FILES[0]).toString()) ||
            !state.zones.saveToCsv(folder.resolve(FILES[1]).toString()) ||
            !state.attendance.saveToCsv(folder.resolve(FILES[2]).toString()) ||
            !state.incidents.saveToCsv(folder.resolve(FILES[3]).toString())) throw new IOException("CSV write failed: " + folder);
        read(folder); // Validate the exact bytes being committed, including mutable model fields.
    }

    public synchronized void save(AppState state) throws IOException {
        checkOpen();
        String next = UUID.randomUUID().toString();
        Path snapshot = history.resolve(next);
        write(state, snapshot);
        List<String> manifest = new ArrayList<>();
        manifest.add("schema,file,sha256");
        for (String file : FILES) manifest.add(CsvHelper.toCsvLine("1", file, hash(snapshot.resolve(file))));
        if (!CsvHelper.writeAllLines(snapshot.resolve("manifest.csv").toString(), manifest)) throw new IOException("Cannot write manifest");
        verifyManifest(snapshot);
        if (!CsvHelper.writeAllLines(history.resolve("CURRENT").toString(), Arrays.asList(next))) throw new IOException("Cannot commit CURRENT; previous state retained");
        generation = next;
    }

    private static String hash(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(Files.readAllBytes(path));
            StringBuilder text = new StringBuilder();
            for (byte value : bytes) text.append(String.format("%02x", value & 255));
            return text.toString();
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    private void verifyManifest(Path folder) throws IOException {
        try {
            List<String[]> rows = CsvHelper.readRows(folder.resolve("manifest.csv").toString());
            if (rows.size() != 5 || !Arrays.equals(rows.get(0), new String[]{"schema","file","sha256"})) throw new IOException("Invalid snapshot manifest");
            for (int i = 0; i < FILES.length; i++) {
                String[] entry = rows.get(i + 1);
                if (entry.length != 3 || !entry[0].equals("1") || !entry[1].equals(FILES[i]) || !entry[2].equals(hash(folder.resolve(FILES[i])))) throw new IOException("Snapshot checksum mismatch: " + FILES[i]);
            }
        } catch (RuntimeException e) { throw new IOException("Unreadable snapshot manifest", e); }
    }

    /** New unique directory; never overwrites the live state or an earlier export. */
    public synchronized Path exportCsv(AppState state, Path destination) throws IOException {
        checkOpen();
        Path folder = destination.toAbsolutePath().resolve("smartsite-export-" + UUID.randomUUID());
        write(state, folder);
        for (String file : FILES) {
            List<String> safeRows = new ArrayList<>();
            for (String[] row : CsvHelper.readRows(folder.resolve(file).toString())) {
                for (int i = 0; i < row.length; i++) {
                    String lead = row[i].trim();
                    if (!lead.isEmpty() && "=+-@".indexOf(lead.charAt(0)) >= 0) row[i] = "'" + row[i];
                }
                safeRows.add(CsvHelper.toCsvLine(row));
            }
            if (!CsvHelper.writeAllLines(folder.resolve(file).toString(), safeRows)) throw new IOException("Export incomplete: " + folder);
        }
        Files.write(folder.resolve("README.txt"), Arrays.asList("SmartSite CSV export", "UTF-8 BOM. Formula-like cells prefixed with apostrophe for spreadsheet display.", "This is a report export, not an exact backup. Live snapshots retain original values.", "Source committed generation: " + generation, "Export includes current in-memory changes."), StandardCharsets.UTF_8);
        return folder;
    }

    @Override public synchronized void close() throws IOException {
        if (!closed) {
            closed = true;
            try { lock.release(); } finally { channel.close(); }
        }
    }
}
