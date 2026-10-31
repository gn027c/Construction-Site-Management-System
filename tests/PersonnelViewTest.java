import static support.Checks.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import model.person.Contractor;
import model.person.Person;
import model.person.Staff;
import model.person.Visitor;
import model.person.Worker;
import service.PersonnelService;
import util.IdentifierHelper;
import view.ListView;
import view.PersonnelView;

public class PersonnelViewTest {
    private static void checkRendering(Person person, String... expectedFields) throws Exception {
        String summary = PersonnelView.formatSummary(person);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        try (PrintStream output = new PrintStream(bytes, true, "UTF-8")) {
            System.setOut(output);
            PersonnelView.showDetails(person);
        } finally {
            System.setOut(previous);
        }
        String details = bytes.toString("UTF-8");
        require(details.contains("PERSONNEL DETAILS"));
        for (String field : expectedFields) {
            require(summary.contains(field));
            require(details.contains(field));
        }
        for (String field : summary.split(" \\| ")) {
            require(details.contains(field));
        }
    }

    public static void main(String[] args) throws Exception {
        test("Worker list and details include contact, status and all worker fields", () -> {
            Person worker = new Worker("P1", "W1", "Worker A", "0901234567", "Welder", "X_SAFE", "Team B");
            checkRendering(
                    worker,
                    "ID: P1", "Code: W1", "Name: Worker A", "Phone: 0901234567",
                    "Personnel type: WORKER", "Status: Active", "Trade: Welder",
                    "Safety certificate: X_SAFE", "Assigned team: Team B", "Valid safety credential: Yes"
            );
        });
        test("Staff details include position, certificate and inactive status", () -> {
            Person staff = new Staff("P2", "S2", "Staff B", "0901234567", "Planner", "S_SAFE");
            staff.setActive(false);
            checkRendering(
                    staff,
                    "Personnel type: STAFF", "Position: Planner", "Safety certificate: S_SAFE",
                    "Status: Inactive", "Valid safety credential: Yes"
            );
        });
        test("Contractor details include company and contract", () -> {
            checkRendering(
                    new Contractor("P3", "C3", "Contractor C", "0901234567", "Builder Co", "CTR-2026"),
                    "Personnel type: CONTRACTOR", "Company: Builder Co", "Contract: CTR-2026",
                    "Valid safety credential: Yes"
            );
        });
        test("Visitor details include purpose, host and date", () -> {
            checkRendering(
                    new Visitor("P4", "V4", "Visitor D", "0901234567", "Inspection", "Host A", "29/10/2026"),
                    "Personnel type: VISITOR", "Visit purpose: Inspection", "Host: Host A",
                    "Entry date: 29/10/2026", "Valid safety credential: No"
            );
        });
        test("Missing optional personnel information stays visible", () -> {
            checkRendering(
                    new Staff("P5", "S5", "Staff E", "0901234567", "Planner", ""),
                    "Safety certificate: Not provided", "Valid safety credential: No"
            );
            checkRendering(
                    new Visitor("P6", "V6", "Visitor F", "0901234567", "Meeting", "Host B", ""),
                    "Entry date: Not provided"
            );
        });
        test("Personnel search includes subtype fields, canonical aliases and exact status", () -> {
            Person active = new Staff("P1", "S1", "Staff A", "0901234567", "Planner", "");
            Person inactive = new Staff("P2", "S2", "Staff B", "0901234567", "Planner", "");
            inactive.setActive(false);
            List<Person> people = Arrays.asList(inactive, active);
            Comparator<Person> order = Comparator.comparing(Person::getCode, IdentifierHelper.NATURAL_ORDER);
            equal(
                    Arrays.asList(active),
                    ListView.filterAndSort(people, "s01 active planner", PersonnelView::formatSummary, order)
            );
            equal(
                    Arrays.asList(inactive),
                    ListView.filterAndSort(people, "inactive planner", PersonnelView::formatSummary, order)
            );
        });
        test("Personnel console renders Staff in lists and details and persists contact edits", () -> {
            Path root = Files.createTempDirectory("smartsite-personnel-view-");
            String output = ConsoleTest.launch(
                    root,
                    "1\n1\n4\nStaff A\n0901234567\nPlanner\n\n"
                            + "2\nf\ns01 active planner\ne\ns01\nUpdated Staff\n\n0\n0\n",
                    0,
                    false
            );
            require(output.contains("Results: 1 | Filter: s01 active planner"));
            require(output.contains("PERSONNEL DETAILS"));
            require(output.contains("Position: Planner"));
            require(output.contains("Safety certificate: Not provided"));
            require(output.contains("Status: Active"));
            PersonnelService service = new PersonnelService();
            require(service.loadFromCsv(root.resolve("persons.csv").toString()));
            equal("Updated Staff", service.findPersonByCode("S1").getName());
            equal("0901234567", service.findPersonByCode("S1").getPhoneNumber());
        });
        finish();
    }
}
