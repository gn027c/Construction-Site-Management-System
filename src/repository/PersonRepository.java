package repository;

import exception.DuplicatePersonCodeException;
import exception.PersonNotFoundException;
import model.Person;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TẦNG LƯU TRỮ (REPOSITORY LAYER)
 * PHỤ TRÁCH: Thành viên 2 (Trần Ngọc Anh Tuấn - SE201513)
 * MÔ TẢ: Quản lý lưu trữ và truy xuất tập hợp đối tượng Person trong bộ nhớ bằng LinkedHashMap O(1).
 */
public class PersonRepository {
    private final Map<String, Person> store = new LinkedHashMap<>();

    /** Saves a person; throws DuplicatePersonCodeException if the code already exists. */
    public boolean save(Person person) throws DuplicatePersonCodeException {
        if (person == null || person.getCode() == null || person.getCode().trim().isEmpty()) {
            return false;
        }
        String code = person.getCode().trim();
        if (store.containsKey(code)) {
            throw new DuplicatePersonCodeException(code);
        }
        store.put(code, person);
        return true;
    }

    /** Looks up a person by code; returns null if not found. */
    public Person findByCode(String code) {
        if (code == null) {
            return null;
        }
        return store.get(code.trim());
    }

    /** Looks up a person by code; throws PersonNotFoundException if not found. */
    public Person findByCodeOrThrow(String code) throws PersonNotFoundException {
        Person person = findByCode(code);
        if (person == null) {
            throw new PersonNotFoundException(code != null ? code.trim() : "null");
        }
        return person;
    }

    public boolean deleteByCode(String code) throws PersonNotFoundException {
        Person p = findByCodeOrThrow(code);
        return store.remove(p.getCode()) != null;
    }

    public List<Person> findAll() {
        return new ArrayList<>(store.values());
    }

   public boolean loadFromCsv(String filePath) {
    List<String> lines = CsvHelper.readAllLines(filePath);

    // CsvHelper returns an empty list if the file does not exist or cannot be read
    if (lines.isEmpty()) {
        return false;
    }

    store.clear();

    for (String line : lines) {
        // Skip blank lines
        if (line == null || line.trim().isEmpty()) {
            continue;
        }

        String[] f = parseCsvLine(line);

        // Skip the header row
        if (f[0].equalsIgnoreCase("type")) {
            continue;
        }

        try {
            Person person = parsePerson(f);
            if (person != null) {
                save(person);
            }
        } catch (DuplicatePersonCodeException | IllegalArgumentException e) {
            // Malformed or duplicate row: skip it and keep loading the rest
            System.out.println("Skipping invalid line: " + line + " (" + e.getMessage() + ")");
        }
    }
    return true;
}

/** Splits a CSV line into fields, supporting quoted fields with "" escapes (RFC 4180). */
private String[] parseCsvLine(String line) {
    List<String> fields = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        if (inQuotes) {
            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"'); // "" -> "
                    i++;
                } else {
                    inQuotes = false;
                }
            } else {
                cur.append(c);
            }
        } else {
            if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
    }
    fields.add(cur.toString().trim());
    return fields.toArray(new String[0]);
}

/** Creates the correct subclass based on the type column. Returns null if the type is unknown or columns are missing. */
private Person parsePerson(String[] f) {
    if (f.length < 6) {
        return null;
    }
    String type = f[0].toUpperCase();
    Role role = Role.valueOf(f[5].toUpperCase()); // throws IllegalArgumentException if the role is invalid

    switch (type) {
        case "WORKER":
            if (f.length < 7) return null;
            return new Worker(f[1], f[2], f[3], f[4], role, f[6]);
        case "CONTRACTOR":
            if (f.length < 8) return null;
            return new Contractor(f[1], f[2], f[3], f[4], role, f[6], f[7]);
        case "VISITOR":
            if (f.length < 8) return null;
            return new Visitor(f[1], f[2], f[3], f[4], role, f[6], f[7]);
        default:
            return null;
    }

   public boolean saveToCsv(String filePath) {
    List<String> lines = new ArrayList<>();

    // Standard CSV header required by the task
    lines.add("type,id,code,name,phoneNumber,role,extra1,extra2");

    // Iterate over all persons; toCsvLine() is polymorphic (Worker/Contractor/Visitor)
    for (Person p : store.values()) {
        lines.add(p.toCsvLine());
    }

    // CsvHelper writes the file with a UTF-8 BOM and returns false on I/O error
    return CsvHelper.writeAllLines(filePath, lines);
}
}
