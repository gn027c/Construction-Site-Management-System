package service;

import exception.AccessDeniedException;
import exception.SiteManagementException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Account;
import model.ActivityEvent;
import repository.ActivityLogRepository;

/**
 * Records sanitized application actions without changing their business result when logging fails.
 * Only the ADMIN role can retrieve activity history, regardless of custom permission grants.
 */
public final class ActivityLogService {
    private final ActivityLogRepository repository;
    private String lastOperationId = "";

    public ActivityLogService(Path directory) {
        repository = new ActivityLogRepository(directory.resolve("activity_log.csv"));
    }

    public synchronized boolean record(
            String actorUsername, String actorRole, String action,
            String targetType, String targetId, String status, String reason
    ) {
        ActivityEvent event = repository.record(
                actorUsername, actorRole, action, targetType, targetId, status, reason
        );
        lastOperationId = event == null ? "" : event.getOperationId();
        return event != null;
    }

    public List<ActivityEvent> list(AuthenticationService authentication) {
        return list(authentication, "", "", "");
    }

    /**
     * Returns newest records first after enforcing administrator access and optional text filters.
     */
    public List<ActivityEvent> list(
            AuthenticationService authentication, String actor, String action, String status
    ) {
        if (authentication.currentAccount().getRole() != Account.Role.ADMIN) {
            throw new AccessDeniedException("only ADMIN can view activity logs");
        }
        if (!repository.reload()) {
            throw new SiteManagementException(repository.getLastError());
        }
        List<ActivityEvent> result = new ArrayList<>();
        for (ActivityEvent event : repository.findAll()) {
            if (matches(event.getActorUsername(), actor)
                    && matches(event.getAction(), action)
                    && matches(event.getStatus(), status)) {
                result.add(event);
            }
        }
        Collections.reverse(result);
        return result;
    }

    private static boolean matches(String actual, String expected) {
        return expected == null || expected.trim().isEmpty() || actual.equalsIgnoreCase(expected.trim());
    }

    public String getLastWriteError() {
        return repository.getLastError();
    }

    public String getLastOperationId() {
        return lastOperationId;
    }
}
