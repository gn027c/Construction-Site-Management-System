package util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DataPaths {
    public static final String ACCOUNTS_FILE = "accounts.csv";
    public static final String PERSONS_FILE = "persons.csv";
    public static final String ZONES_FILE = "zones.csv";
    public static final String ATTENDANCE_FILE = "attendance.csv";
    public static final String INCIDENTS_FILE = "incidents.csv";

    private final Path baseDir;

    public DataPaths() {
        this(System.getProperty("smartsite.dataDir", "data"));
    }

    public DataPaths(String directory) {
        this.baseDir = Paths.get(directory != null && !directory.trim().isEmpty() ? directory : "data");
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public String path(String fileName) {
        return baseDir.resolve(fileName).toString();
    }

    public String accounts() {
        return path(ACCOUNTS_FILE);
    }

    public String persons() {
        return path(PERSONS_FILE);
    }

    public String zones() {
        return path(ZONES_FILE);
    }

    public String attendance() {
        return path(ATTENDANCE_FILE);
    }

    public String incidents() {
        return path(INCIDENTS_FILE);
    }
}

