package view;

import model.AttendanceRecord;

/**
 * Explains evidence-based attendance repair and displays the original record before editing.
 */
public final class AttendanceReviewView {
    private AttendanceReviewView() {
    }

    public static void showGuide() {
        System.out.println("  REVIEW STEPS");
        System.out.println("  1. Scan issues, then inspect that person's timeline with option 3.");
        System.out.println("  2. Verify events against a reliable register or the data owner's records.");
        System.out.println("  3. Correct a confirmed entry with option 2, then scan again.");
        System.out.println();
        System.out.println("  Rule: IN zone A -> OUT zone A -> next IN. Times must not go backwards.");
        System.out.println("  A mismatch is a data inconsistency; it does not prove misconduct.");
        System.out.println("  Missing/duplicate events need reconciliation; do not invent times or evidence.");
        System.out.println();
    }

    public static String formatRecord(AttendanceRecord record) {
        return record.getRecordId() + " | " + record.getPersonCode() + " | " + record.getTimestamp()
                + " | " + record.getCheckType() + " | " + record.getZoneId();
    }

    public static void showOriginal(AttendanceRecord record) {
        ConsoleTheme.title("ORIGINAL ATTENDANCE RECORD");
        System.out.println(formatRecord(record));
        System.out.println("Press Enter to keep zone/time; choose 0 to keep the action.");
        System.out.println("This edits an existing event. It cannot restore a missing event.");
    }
}
