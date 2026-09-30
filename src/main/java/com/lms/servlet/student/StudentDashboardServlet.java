package com.lms.servlet.student;

import com.lms.dao.BookDAO;
import com.lms.dao.IssueDAO;
import com.lms.dao.StudentDAO;
import com.lms.model.Issue;
import com.lms.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();
    private final IssueDAO issueDAO = new IssueDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            Student student = studentDAO.findByUserId(userId);
            if (student == null) {
                resp.sendError(403, "Student profile not found");
                return;
            }
            req.setAttribute("student", student);
            req.setAttribute("totalBooks", bookDAO.countAll());
            req.setAttribute("availableNow", bookDAO.availableCopies());
            req.setAttribute("myActive", issueDAO.countActiveForStudent(student.getId()));
            req.setAttribute("myOverdue", issueDAO.countOverdueForStudent(student.getId()));

            List<Issue> dueSoon = issueDAO.dueSoon(student.getId(), 7);
            req.setAttribute("dueSoon", dueSoon);
            req.setAttribute("recentBooks", bookDAO.search(null, null, 0, 6));

            req.getRequestDispatcher("/WEB-INF/views/student/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
