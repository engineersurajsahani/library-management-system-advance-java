package com.lms.servlet.student;

import com.lms.dao.StudentDAO;
import com.lms.dao.UserDAO;
import com.lms.model.Student;
import com.lms.util.SecurityUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/student/profile")
public class ProfileServlet extends HttpServlet {

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            Student student = studentDAO.findByUserId(userId);
            if (student == null) {
                resp.sendError(403);
                return;
            }
            req.setAttribute("student", student);
            req.getRequestDispatcher("/WEB-INF/views/student/profile.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            Student student = studentDAO.findByUserId(userId);

            String current = req.getParameter("currentPassword");
            String next = req.getParameter("newPassword");
            String confirm = req.getParameter("confirmPassword");

            com.lms.model.User user = userDAO.findById(userId);
            List<String> errors = new ArrayList<>();

            if (!SecurityUtil.verifyPassword(current, user.getPasswordHash())) {
                errors.add("Current password is incorrect");
            }
            if (next == null || next.length() < 6) {
                errors.add("New password must be at least 6 characters");
            }
            if (!next.equals(confirm)) {
                errors.add("Passwords do not match");
            }

            if (!errors.isEmpty()) {
                req.setAttribute("student", student);
                req.setAttribute("errors", errors);
                req.getRequestDispatcher("/WEB-INF/views/student/profile.jsp").forward(req, resp);
                return;
            }

            userDAO.updatePassword(userId, next);
            resp.sendRedirect(req.getContextPath() + "/student/profile?msg=" +
                    java.net.URLEncoder.encode("Password changed successfully", java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
