package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** UTF-8 CSV codec, including quoted multiline fields. Maintainer: Khang SE201461. */
public final class CsvHelper {
    public static final String UTF8_BOM = "\uFEFF";
    private CsvHelper() { }

    public static List<String> readAllLines(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
            if (!lines.isEmpty() && lines.get(0).startsWith(UTF8_BOM)) lines.set(0, lines.get(0).substring(1));
            return lines;
        } catch (IOException e) { throw new UncheckedIOException("Cannot read " + filePath, e); }
    }

    public static List<String[]> readRows(String path) {
        try { return parseRecords(new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8)); }
        catch (IOException e) { throw new UncheckedIOException("Cannot read " + path, e); }
    }

    public static String[] parseLine(String line) {
        List<String[]> rows = parseRecords(line);
        if (rows.size() != 1) throw new IllegalArgumentException("Expected one CSV record");
        return rows.get(0);
    }

    private static List<String[]> parseRecords(String text) {
        Objects.requireNonNull(text, "CSV text");
        List<String[]> rows = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false, closed = false, started = false;
        for (int i = text.startsWith(UTF8_BOM) ? 1 : 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') { field.append('"'); i++; }
                    else { quoted = false; closed = true; }
                } else field.append(ch);
                continue;
            }
            if (ch == ',' || ch == '\r' || ch == '\n') {
                fields.add(field.toString()); field.setLength(0); closed = false;
                if (ch == ',') started = true;
                else {
                    rows.add(fields.toArray(new String[0])); fields.clear(); started = false;
                    if (ch == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                }
            } else if (ch == '"' && field.length() == 0 && !closed) { quoted = true; started = true; }
            else {
                if (ch == '"' || closed) throw new IllegalArgumentException("Malformed CSV at character " + (i + 1));
                field.append(ch); started = true;
            }
        }
        if (quoted) throw new IllegalArgumentException("Unclosed CSV quote");
        if (started || !fields.isEmpty()) { fields.add(field.toString()); rows.add(fields.toArray(new String[0])); }
        return rows;
    }

    public static boolean isBlankRow(String[] row) { return row.length == 1 && row[0].trim().isEmpty(); }
    public static String toCsvLine(String... fields) {
        List<String> escaped = new ArrayList<>();
        for (String field : fields) escaped.add(escapeSpecialCharacters(field));
        return String.join(",", escaped);
    }
    public static String escapeSpecialCharacters(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\r") || field.contains("\n"))
            return "\"" + field.replace("\"", "\"\"") + "\"";
        return field;
    }

    /** Write a sibling temporary file, then atomically replace. No unsafe fallback. */
    public static boolean writeAllLines(String filePath, List<String> lines) {
        if (lines == null || lines.contains(null)) return false;
        Path temporary = null;
        try {
            Path target = Paths.get(filePath).toAbsolutePath();
            Files.createDirectories(target.getParent());
            temporary = Files.createTempFile(target.getParent(), ".smartsite-", ".tmp");
            byte[] bytes = (UTF8_BOM + String.join("\r\n", lines)).getBytes(StandardCharsets.UTF_8);
            try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException | RuntimeException e) { return false; }
        finally { if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) { } }
    }
}
