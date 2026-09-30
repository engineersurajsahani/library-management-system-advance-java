package com.lms.servlet.librarian;

import com.lms.dao.BookDAO;
import com.lms.dao.IssueDAO;
import com.lms.dao.StudentDAO;
import com.lms.model.Issue;
import com.lms.util.FineUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/** Librarian: issue and return books, collect fines, view circulation. */
@WebServlet(urlPatterns = {"/librarian/issues", "/librarian/issues/issue", "/librarian/issues/return", "/librarian/issues/fine"})
public class IssueServlet extends HttpServlet {

    private static final int PAGE_SIZE = 10;
    private final IssueDAO issueDAO = new IssueDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String q = req.getParameter("q");
            int page = parseInt(req.getParameter("page"), 1);
            int total = issueDAO.countAll();
            int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
            page = Math.min(page, pages);

            List<Issue> active = issueDAO.findActiveForLibrarian(q, (page - 1) * PAGE_SIZE, PAGE_SIZE);
            for (Issue i : active) {
                i.setFine(FineUtil.currentFine(i.getDueDate()));
            }
            req.setAttribute("activeIssues", active);
            req.setAttribute("activeIssuesCount", issueDAO.countActive());
            req.setAttribute("students", studentDAO.search(null, 0, 100));
            req.setAttribute("availableBooks", bookDAO.search(null, null, 0, 200));
            req.setAttribute("today", java.time.LocalDate.now().toString());
            req.setAttribute("total", total);
            req.setAttribute("page", page);
            req.setAttribute("pages", pages);
            req.setAttribute("q", q == null ? "" : q);
            req.getRequestDispatcher("/WEB-INF/views/librarian/issues.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/librarian/issues/issue" -> doIssue(req, resp);
                case "/librarian/issues/return" -> doReturn(req, resp);
                case "/librarian/issues/fine" -> collectFine(req, resp);
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            // Friendly redirect with error message instead of stack trace
            String msg = e.getMessage() != null && e.getCause() == null ? e.getMessage() : "Action failed";
            resp.sendRedirect(req.getContextPath() + "/librarian/issues?msg=" +
                    java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8) + "&type=error");
        }
    }

    private void doIssue(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int bookId = Integer.parseInt(req.getParameter("bookId"));
        int studentId = Integer.parseInt(req.getParameter("studentId"));
        String issuedBy = (String) req.getSession().getAttribute("username");
        issueDAO.issueBook(bookId, studentId, issuedBy);
        resp.sendRedirect(req.getContextPath() + "/librarian/issues?msg=book-issued");
    }

    private void doReturn(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int issueId = Integer.parseInt(req.getParameter("issueId"));
        String returnedBy = (String) req.getSession().getAttribute("username");
        double fine = issueDAO.returnBook(issueId, returnedBy);
        String msg = fine > 0
                ? "Book returned. Fine collected: ₹" + String.format("%.2f", fine)
                : "Book returned on time. No fine.";
        resp.sendRedirect(req.getContextPath() + "/librarian/issues?msg=" +
                java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8));
    }

    private void collectFine(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int issueId = Integer.parseInt(req.getParameter("issueId"));
        issueDAO.payFine(issueId);
        resp.sendRedirect(req.getContextPath() + "/librarian/issues?msg=fine-recorded");
    }

    private int parseInt(String v, int def) {
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }
}
