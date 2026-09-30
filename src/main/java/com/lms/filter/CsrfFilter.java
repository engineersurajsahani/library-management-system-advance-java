package com.lms.filter;

import com.lms.util.CsrfUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** Rejects POST requests without a valid CSRF token. */
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            // AJAX endpoints send the token as a header or form field
            HttpSession session = request.getSession(false);
            String expected = session == null ? null : (String) session.getAttribute(CsrfUtil.TOKEN_ATTR);
            String provided = request.getHeader("X-CSRF-TOKEN");
            if (provided == null) {
                provided = request.getParameter("_csrf");
            }
            boolean ok = expected != null && provided != null && java.security.MessageDigest.isEqual(
                    expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    provided.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            if (!ok) {
                // Allow logout via POST without token? No — always require. Send 403.
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
