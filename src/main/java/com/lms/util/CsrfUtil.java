package com.lms.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * CSRF protection using a per-session synchronizer token.
 * Token is embedded in every form and verified on every POST.
 */
public final class CsrfUtil {

    public static final String TOKEN_ATTR = "CSRF_TOKEN";

    private CsrfUtil() { }

    public static String getToken(HttpSession session) {
        String token = (String) session.getAttribute(TOKEN_ATTR);
        if (token == null || token.isBlank()) {
            token = generateToken();
            session.setAttribute(TOKEN_ATTR, token);
        }
        return token;
    }

    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String expected = (String) session.getAttribute(TOKEN_ATTR);
        String provided = request.getParameter("_csrf");
        if (expected == null || provided == null) {
            return false;
        }
        return MessageDigestEqual.equalsSafe(expected, provided);
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64Url.encode(bytes);
    }

    /** Small helpers kept separate to avoid accidental misuse. */
    private static final class MessageDigestEqual {
        static boolean equalsSafe(String a, String b) {
            return java.security.MessageDigest.isEqual(
                    a.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    b.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private static final class Base64Url {
        static String encode(byte[] bytes) {
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }
    }
}
