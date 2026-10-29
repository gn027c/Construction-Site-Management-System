package util;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.file.*;

/** Prevent two console sessions from writing the same CSV directory. Never waits for a lock. */
public final class DataDirectoryLock implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;

    public DataDirectoryLock(Path directory) throws IOException {
        Files.createDirectories(directory);
        Path root = directory.toRealPath();
        channel = FileChannel.open(root.resolve(".smartsite.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock acquired;
        try { acquired = channel.tryLock(); }
        catch (IOException | RuntimeException e) { channel.close(); throw new IOException("Data directory is busy: " + root, e); }
        if (acquired == null) { channel.close(); throw new IOException("Another SmartSite session is using " + root); }
        lock = acquired;
    }

    @Override public void close() throws IOException {
        try { if (lock.isValid()) lock.release(); } finally { channel.close(); }
    }
}
