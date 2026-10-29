package model;

import static model.ModelValidation.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a zone requiring explicit permission and a safety credential.
 *
 * <p>Access requires both whitelist membership and a valid credential supplied
 * by the person subtype. The safety level describes the zone hazard; personnel
 * do not currently have a comparable credential level.
 *
 * <p>Responsibility: Le Tan Thien (SE201852) - restricted zones and access rules.
 */
public class RestrictedZone extends Zone {
    public enum SafetyLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    private SafetyLevel requiredSafetyLevel;
    private Set<String> allowedPersonCodes;

    public RestrictedZone(
            String zoneId,
            String zoneName,
            SafetyLevel requiredSafetyLevel,
            Set<String> allowedPersonCodes
    ) {
        super(zoneId, zoneName);
        this.requiredSafetyLevel = requiredValue(requiredSafetyLevel, "requiredSafetyLevel");
        this.allowedPersonCodes = validatedCodes(allowedPersonCodes);
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
        requiredSafetyLevel = requiredValue(level, "requiredSafetyLevel");
    }

    /**
     * Copies the whitelist so callers cannot change it without validation.
     */
    public Set<String> getAllowedPersonCodes() {
        return new HashSet<>(allowedPersonCodes);
    }

    /**
     * Validates every code before replacing the current whitelist.
     */
    public void setAllowedPersonCodes(Set<String> codes) {
        allowedPersonCodes = validatedCodes(codes);
    }

    public boolean grantAccess(String code) {
        return allowedPersonCodes.add(personCode(code, "personCode"));
    }

    public boolean revokeAccess(String code) {
        return allowedPersonCodes.remove(personCode(code, "personCode"));
    }

    /**
     * Requires whitelist membership and the person's subtype-specific safety credential.
     *
     * <p>The hazard level is metadata and is not compared with personnel
     * credentials because those credentials have no comparable level.
     */
    @Override
    public boolean checkAccess(Person person) {
        return person != null && allowedPersonCodes.contains(person.getCode()) && person.hasValidSafetyCredential();
    }

    @Override
    public String toCsvLine() {
        String codes = allowedPersonCodes.isEmpty() ? "NONE" : String.join(";", new TreeSet<>(allowedPersonCodes));
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
