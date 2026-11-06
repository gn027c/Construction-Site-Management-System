package repository;

import exception.AppError;
import exception.DataValidationException;
import exception.NotFoundException;
import model.Contractor;
import model.Person;
import model.SafetyLevel;
import model.Visitor;
import model.Worker;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PersonRepository {
    private static final String HEADER =
            "type,id,code,name,phoneNumber,position,active,safetyLevel,extra1,extra2,extra3";
    private static final int COLUMN_COUNT = 11;

    private final Map<String, Person> store = new LinkedHashMap<>();

    public boolean save(Person person) throws AppError {
        if (person == null || person.getCode() == null || person.getCode().trim().isEmpty()) {
            return false;
        }
        String code = person.getCode().trim();
        if (findByCode(code) != null) {
            throw new exception.AppError("Personnel code already exists in the system: " + code);
        }
        store.put(code.toUpperCase(), person);
        return true;
    }

    public Person findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        Person direct = store.get(code.trim().toUpperCase());
        if (direct != null) {
            return direct;
        }
        for (Person p : store.values()) {
            if (util.IdHelper.isSame(p.getCode(), code)) {
                return p;
            }
        }
        return null;
    }

    public Person findByCodeOrThrow(String code) throws NotFoundException {
        Person person = findByCode(code);
        if (person == null) {
            throw new exception.NotFoundException("Personnel record", "code", code != null ? code.trim() : "null");
        }
        return person;
    }

    public boolean deleteByCode(String code) throws NotFoundException {
        Person p = findByCodeOrThrow(code);
        return store.remove(p.getCode().toUpperCase()) != null;
    }

    public List<Person> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean loadFromCsv(String filePath) {
        List<Person> people = util.CsvHelper.readTable(filePath, HEADER, COLUMN_COUNT, this::parse, Person::getCode);
        if (people == null) {
            return false;
        }
        store.clear();
        for (Person person : people) {
            store.put(person.getCode().toUpperCase(), person);
        }
        return true;
    }

    private Person parse(List<String> columns) {
        String type = columns.get(0).trim();
        String id = columns.get(1).trim();
        String code = columns.get(2).trim();
        String name = columns.get(3).trim();
        String phone = columns.get(4).trim();
        String active = columns.get(6).trim();
        String first = columns.get(8).trim();
        String second = columns.get(9).trim();
        String third = columns.get(10).trim();

        Person person;
        if (type.equals("WORKER")) {
            person = new Worker(id, code, name, phone, first, second, third);
        } else if (type.equals("CONTRACTOR")) {
            person = new Contractor(id, code, name, phone, second, first);
        } else if (type.equals("VISITOR")) {
            person = new Visitor(id, code, name, phone, first, second, third);
        } else {
            throw new DataValidationException("Unknown person type " + type + ".");
        }
        person.setSafetyLevel(SafetyLevel.valueOf(columns.get(7).trim()));
        person.restorePosition(columns.get(5));
        if (active.equals("false")) {
            person.deactivate();
        } else if (!active.equals("true")) {
            throw new DataValidationException("Active must be true or false.");
        }
        return person;
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Person p : store.values()) {
            lines.add(p.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }

}
