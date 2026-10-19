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
        List<String> lines = util.CsvHelper.readAllLines(filePath);
        if (lines.isEmpty()) {
            return false;
        }
        store.clear();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (i == 0 && line.toLowerCase().startsWith("type")) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length < 6) {
                continue;
            }
            String type = parts[0].trim().toUpperCase();
            String id = parts[1].trim();
            String code = parts[2].trim();
            String name = parts[3].trim();
            String phone = parts[4].trim();

            // TODO (WS6 - Thành viên 2: Trần Ngọc Anh Tuấn - SE201513, nhánh tuantna-final-verification):
            // Kiểm tra toàn vẹn dữ liệu persons.csv (Task 2.2): Worker đang được nạp lại với trade = "Construction"
            // và team = "Team A" cố định, Visitor với entryDate = "01/01/2026" cố định, nên các giá trị thật bị mất
            // sau một lần lưu/nạp. Ghi log thực tế cho TC-PERS-01..06.
            try {
                if (type.equals("WORKER") && parts.length >= 7) {
                    String cert = parts[6].trim();
                    store.put(code, new model.Worker(id, code, name, phone, "Construction", cert, "Team A"));
                } else if (type.equals("CONTRACTOR") && parts.length >= 8) {
                    String contractId = parts[6].trim();
                    String companyName = parts[7].trim();
                    store.put(code, new model.Contractor(id, code, name, phone, companyName, contractId));
                } else if (type.equals("VISITOR") && parts.length >= 8) {
                    String purpose = parts[6].trim();
                    String hostPersonName = parts[7].trim();
                    store.put(code, new model.Visitor(id, code, name, phone, purpose, hostPersonName, "01/01/2026"));
                }
            } catch (Exception e) {
                System.out.println("Error parsing person record: " + line + " - " + e.getMessage());
            }
        }
        return true;
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("type,id,code,name,phoneNumber,role,extra1,extra2");
        for (Person p : store.values()) {
            lines.add(p.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }
}
