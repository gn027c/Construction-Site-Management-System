package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvHelper {

    public static final String UTF8_BOM = "\uFEFF";

    public static List<String> readAllLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {

                if (isFirstLine) {
                    if (line.startsWith(UTF8_BOM)) {
                        line = line.substring(1);
                    }
                    isFirstLine = false;
                }
                lines.add(line);
            }
        } catch (Exception e) {

            return new ArrayList<>();
        }
        return lines;
    }

    public static boolean writeAllLines(String filePath, List<String> lines) {
        if (lines == null) {
            return false;
        }
        try {
            java.io.File parent = new java.io.File(filePath).getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
        } catch (Exception ignored) {
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {

            writer.write(UTF8_BOM);

            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                if (i < lines.size() - 1) {
                    writer.newLine();
                }
            }
            writer.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String escapeSpecialCharacters(String field) {
        if (field == null) {
            return "";
        }
        String escapedField = field;

        if (escapedField.contains("\"")) {
            escapedField = escapedField.replace("\"", "\"\"");
        }

        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            escapedField = "\"" + escapedField + "\"";
        }

        return escapedField;
    }

    public interface RowParser<T> {
        T parse(List<String> columns);
    }

    public interface RowKey<T> {
        String of(T row);
    }

    public static <T> List<T> readTable(
            String filePath, String header, int columnCount, RowParser<T> parser, RowKey<T> key
    ) {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            return null;
        }
        String fileName = file.getName();
        List<String> lines = readAllLines(filePath);
        if (lines.isEmpty() || !header.equals(lines.get(0).trim())) {
            throw new exception.DataValidationException(
                    fileName + " line 1: unexpected header. Expected: " + header);
        }
        List<T> rows = new ArrayList<>();
        java.util.Set<String> keys = new java.util.HashSet<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                List<String> columns = parseLine(line);
                if (columns.size() != columnCount) {
                    throw new exception.DataValidationException(
                            "Expected " + columnCount + " columns, found " + columns.size() + ".");
                }
                T row = parser.parse(columns);
                if (!keys.add(key.of(row).toUpperCase())) {
                    throw new exception.DataValidationException("Duplicate identifier " + key.of(row) + ".");
                }
                rows.add(row);
            } catch (RuntimeException e) {
                throw new exception.DataValidationException(
                        fileName + " line " + (i + 1) + ": " + e.getMessage());
            }
        }
        return rows;
    }

    public static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (quoted) {
                if (current != '"') {
                    field.append(current);
                } else if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = false;
                }
            } else if (current == '"') {
                quoted = true;
            } else if (current == ',') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }
        if (quoted) {
            throw new exception.DataValidationException("A quoted value is not closed.");
        }
        fields.add(field.toString());
        return fields;
    }

    public static String toCsvLine(String... values) {
        if (values == null || values.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escapeSpecialCharacters(values[i]));
        }
        return sb.toString();
    }
}
