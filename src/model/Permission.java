package model;

/**
 * Identifies application operations that can be granted to a login account.
 * These permissions are independent of physical zone access rules.
 */
public enum Permission {
    MANAGE_PERSONNEL,
    MANAGE_ZONES,
    RECORD_ATTENDANCE,
    VIEW_ATTENDANCE,
    REPORT_INCIDENTS,
    VIEW_INCIDENTS,
    ASSIGN_INCIDENTS,
    RESOLVE_INCIDENTS,
    PURGE_INCIDENTS,
    EXPORT_REPORTS,
    RELOAD_DATA,
    MANAGE_ACCOUNTS
}
