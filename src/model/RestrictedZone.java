package model;

import exception.AppError;
import java.util.HashSet;
import java.util.Set;
import util.ValidationHelper;

public class RestrictedZone extends Zone {
    private SafetyLevel requiredSafetyLevel;
    private Set<String> allowedPersonCodes;

    public RestrictedZone(String zoneId, String zoneName, SafetyLevel requiredSafetyLevel, Set<String> allowedPersonCodes) {
        super(zoneId, zoneName);
        this.requiredSafetyLevel = validateSafetyLevel(requiredSafetyLevel);
        this.allowedPersonCodes = new HashSet<>(
                ValidationHelper.requireNotNull(allowedPersonCodes, "Danh sách nhân sự được cấp phép không được null.")
        );
    }

    public SafetyLevel getRequiredSafetyLevel() {
        return requiredSafetyLevel;
    }

    public Set<String> getAllowedPersonCodes() {
        return new HashSet<>(allowedPersonCodes);
    }

    public void changeRequiredSafetyLevel(SafetyLevel nextLevel) {
        requiredSafetyLevel = validateSafetyLevel(nextLevel);
    }

    public void grant(Person person) {
        ValidationHelper.requireNotNull(person, "Person cannot be null");
        if (!person.canHoldZoneAccess()) {
            throw new AppError("Visitor cannot be granted access to restricted zone");
        }
        for (String allowed : allowedPersonCodes) {
            if (util.IdHelper.isSame(allowed, person.getCode())) {
                return;
            }
        }
        allowedPersonCodes.add(person.getCode());
    }

    public boolean revoke(Person person) {
        if (person == null) {
            return false;
        }
        String toRemove = null;
        for (String allowed : allowedPersonCodes) {
            if (util.IdHelper.isSame(allowed, person.getCode())) {
                toRemove = allowed;
                break;
            }
        }
        return toRemove != null && allowedPersonCodes.remove(toRemove);
    }

    @Override
    public String accessDenialReason(Person person) {
        if (person == null) {
            return "Person cannot be null";
        }
        if (!person.isActive()) {
            return "Person is inactive";
        }
        boolean listed = false;
        for (String allowed : allowedPersonCodes) {
            if (util.IdHelper.isSame(allowed, person.getCode())) {
                listed = true;
                break;
            }
        }
        if (!listed) {
            return "Person is not on the allowed list for restricted zone " + zoneId;
        }
        if (!person.hasValidSafetyCredential()) {
            return "Missing valid safety credential";
        }
        if (!person.getSafetyLevel().covers(requiredSafetyLevel)) {
            return "Safety level " + person.getSafetyLevel() + " does not meet required " + requiredSafetyLevel;
        }
        return null;
    }

    @Override
    public boolean checkAccess(Person person) {
        return accessDenialReason(person) == null;
    }

    private static SafetyLevel validateSafetyLevel(SafetyLevel level) {
        ValidationHelper.requireNotNull(level, "requiredSafetyLevel", "Choose LOW, MEDIUM, HIGH or CRITICAL.");
        if (level == SafetyLevel.NONE) {
            throw new exception.DataValidationException("requiredSafetyLevel", "Choose LOW, MEDIUM, HIGH or CRITICAL.");
        }
        return level;
    }

    @Override
    public String toCsvLine() {
        String codesStr = (allowedPersonCodes == null || allowedPersonCodes.isEmpty())
                ? "NONE"
                : String.join(";", allowedPersonCodes);
        return util.CsvHelper.toCsvLine("RESTRICTED", zoneId, zoneName, requiredSafetyLevel.name(), codesStr);
    }

    @Override
    public String toString() {
        return String.format("RestrictedZone{%s, requiredSafetyLevel=%s, allowedCount=%d}",
                super.toString(), requiredSafetyLevel, allowedPersonCodes.size());
    }
}
