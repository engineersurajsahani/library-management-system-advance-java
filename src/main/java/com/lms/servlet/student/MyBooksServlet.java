package com.lms.servlet.student;

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

@WebServlet("/student/my-books")
public class MyBooksServlet extends HttpServlet {

    private final IssueDAO issueDAO = new IssueDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            Student student = studentDAO.findByUserId(userId);
            if (student == null) {
                resp.sendError(403);
                return;
            }
            List<Issue> active = issueDAO.findForStudent(student.getId(), true);
            List<Issue> history = issueDAO.findForStudent(student.getId(), false);

            // Compute live fine preview for active issues
            for (Issue i : active) {
                i.setFine(com.lms.util.FineUtil.currentFine(i.getDueDate()));
            }

            req.setAttribute("student", student);
            req.setAttribute("activeIssues", active);
            req.setAttribute("history", history);
            req.getRequestDispatcher("/WEB-INF/views/student/my-books.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
