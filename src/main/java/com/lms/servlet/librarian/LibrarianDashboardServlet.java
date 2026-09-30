package com.lms.servlet.librarian;

import com.lms.dao.BookDAO;
import com.lms.dao.IssueDAO;
import com.lms.dao.StudentDAO;
import com.lms.model.Issue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/librarian/dashboard")
public class LibrarianDashboardServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final IssueDAO issueDAO = new IssueDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("totalBooks", bookDAO.countAll());
            req.setAttribute("totalCopies", bookDAO.totalCopies());
            req.setAttribute("availableCopies", bookDAO.availableCopies());
            req.setAttribute("issuedCopies", bookDAO.countIssuedCopies());
            req.setAttribute("totalStudents", studentDAO.count(null));
            req.setAttribute("activeIssues", issueDAO.countActive());
            req.setAttribute("overdueCount", issueDAO.countOverdue());
            req.setAttribute("outstandingFine", issueDAO.totalOutstandingFine());
            req.setAttribute("fineCollected", issueDAO.totalFineCollected());

            List<Issue> overdue = issueDAO.overdueForLibrarian(8);
            req.setAttribute("overdueList", overdue);
            req.setAttribute("recentIssues", issueDAO.findHistory(null, 0, 8));

            req.getRequestDispatcher("/WEB-INF/views/librarian/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
