package model.common;

/**
 * Orders safety clearance from none to critical.
 *
 * <p>A restricted zone requires a level from {@code LOW} to {@code CRITICAL}. A person
 * holds a level; {@code NONE} means the person may not enter any restricted zone.
 * A higher level also covers every lower one, so {@code HIGH} may enter
 * {@code LOW}, {@code MEDIUM} and {@code HIGH} zones.
 */
public enum SafetyLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    /**
     * Reports whether this level is at least the required level.
     */
    public boolean covers(SafetyLevel required) {
        return compareTo(required) >= 0;
    }
}
