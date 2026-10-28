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
        PersonRepository staged = new PersonRepository();
        try {
            List<String[]> rows = util.CsvHelper.readRows(filePath);
            if (rows.isEmpty() || !rows.get(0)[0].equals("type")) return false;
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                if (util.CsvHelper.isBlankRow(p)) continue;
                if (p.length < 7 || !p[4].matches("0\\d{9}")) return false;
                Person person;
                switch (p[0]) {
                    case "WORKER":
                        if (p.length != 7 && p.length != 9) return false;
                        person = new model.Worker(p[1], p[2], p[3], p[4],
                            p.length == 9 ? p[7] : "Unknown (legacy)", p[6],
                            p.length == 9 ? p[8] : "Unknown (legacy)");
                        break;
                    case "CONTRACTOR":
                        if (p.length != 8) return false;
                        person = new model.Contractor(p[1], p[2], p[3], p[4], p[7], p[6]);
                        break;
                    case "VISITOR":
                        if (p.length != 8 && p.length != 9) return false;
                        person = new model.Visitor(p[1], p[2], p[3], p[4], p[6], p[7],
                            p.length == 9 ? p[8] : "Unknown (legacy)");
                        break;
                    default: return false;
                }
                if (!person.getRole().name().equals(p[5])) return false;
                staged.save(person);
            }
        } catch (RuntimeException e) { return false; }
        store.clear(); store.putAll(staged.store);
        return true;
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("type,id,code,name,phoneNumber,role,extra1,extra2,extra3");
        for (Person p : store.values()) {
            lines.add(p.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }
}
