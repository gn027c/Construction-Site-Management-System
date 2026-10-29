package view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import model.Contractor;
import model.Person;
import model.Staff;
import model.Visitor;
import model.Worker;

/**
 * Renders personnel fields consistently for searchable lists and detail screens.
 * Personnel types describe people independently of account permissions.
 */
public final class PersonnelView {
    private PersonnelView() {
    }

    public static String formatSummary(Person person) {
        StringJoiner summary = new StringJoiner(" | ");
        for (Map.Entry<String, String> field : fields(person).entrySet()) {
            summary.add(field.getKey() + ": " + field.getValue());
        }
        return summary.toString();
    }

    public static void showDetails(Person person) {
        ConsoleTheme.title("PERSONNEL DETAILS");
        for (Map.Entry<String, String> field : fields(person).entrySet()) {
            System.out.println(field.getKey() + ": " + field.getValue());
        }
    }

    /**
     * Defines common and subtype fields once so lists and details show the same information.
     */
    private static Map<String, String> fields(Person person) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("ID", person.getId());
        fields.put("Code", person.getCode());
        fields.put("Name", person.getName());
        fields.put("Phone", person.getPhoneNumber());
        fields.put("Personnel type", person.getRole().name());
        fields.put("Status", person.isActive() ? "Active" : "Inactive");
        if (person instanceof Worker) {
            Worker worker = (Worker) person;
            fields.put("Trade", worker.getTrade());
            fields.put("Safety certificate", worker.getSafetyCertId());
            fields.put("Assigned team", worker.getAssignedTeam());
        } else if (person instanceof Staff) {
            Staff staff = (Staff) person;
            fields.put("Position", staff.getPosition());
            fields.put("Safety certificate", displayOptional(staff.getSafetyCertId()));
        } else if (person instanceof Contractor) {
            Contractor contractor = (Contractor) person;
            fields.put("Company", contractor.getCompanyName());
            fields.put("Contract", contractor.getContractId());
        } else if (person instanceof Visitor) {
            Visitor visitor = (Visitor) person;
            fields.put("Visit purpose", visitor.getPurpose());
            fields.put("Host", visitor.getHostPersonName());
            fields.put("Entry date", displayOptional(visitor.getEntryDate()));
        }
        fields.put("Valid safety credential", person.hasValidSafetyCredential() ? "Yes" : "No");
        return fields;
    }

    private static String displayOptional(String value) {
        return value.isEmpty() ? "Not provided" : value;
    }
}
