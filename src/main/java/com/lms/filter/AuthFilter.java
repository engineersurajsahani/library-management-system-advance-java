package com.lms.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Role-based access control.
 * <p>
 * web.xml maps this filter to protected URL patterns with init-params:
 * roles = comma separated allowed roles (ADMIN, STUDENT)
 */
public class AuthFilter implements Filter {

    private String[] roles = new String[0];

    @Override
    public void init(jakarta.servlet.FilterConfig config) throws ServletException {
        String r = config.getInitParameter("roles");
        if (r != null && !r.isBlank()) {
            roles = r.split("\\s*,\\s*");
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        String role = session == null ? null : (String) session.getAttribute("role");

        if (role == null) {
            // Not logged in — bounce to login with a friendly message
            response.sendRedirect(request.getContextPath() + "/login?next=" +
                    java.net.URLEncoder.encode(request.getRequestURI().substring(request.getContextPath().length()),
                            java.nio.charset.StandardCharsets.UTF_8));
            return;
        }

        boolean allowed = roles.length == 0;
        for (String r : roles) {
            if (r.trim().equalsIgnoreCase(role)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have access to this page");
            return;
        }
        chain.doFilter(req, res);
    }
}
