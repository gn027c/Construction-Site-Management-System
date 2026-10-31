package view;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import model.zone.RestrictedZone;
import model.zone.Zone;

/**
 * Formats site zone fields consistently for searchable lists and detail screens.
 */
public final class ZoneView {
    public static final List<String> TABLE_HEADERS = Arrays.asList(
            "Zone ID", "Name", "Type", "Required Safety", "Authorized Personnel"
    );

    private ZoneView() {
    }

    public static List<String> tableRow(Zone zone) {
        if (zone instanceof RestrictedZone) {
            RestrictedZone r = (RestrictedZone) zone;
            return Arrays.asList(
                    r.getZoneId(),
                    r.getZoneName(),
                    "RESTRICTED",
                    r.getRequiredSafetyLevel().name(),
                    r.getAllowedPersonCodes().size() + " people"
            );
        }
        return Arrays.asList(
                zone.getZoneId(),
                zone.getZoneName(),
                "REGULAR",
                "-",
                "All"
        );
    }

    public static String formatSummary(Zone zone) {
        StringJoiner summary = new StringJoiner(" | ");
        for (Map.Entry<String, String> field : fields(zone).entrySet()) {
            summary.add(field.getKey() + ": " + field.getValue());
        }
        return summary.toString();
    }

    public static void showDetails(Zone zone) {
        ConsoleTheme.title("ZONE DETAILS");
        for (Map.Entry<String, String> field : fields(zone).entrySet()) {
            System.out.println(field.getKey() + ": " + field.getValue());
        }
    }

    private static Map<String, String> fields(Zone zone) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Zone ID", zone.getZoneId());
        fields.put("Name", zone.getZoneName());
        if (zone instanceof RestrictedZone) {
            RestrictedZone restricted = (RestrictedZone) zone;
            fields.put("Type", "RESTRICTED");
            fields.put("Required safety", restricted.getRequiredSafetyLevel().name());
            fields.put("Authorized personnel", String.valueOf(restricted.getAllowedPersonCodes().size()));
        } else {
            fields.put("Type", "REGULAR");
        }
        return fields;
    }
}

