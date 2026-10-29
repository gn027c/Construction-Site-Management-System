package model;

/**
 * Lists personnel role labels stored in personnel records.
 *
 * <p>A role label alone grants no access. Zone access is decided by
 * {@link Zone#checkAccess(Person)} and the person subtype's credential rule.
 */
public enum Role {
    WORKER,
    CONTRACTOR,
    VISITOR,
    SAFETY_OFFICER,
    SITE_MANAGER
}
