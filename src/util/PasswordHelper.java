package util;

import exception.DataValidationException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashes passwords using PBKDF2-HMAC-SHA256 with an independent random salt.
 * Stored values contain the algorithm, iteration count, salt and derived key.
 */
public final class PasswordHelper {
    private static final int ITERATIONS = 600000;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIX = "PBKDF2-SHA256$" + ITERATIONS + "$";

    private PasswordHelper() {
    }

    public static String hash(String password) {
        if (password == null || password.isEmpty() || password.length() > 128) {
            throw new DataValidationException("password", "Must contain between 1 and 128 characters.");
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] key = derive(password, salt);
        return PREFIX + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(key);
    }

    public static boolean isValidHash(String encoded) {
        if (encoded == null || !encoded.startsWith(PREFIX)) {
            return false;
        }
        String[] parts = encoded.split("\\$", -1);
        try {
            return parts.length == 4
                    && Base64.getDecoder().decode(parts[2]).length == 16
                    && Base64.getDecoder().decode(parts[3]).length == 32;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean verify(String password, String encoded) {
        if (password == null || password.isEmpty() || password.length() > 128 || !isValidHash(encoded)) {
            return false;
        }
        String[] parts = encoded.split("\\$", -1);
        byte[] actual = derive(password, Base64.getDecoder().decode(parts[2]));
        return MessageDigest.isEqual(Base64.getDecoder().decode(parts[3]), actual);
    }

    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
