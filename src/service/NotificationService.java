package service;

import exception.SiteManagementException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Account;
import model.Notification;
import repository.NotificationRepository;

/**
 * Delivers persistent console messages while exposing only the signed-in account's inbox.
 */
public final class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(Path directory) {
        repository = new NotificationRepository(directory.resolve("notifications.csv"));
    }

    public boolean notifyUser(String username, String operationId, String message) {
        return repository.add(username, operationId, message);
    }

    public boolean notifyAdmins(List<Account> accounts, String operationId, String message) {
        boolean saved = true;
        for (Account account : accounts) {
            if (account.getRole() == Account.Role.ADMIN && account.isEnabled()) {
                saved = notifyUser(account.getUsername(), operationId, message) && saved;
            }
        }
        return saved;
    }

    public List<Notification> listMine(AuthenticationService authentication) {
        String username = authentication.currentAccount().getUsername();
        if (!repository.reload()) {
            throw new SiteManagementException(repository.getLastError());
        }
        List<Notification> result = new ArrayList<>();
        for (Notification notification : repository.findAll()) {
            if (notification.getRecipientUsername().equals(username)) {
                result.add(notification);
            }
        }
        Collections.reverse(result);
        return result;
    }

    public boolean markRead(AuthenticationService authentication, String notificationId) {
        String username = authentication.currentAccount().getUsername();
        String normalized = normalizeId(notificationId);
        return repository.markRead(username, normalized);
    }

    private static String normalizeId(String id) {
        if (id == null || !id.trim().matches("(?i)NTF-?0*[1-9][0-9]*")) {
            return "";
        }
        return "NTF" + id.trim().substring(3).replaceFirst("^-", "").replaceFirst("^0+", "");
    }

    public String getLastWriteError() {
        return repository.getLastError();
    }
}
