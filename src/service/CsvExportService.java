package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import util.CsvHelper;

/** Writes spreadsheet-friendly reports separately from the application CSV files. */
public final class CsvExportService {
    public Path export(Map<String, List<String>> tables, Path destination) throws IOException {
        Files.createDirectories(destination);
        Path folder = Files.createDirectory(destination.resolve("smartsite-export-" + UUID.randomUUID()));
        for (Map.Entry<String, List<String>> table : tables.entrySet()) {
            List<String> safeRows = new ArrayList<>();
            for (String line : table.getValue()) {
                String[] fields = CsvHelper.parseLine(line);
                for (int i = 0; i < fields.length; i++) {
                    String lead = fields[i].trim();
                    if (!lead.isEmpty() && "=+-@".indexOf(lead.charAt(0)) >= 0) fields[i] = "'" + fields[i];
                }
                safeRows.add(CsvHelper.toCsvLine(fields));
            }
            if (!CsvHelper.writeAllLines(folder.resolve(table.getKey()).toString(), safeRows))
                throw new IOException("Export incomplete: " + folder);
        }
        Files.write(folder.resolve("README.txt"), Arrays.asList("SmartSite CSV export", "UTF-8 BOM. Formula-like cells are prefixed with an apostrophe for spreadsheet display.", "Export includes current in-memory data. It is a report, not an exact backup for import."), StandardCharsets.UTF_8);
        return folder.toAbsolutePath();
    }
}
