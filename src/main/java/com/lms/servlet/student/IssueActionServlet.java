package com.lms.servlet.student;

import com.lms.dao.IssueDAO;
import com.lms.dao.StudentDAO;
import com.lms.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Student self-service: request issue of a book and return a book. */
@WebServlet(urlPatterns = {"/student/issue", "/student/return"})
public class IssueActionServlet extends HttpServlet {

    private final IssueDAO issueDAO = new IssueDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            Student student = studentDAO.findByUserId(userId);
            if (student == null) {
                resp.sendError(403);
                return;
            }

            if ("/student/issue".equals(path)) {
                // Guard: max 4 active books per student
                int active = issueDAO.countActiveForStudent(student.getId());
                if (active >= 4) {
                    resp.sendRedirect(req.getContextPath() + "/student/books?msg=" +
                            enc("You already have 4 books. Return one to issue another.") + "&type=error");
                    return;
                }
                int bookId = Integer.parseInt(req.getParameter("bookId"));
                issueDAO.issueBook(bookId, student.getId(), student.getName());
                resp.sendRedirect(req.getContextPath() + "/student/my-books?msg=" +
                        enc("Book issued successfully. Due in 14 days."));
            } else {
                int issueId = Integer.parseInt(req.getParameter("issueId"));
                double fine = issueDAO.returnBook(issueId, student.getName());
                String msg = fine > 0
                        ? "Book returned. Please pay ₹" + String.format("%.2f", fine) + " fine at the library desk."
                        : "Book returned on time. No fine.";
                resp.sendRedirect(req.getContextPath() + "/student/my-books?msg=" + enc(msg));
            }
        } catch (Exception e) {
            String msg = (e.getCause() == null && e.getMessage() != null) ? e.getMessage() : "Action failed. Please try again.";
            resp.sendRedirect(req.getContextPath() + "/student/books?msg=" + enc(msg) + "&type=error");
        }
    }

    private String enc(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
