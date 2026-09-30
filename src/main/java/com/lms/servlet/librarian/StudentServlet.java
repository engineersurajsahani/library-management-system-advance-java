package com.lms.servlet.librarian;

import com.lms.dao.StudentDAO;
import com.lms.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Librarian: list / add / edit / activate / delete students. */
@WebServlet(urlPatterns = {"/librarian/students", "/librarian/students/add", "/librarian/students/edit", "/librarian/students/delete", "/librarian/students/toggle"})
public class StudentServlet extends HttpServlet {

    private static final int PAGE_SIZE = 8;
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/librarian/students" -> list(req, resp);
                case "/librarian/students/add" -> {
                    req.setAttribute("student", new Student());
                    req.setAttribute("mode", "add");
                    req.getRequestDispatcher("/WEB-INF/views/librarian/student-form.jsp").forward(req, resp);
                }
                case "/librarian/students/edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Student s = studentDAO.findById(id);
                    if (s == null) { resp.sendError(404); return; }
                    req.setAttribute("student", s);
                    req.setAttribute("mode", "edit");
                    req.getRequestDispatcher("/WEB-INF/views/librarian/student-form.jsp").forward(req, resp);
                }
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/librarian/students/add" -> add(req, resp);
                case "/librarian/students/edit" -> update(req, resp);
                case "/librarian/students/delete" -> delete(req, resp);
                case "/librarian/students/toggle" -> toggle(req, resp);
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String q = req.getParameter("q");
        int page = parseInt(req.getParameter("page"), 1);
        int total = studentDAO.count(q);
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        page = Math.min(page, pages);
        req.setAttribute("students", studentDAO.search(q, (page - 1) * PAGE_SIZE, PAGE_SIZE));
        req.setAttribute("total", total);
        req.setAttribute("page", page);
        req.setAttribute("pages", pages);
        req.setAttribute("q", q == null ? "" : q);
        req.getRequestDispatcher("/WEB-INF/views/librarian/students.jsp").forward(req, resp);
    }

    private void add(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Student s = new Student();
        s.setName(trim(req.getParameter("name")));
        s.setEmail(trim(req.getParameter("email")));
        s.setPhone(trim(req.getParameter("phone")));
        s.setRollNo(trim(req.getParameter("rollNo")));
        s.setDepartment(trim(req.getParameter("department")));
        s.setSemester(parseInt(req.getParameter("semester"), 1));

        List<String> errors = validate(s, null, true, req);
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");

        if (username == null || username.length() < 4) errors.add("Username must be at least 4 characters");
        if (password == null || password.length() < 6) errors.add("Password must be at least 6 characters");

        if (!errors.isEmpty()) {
            req.setAttribute("student", s);
            req.setAttribute("mode", "add");
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/views/librarian/student-form.jsp").forward(req, resp);
            return;
        }

        studentDAO.create(s, username, password);
        resp.sendRedirect(req.getContextPath() + "/librarian/students?msg=student-added");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));
        Student existing = studentDAO.findById(id);
        if (existing == null) { resp.sendError(404); return; }

        existing.setName(trim(req.getParameter("name")));
        existing.setEmail(trim(req.getParameter("email")));
        existing.setPhone(trim(req.getParameter("phone")));
        existing.setRollNo(trim(req.getParameter("rollNo")));
        existing.setDepartment(trim(req.getParameter("department")));
        existing.setSemester(parseInt(req.getParameter("semester"), existing.getSemester()));

        List<String> errors = validate(existing, id, false, req);
        if (!errors.isEmpty()) {
            req.setAttribute("student", existing);
            req.setAttribute("mode", "edit");
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/views/librarian/student-form.jsp").forward(req, resp);
            return;
        }
        studentDAO.update(existing);
        resp.sendRedirect(req.getContextPath() + "/librarian/students?msg=student-updated");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));
        try {
            studentDAO.delete(id);
            resp.sendRedirect(req.getContextPath() + "/librarian/students?msg=student-deleted");
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/librarian/students?msg=student-has-books");
        }
    }

    private void toggle(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));
        Student s = studentDAO.findById(id);
        if (s != null) {
            studentDAO.setActive(id, !s.isActive());
        }
        resp.sendRedirect(req.getContextPath() + "/librarian/students?msg=student-updated");
    }

    private List<String> validate(Student s, Integer excludeId, boolean isAdd, HttpServletRequest req) {
        List<String> errors = new ArrayList<>();
        if (s.getName() == null || s.getName().length() < 3) errors.add("Name must be at least 3 characters");
        if (s.getEmail() == null || !s.getEmail().matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")) errors.add("Valid email is required");
        if (s.getRollNo() == null || s.getRollNo().isBlank()) errors.add("Roll number is required");
        try {
            if (isAdd && studentDAO.rollNoExists(s.getRollNo(), null)) errors.add("Roll number already exists");
            if (!s.getEmail().isBlank() && studentDAO.emailExists(s.getEmail(), excludeId)) errors.add("Email already exists");
        } catch (Exception e) {
            errors.add("Validation failed: " + e.getMessage());
        }
        return errors;
    }

    private String trim(String v) {
        return v == null ? null : v.trim();
    }

    private int parseInt(String v, int def) {
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }
}
