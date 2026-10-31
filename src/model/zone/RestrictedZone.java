package model.zone;

import static model.common.ModelValidation.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import model.common.SafetyLevel;
import model.person.Person;

public class RestrictedZone extends Zone {
    private SafetyLevel requiredSafetyLevel;
    private Set<String> allowedPersonCodes;

    public RestrictedZone(
            String zoneId,
            String zoneName,
            SafetyLevel requiredSafetyLevel,
            Set<String> allowedPersonCodes
    ) {
        super(zoneId, zoneName);
        this.requiredSafetyLevel = zoneLevel(requiredSafetyLevel);
        this.allowedPersonCodes = validatedCodes(allowedPersonCodes);
    }

    private static SafetyLevel zoneLevel(SafetyLevel level) {
        requiredValue(level, "requiredSafetyLevel");
        if (level == SafetyLevel.NONE) {
            throw new exception.DataValidationException(
                    "requiredSafetyLevel", "A restricted zone requires LOW, MEDIUM, HIGH or CRITICAL."
            );
        }
        return level;
    }

    private static Set<String> validatedCodes(Set<String> codes) {
        requiredValue(codes, "allowedPersonCodes");
        Set<String> normalized = new HashSet<>();
        for (String code : codes) {
            normalized.add(personCode(code, "allowedPersonCodes"));
        }
        return normalized;
    }

    public SafetyLevel getRequiredSafetyLevel() {
        return requiredSafetyLevel;
    }

    public void setRequiredSafetyLevel(SafetyLevel level) {
        requiredSafetyLevel = zoneLevel(level);
    }

    public Set<String> getAllowedPersonCodes() {
        return new HashSet<>(allowedPersonCodes);
    }

    public void setAllowedPersonCodes(Set<String> codes) {
        allowedPersonCodes = validatedCodes(codes);
    }

    public boolean grantAccess(String code) {
        return allowedPersonCodes.add(personCode(code, "personCode"));
    }

    public boolean revokeAccess(String code) {
        return allowedPersonCodes.remove(personCode(code, "personCode"));
    }

    @Override
    public String accessDenialReason(Person person) {
        if (person == null) {
            return "No person was given.";
        }
        if (!allowedPersonCodes.contains(person.getCode())) {
            return "Not on the allowed list for this zone.";
        }
        if (!person.hasValidSafetyCredential()) {
            return "No valid safety credential.";
        }
        if (!person.getSafetyLevel().covers(requiredSafetyLevel)) {
            return "Safety level " + person.getSafetyLevel() + " is below the required " + requiredSafetyLevel + ".";
        }
        return "";
    }

    @Override
    public String toCsvLine() {
        Set<String> orderedCodes = new TreeSet<>(util.IdentifierHelper.NATURAL_ORDER);
        orderedCodes.addAll(allowedPersonCodes);
        String codes = orderedCodes.isEmpty() ? "NONE" : String.join(";", orderedCodes);
        return util.CsvHelper.toCsvLine("RESTRICTED", getZoneId(), getZoneName(), requiredSafetyLevel.name(), codes);
    }

    @Override
    public String toString() {
        return String.format(
                "RestrictedZone{%s, requiredSafetyLevel=%s, allowedCount=%d}",
                super.toString(),
                requiredSafetyLevel,
                allowedPersonCodes.size()
        );
    }
}
