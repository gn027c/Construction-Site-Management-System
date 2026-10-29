import static support.Checks.*;

import exception.AuthenticationException;
import exception.DataValidationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.Account;
import model.ActivityEvent;
import model.Notification;
import service.AuthenticationService;

public class AccountAuditTest {
    private static AuthenticationService admin(Path root) throws Exception {
        AuthenticationService authentication = new AuthenticationService(root);
        authentication.login("admin", "admin");
        return authentication;
    }

    private static ActivityEvent event(AuthenticationService authentication, String action, String status) {
        for (ActivityEvent event : authentication.getActivityLog().list(authentication)) {
            if (event.getAction().equals(action) && event.getStatus().equals(status)) {
                return event;
            }
        }
        throw new AssertionError("Missing " + action + " / " + status);
    }

    public static void main(String[] args) throws Exception {
        test("Rejected account changes are audited without password values", () -> {
            AuthenticationService authentication = admin(Files.createTempDirectory("smartsite-account-audit-"));
            throwsType(DataValidationException.class, () -> authentication.createAccount(
                    "admin", "never-log-this-password", Account.Role.SITE_MANAGER
            ));
            ActivityEvent rejected = event(authentication, "CREATE_ACCOUNT", "FAILED");
            equal("admin", rejected.getActorUsername());
            equal("admin", rejected.getTargetId());
            require(!rejected.toCsvLine().contains("never-log-this-password"));
            throwsType(AuthenticationException.class, () -> authentication.changePassword(
                    "another-secret", "new-secret"
            ));
            ActivityEvent password = event(authentication, "CHANGE_PASSWORD", "FAILED");
            require(!password.toCsvLine().contains("another-secret"));
            require(!password.toCsvLine().contains("new-secret"));
        });
        test("Successful account changes notify the actor and retain the log operation identifier", () -> {
            AuthenticationService authentication = admin(Files.createTempDirectory("smartsite-account-notice-"));
            authentication.createAccount("manager", "secret", Account.Role.SITE_MANAGER);
            ActivityEvent created = event(authentication, "CREATE_ACCOUNT", "SUCCESS");
            List<Notification> notifications = authentication.getNotifications().listMine(authentication);
            boolean matched = false;
            for (Notification notification : notifications) {
                if (notification.getOperationId().equals(created.getOperationId())
                        && notification.getMessage().contains("CREATE_ACCOUNT")) {
                    matched = true;
                }
            }
            require(matched);
            authentication.login("manager", "secret");
            boolean notified = false;
            for (Notification notification : authentication.getNotifications().listMine(authentication)) {
                notified |= notification.getOperationId().equals(created.getOperationId());
            }
            require(notified);
        });
        test("Role changes retain the actor role used to authorize the operation", () -> {
            AuthenticationService authentication = admin(Files.createTempDirectory("smartsite-role-audit-"));
            authentication.createAccount("second-admin", "secret", Account.Role.ADMIN);
            authentication.changeRole("admin", Account.Role.SITE_MANAGER);
            authentication.login("second-admin", "secret");
            ActivityEvent changed = event(authentication, "CHANGE_ROLE", "SUCCESS");
            equal("admin", changed.getActorUsername());
            equal("ADMIN", changed.getActorRole());
            equal("admin", changed.getTargetId());
        });
        test("Failed activity storage does not undo a saved password change", () -> {
            Path root = Files.createTempDirectory("smartsite-account-log-failure-");
            AuthenticationService authentication = admin(root);
            Path activity = root.resolve("activity_log.csv");
            Path saved = root.resolve("saved-activity.csv");
            Files.move(activity, saved);
            Files.createDirectory(activity);
            try {
                authentication.resetPassword("admin", "replacement");
                require(!authentication.getActivityLog().getLastWriteError().isEmpty());
                new AuthenticationService(root).login("admin", "replacement");
            } finally {
                Files.delete(activity);
                Files.move(saved, activity);
            }
        });
        finish();
    }
}
