package model.common;

public enum SafetyLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public boolean covers(SafetyLevel required) {
        return compareTo(required) >= 0;
    }
}
