package com.lms.servlet;

import com.lms.dao.StudentDAO;
import com.lms.dao.UserDAO;
import com.lms.model.Student;
import com.lms.model.User;
import com.lms.util.CsrfUtil;
import com.lms.util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Handles login with rate limiting, session fixation protection and role routing. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    // Simple in-memory rate limiter: max 10 failed attempts / 10 min / IP
    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 10 * 60 * 1000L;
    private final Map<String, AtomicInteger> failures = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            redirectByRole(resp, session, req.getContextPath());
            return;
        }
        req.setAttribute("error", req.getParameter("error"));
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        String ip = req.getRemoteAddr();
        if (isRateLimited(ip)) {
            req.setAttribute("error", "Too many attempts. Please try again after some time.");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        try {
            User user = userDAO.findByUsername(username == null ? "" : username.trim());
            if (user == null || !SecurityUtil.verifyPassword(password, user.getPasswordHash())) {
                recordFailure(ip);
                req.setAttribute("error", "Invalid username or password");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            if ("STUDENT".equals(user.getRole())) {
                Student student = studentDAO.findByUserId(user.getId());
                if (student == null || !student.isActive()) {
                    recordFailure(ip);
                    req.setAttribute("error", "Your account is inactive. Contact the librarian.");
                    req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                    return;
                }
            }

            // Success: prevent session fixation
            HttpSession old = req.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            CsrfUtil.getToken(session); // pre-generate

            clearFailures(ip);

            // Support ?next= only for same-app relative paths (open-redirect guard)
            String next = req.getParameter("next");
            if (next != null && !next.isBlank() && next.startsWith("/") && !next.startsWith("//")) {
                resp.sendRedirect(req.getContextPath() + next);
            } else {
                redirectByRole(resp, session, req.getContextPath());
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void redirectByRole(HttpServletResponse resp, HttpSession session, String ctx) throws IOException {
        if ("ADMIN".equals(session.getAttribute("role"))) {
            resp.sendRedirect(ctx + "/librarian/dashboard");
        } else {
            resp.sendRedirect(ctx + "/student/dashboard");
        }
    }

    private synchronized boolean isRateLimited(String ip) {
        Long start = windowStart.get(ip);
        long now = System.currentTimeMillis();
        if (start == null || now - start > WINDOW_MS) {
            return false;
        }
        AtomicInteger count = failures.get(ip);
        return count != null && count.get() >= MAX_ATTEMPTS;
    }

    private synchronized void recordFailure(String ip) {
        Long start = windowStart.get(ip);
        long now = System.currentTimeMillis();
        if (start == null || now - start > WINDOW_MS) {
            windowStart.put(ip, now);
            failures.put(ip, new AtomicInteger(1));
        } else {
            failures.computeIfAbsent(ip, k -> new AtomicInteger()).incrementAndGet();
        }
    }

    private synchronized void clearFailures(String ip) {
        failures.remove(ip);
        windowStart.remove(ip);
    }
}
