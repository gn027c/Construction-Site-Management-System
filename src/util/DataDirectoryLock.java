package util;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.file.*;

/**
 * Holds an exclusive file lock for one console session's data directory.
 *
 * <p>Acquisition uses a nonblocking attempt and fails when another session holds
 * the lock. External tools must cooperate with this lock for it to protect the
 * files. Closing releases both the lock and its channel.
 */
public final class DataDirectoryLock implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;

    /**
     * Attempts to acquire the data directory lock without waiting.
     */
    public DataDirectoryLock(Path directory) throws IOException {
        Files.createDirectories(directory);
        Path root = directory.toRealPath();
        channel = FileChannel.open(
                root.resolve(".smartsite.lock"),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        );
        FileLock acquired;
        try {
            acquired = channel.tryLock();
        } catch (IOException | RuntimeException e) {
            channel.close();
            throw new IOException("Data directory is busy: " + root, e);
        }
        if (acquired == null) {
            channel.close();
            throw new IOException("Another SmartSite session is using " + root);
        }
        lock = acquired;
    }

    @Override
    public void close() throws IOException {
        try {
            if (lock.isValid()) {
                lock.release();
            }
        } finally {
            channel.close();
        }
    }
}
