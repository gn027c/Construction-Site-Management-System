package repository;

import exception.DuplicatePersonCodeException;
import exception.PersonNotFoundException;
import model.Person;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 * Stores personnel by code in an insertion-ordered map and handles personnel CSV.
 *
 * <p>Code lookup normalizes identifiers. CSV loading requires the canonical ten-column
 * header and validates all rows before replacing the active map.
 */
public class PersonRepository {
    private final Map<String, Person> store = new LinkedHashMap<>();

    /**
     * Registers a person under its canonical code and unique internal identifier.
     */
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

    /**
     * Looks up equivalent case, separator and zero-padded forms of a personnel code.
     */
    public Person findByCode(String code) {
        if (code == null) {
            return null;
        }
        return store.get(util.IdentifierHelper.normalizeForLookup(code));
    }

    /**
     * Looks up personnel when the caller requires an existing record.
     */
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

    /**
     * Returns a separate list in storage order containing the stored model objects.
     *
     * <p>Changing the returned list does not change this repository's collection.
     * The model objects themselves are shared and retain their validated operations.
     */
    public List<Person> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * Loads CSV into a temporary repository before replacing active data.
     * A read or validation failure returns false and retains the current collection.
     */
    public boolean loadFromCsv(String filePath) {
        PersonRepository staged = new PersonRepository();
        try {
            List<String[]> rows = util.CsvHelper.readRows(filePath);
            if (rows.isEmpty()
                    || !Arrays.equals(
                            rows.get(0),
                            new String[] {
                                    "type", "id", "code", "name", "phoneNumber",
                                    "role", "extra1", "extra2", "extra3", "active"
                            }
                    )) {
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                if (util.CsvHelper.isBlankRow(p)) {
                    continue;
                }
                if (p.length != 10 || !(p[9].equals("true") || p[9].equals("false"))) {
                    return false;
                }
                Person person;
                switch (p[0]) {
                    case "WORKER":
                        person = new model.Worker(p[1], p[2], p[3], p[4], p[7], p[6], p[8]);
                        break;
                    case "CONTRACTOR":
                        if (!p[8].isEmpty()) {
                            return false;
                        }
                        person = new model.Contractor(p[1], p[2], p[3], p[4], p[7], p[6]);
                        break;
                    case "VISITOR":
                        person = new model.Visitor(p[1], p[2], p[3], p[4], p[6], p[7], p[8]);
                        break;
                    case "STAFF":
                        if (!p[8].isEmpty()) {
                            return false;
                        }
                        person = new model.Staff(p[1], p[2], p[3], p[4], p[7], p[6]);
                        break;
                    default:
                        return false;
                }
                if (!person.getRole().name().equals(p[5])) {
                    return false;
                }
                person.setActive(Boolean.parseBoolean(p[9]));
                staged.save(person);
            }
        } catch (RuntimeException e) {
            return false;
        }
        store.clear();
        store.putAll(staged.store);
        return true;
    }

    /**
     * Serializes current data and requests an atomic replacement of one CSV file.
     */
    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("type,id,code,name,phoneNumber,role,extra1,extra2,extra3,active");
        for (Person p : store.values()) {
            lines.add(p.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }
}
