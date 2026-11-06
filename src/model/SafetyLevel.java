package model;

public enum SafetyLevel {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int rank;

    SafetyLevel(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public boolean covers(SafetyLevel required) {
        if (required == null) {
            return true;
        }
        return this.rank >= required.rank;
    }
}
