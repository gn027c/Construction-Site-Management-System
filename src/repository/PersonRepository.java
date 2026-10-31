package repository;

import exception.DuplicatePersonCodeException;
import exception.PersonNotFoundException;
import model.person.Person;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class PersonRepository {
    private final Map<String, Person> store = new LinkedHashMap<>();

    public boolean save(Person person) throws DuplicatePersonCodeException {
        if (person == null || person.getCode() == null || person.getCode().trim().isEmpty()) {
            return false;
        }
        String code = person.getCode().trim();
        if (store.containsKey(code)) {
            throw new DuplicatePersonCodeException(code);
        }
        for (Person existing : store.values()) {
            if (existing.getId().equals(person.getId())) {
                throw new exception.DataValidationException("id", "Already belongs to another personnel record.");
            }
        }
        store.put(code, person);
        return true;
    }

    public Person findByCode(String code) {
        if (code == null) {
            return null;
        }
        return store.get(util.IdentifierHelper.normalizeForLookup(code));
    }

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
            if (rows.isEmpty()
                    || !Arrays.equals(
                            rows.get(0),
                            new String[] {
                                    "type", "id", "code", "name", "phoneNumber",
                                    "role", "extra1", "extra2", "extra3", "active", "safetyLevel"
                            }
                    )) {
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                if (util.CsvHelper.isBlankRow(p)) {
                    continue;
                }
                if (p.length != 11 || !(p[9].equals("true") || p[9].equals("false"))) {
                    return false;
                }
                Person person;
                switch (p[0]) {
                    case "WORKER":
                        person = new model.person.Worker(p[1], p[2], p[3], p[4], p[7], p[6], p[8]);
                        break;
                    case "CONTRACTOR":
                        if (!p[8].isEmpty()) {
                            return false;
                        }
                        person = new model.person.Contractor(p[1], p[2], p[3], p[4], p[7], p[6]);
                        break;
                    case "VISITOR":
                        person = new model.person.Visitor(p[1], p[2], p[3], p[4], p[6], p[7], p[8]);
                        break;
                    case "STAFF":
                        if (!p[8].isEmpty()) {
                            return false;
                        }
                        person = new model.person.Staff(p[1], p[2], p[3], p[4], p[7], p[6]);
                        break;
                    default:
                        return false;
                }
                if (!person.getRole().name().equals(p[5])) {
                    return false;
                }
                person.setActive(Boolean.parseBoolean(p[9]));
                person.setSafetyLevel(model.common.SafetyLevel.valueOf(p[10]));
                staged.save(person);
            }
        } catch (RuntimeException e) {
            return false;
        }
        store.clear();
        store.putAll(staged.store);
        return true;
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("type,id,code,name,phoneNumber,role,extra1,extra2,extra3,active,safetyLevel");
        for (Person p : store.values()) {
            lines.add(p.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }
}
