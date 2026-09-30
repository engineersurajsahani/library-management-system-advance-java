package com.lms.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Password hashing with PBKDF2-HMAC-SHA256 (no external dependencies).
 * Stored format: {@code pbkdf2$<iterations>$<salt-b64>$<hash-b64>}
 */
public final class SecurityUtil {

    private static final int ITERATIONS = 120_000;
    private static final int SALT_LEN = 16;
    private static final int KEY_LEN = 256;

    private SecurityUtil() { }

    public static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LEN];
            random.nextBytes(salt);
            return "pbkdf2$" + ITERATIONS + "$" +
                    Base64.getEncoder().encodeToString(salt) + "$" +
                    Base64.getEncoder().encodeToString(pbkdf2(password, salt, ITERATIONS));
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    public static boolean verifyPassword(String password, String stored) {
        try {
            if (password == null || stored == null) {
                return false;
            }
            String[] parts = stored.split("\\$");
            if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LEN);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return factory.generateSecret(spec).getEncoded();
    }
}
