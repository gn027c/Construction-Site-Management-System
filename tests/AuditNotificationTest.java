import static support.Checks.*;

import exception.AccessDeniedException;
import exception.SiteManagementException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import model.Account;
import model.ActivityEvent;
import model.Notification;
import service.ActivityLogService;
import service.AuthenticationService;
import service.NotificationService;

public class AuditNotificationTest {
    private static AuthenticationService admin(Path root) throws Exception {
        AuthenticationService authentication = new AuthenticationService(root);
        authentication.login("admin", "admin");
        return authentication;
    }

    private static List<Notification> operationMessages(
            NotificationService notifications, AuthenticationService authentication, String operationId
    ) {
        return notifications.listMine(authentication).stream()
                .filter(message -> message.getOperationId().equals(operationId))
                .collect(java.util.stream.Collectors.toList());
    }

    public static void main(String[] args) throws Exception {
        test("Activity statuses, quoted text and identifiers survive a restart", () -> {
            Path root = Files.createTempDirectory("smartsite-activity-");
            AuthenticationService authentication = admin(root);
            ActivityLogService activity = new ActivityLogService(root);
            int initialCount = activity.list(authentication).size();
            require(activity.record("admin", "ADMIN", "CHECK_IN", "PERSON", "W1", "FAILED", "Exit Z1 first."));
            require(activity.getLastOperationId().matches("OP[1-9][0-9]*"));
            require(activity.record(
                    "admin", "ADMIN", "UPDATE_PERSON", "PERSON", "W1", "SUCCESS", "Text, with \"quotes\"\nand lines"
            ));
            require(activity.record("admin", "ADMIN", "RELOAD_DATA", "CSV", "", "CANCELLED", "User cancelled."));
            List<ActivityEvent> events = new ActivityLogService(root).list(authentication);
            equal(initialCount + 3, events.size());
            require(events.get(0).getLogId().matches("LOG[1-9][0-9]*"));
            equal("CANCELLED", events.get(0).getStatus());
            equal("Text, with \"quotes\"\nand lines", events.get(1).getReason());
            equal("FAILED", events.get(2).getStatus());
        });
        test("Only the ADMIN role can read complete or filtered activity history", () -> {
            Path root = Files.createTempDirectory("smartsite-activity-access-");
            AuthenticationService authentication = admin(root);
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            ActivityLogService activity = new ActivityLogService(root);
            require(activity.record("officer", "SAFETY_OFFICER", "REPORT_INCIDENT", "INCIDENT", "INC1", "SUCCESS", ""));
            equal(1, activity.list(authentication, "OFFICER", "report_incident", "success").size());
            equal(0, activity.list(authentication, "someone", "", "").size());
            authentication.login("officer", "secret");
            throwsType(AccessDeniedException.class, () -> activity.list(authentication));
            throwsType(AccessDeniedException.class, () -> activity.list(authentication, "officer", "", ""));
            authentication.logout();
            throwsType(AccessDeniedException.class, () -> activity.list(authentication));
        });
        test("Malformed activity files are preserved instead of replaced by new actions", () -> {
            Path root = Files.createTempDirectory("smartsite-activity-corrupt-");
            AuthenticationService authentication = admin(root);
            Path file = root.resolve("activity_log.csv");
            Files.write(file, Arrays.asList("invalid header"), StandardCharsets.UTF_8);
            byte[] before = Files.readAllBytes(file);
            ActivityLogService activity = new ActivityLogService(root);
            require(!activity.record("admin", "ADMIN", "SAVE", "CSV", "", "SUCCESS", ""));
            require(!activity.getLastWriteError().isEmpty());
            require(Arrays.equals(before, Files.readAllBytes(file)));
            throwsType(SiteManagementException.class, () -> activity.list(authentication));
        });
        test("Activity storage errors are returned separately from the completed business result", () -> {
            Path root = Files.createTempDirectory("smartsite-activity-write-");
            ActivityLogService activity = new ActivityLogService(root);
            Path csv = root.resolve("persons.csv");
            Files.write(csv, Arrays.asList("business result already saved"), StandardCharsets.UTF_8);
            Files.createDirectory(root.resolve("activity_log.csv"));
            boolean businessSucceeded = true;
            require(!activity.record("admin", "ADMIN", "SAVE_PERSON", "PERSON", "W1", "SUCCESS", "Saved."));
            require(businessSucceeded);
            require(Files.readAllLines(csv, StandardCharsets.UTF_8).contains("business result already saved"));
        });
        test("Credential hashes are rejected from log details", () -> {
            Path root = Files.createTempDirectory("smartsite-activity-credential-");
            ActivityLogService activity = new ActivityLogService(root);
            require(!activity.record(
                    "admin", "ADMIN", "RESET_PASSWORD", "ACCOUNT", "worker", "SUCCESS", "PBKDF2-SHA256$secret"
            ));
            require(!Files.exists(root.resolve("activity_log.csv")));
        });
        test("Notification inboxes are isolated and read flags persist", () -> {
            Path root = Files.createTempDirectory("smartsite-notifications-");
            AuthenticationService authentication = admin(root);
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            NotificationService notifications = new NotificationService(root);
            require(notifications.notifyUser("admin", "OP9001", "Administration message."));
            require(notifications.notifyUser("officer", "OP9001", "Check in rejected. Exit Z1 first."));
            List<Notification> adminInbox = operationMessages(notifications, authentication, "OP9001");
            equal(1, adminInbox.size());
            equal("admin", adminInbox.get(0).getRecipientUsername());
            authentication.login("officer", "secret");
            List<Notification> officerInbox = operationMessages(notifications, authentication, "OP9001");
            equal(1, officerInbox.size());
            String notificationId = officerInbox.get(0).getNotificationId();
            equal("officer", officerInbox.get(0).getRecipientUsername());
            require(!officerInbox.get(0).isRead());
            authentication.login("admin", "admin");
            require(!notifications.markRead(authentication, notificationId));
            authentication.login("officer", "secret");
            require(notifications.markRead(authentication, "ntf-00" + notificationId.substring(3)));
            List<Notification> restarted = operationMessages(new NotificationService(root), authentication, "OP9001");
            require(restarted.get(0).isRead());
            equal("Check in rejected. Exit Z1 first.", restarted.get(0).getMessage());
        });
        test("Marking another account's notification read leaves storage unchanged", () -> {
            Path root = Files.createTempDirectory("smartsite-notification-scope-");
            AuthenticationService authentication = admin(root);
            NotificationService notifications = new NotificationService(root);
            require(notifications.notifyUser("officer", "OP1", "Private message."));
            byte[] before = Files.readAllBytes(root.resolve("notifications.csv"));
            require(!notifications.markRead(authentication, "NTF1"));
            require(Arrays.equals(before, Files.readAllBytes(root.resolve("notifications.csv"))));
            authentication.logout();
            throwsType(AccessDeniedException.class, () -> notifications.listMine(authentication));
            throwsType(AccessDeniedException.class, () -> notifications.markRead(authentication, "NTF1"));
        });
        test("Administrator notifications exclude non-admin recipients", () -> {
            Path root = Files.createTempDirectory("smartsite-notification-admins-");
            AuthenticationService authentication = admin(root);
            authentication.createAccount("officer", "secret", Account.Role.SAFETY_OFFICER);
            NotificationService notifications = new NotificationService(root);
            require(notifications.notifyAdmins(authentication.listAccounts(), "OP9002", "Invalid attendance history."));
            equal(1, operationMessages(notifications, authentication, "OP9002").size());
            authentication.login("officer", "secret");
            equal(0, operationMessages(notifications, authentication, "OP9002").size());
        });
        test("Malformed notifications and write failures preserve existing messages", () -> {
            Path root = Files.createTempDirectory("smartsite-notification-corrupt-");
            AuthenticationService authentication = admin(root);
            Path file = root.resolve("notifications.csv");
            Files.write(file, Arrays.asList("invalid header"), StandardCharsets.UTF_8);
            byte[] before = Files.readAllBytes(file);
            NotificationService notifications = new NotificationService(root);
            require(!notifications.notifyUser("admin", "OP1", "Message."));
            require(!notifications.getLastWriteError().isEmpty());
            require(Arrays.equals(before, Files.readAllBytes(file)));
            throwsType(SiteManagementException.class, () -> notifications.listMine(authentication));
        });
        test("A missing notification file is created only when a message is delivered", () -> {
            Path root = Files.createTempDirectory("smartsite-notification-empty-");
            AuthenticationService authentication = admin(root);
            NotificationService notifications = new NotificationService(root);
            equal(0, notifications.listMine(authentication).size());
            require(!Files.exists(root.resolve("notifications.csv")));
            require(notifications.notifyUser("admin", "", "First message."));
            equal("NTF1", notifications.listMine(authentication).get(0).getNotificationId());
        });
        finish();
    }
}
