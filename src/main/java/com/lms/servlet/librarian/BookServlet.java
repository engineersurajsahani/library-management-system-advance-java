package com.lms.servlet.librarian;

import com.lms.dao.BookDAO;
import com.lms.model.Book;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Librarian: list / add / edit / delete books. */
@WebServlet(urlPatterns = {"/librarian/books", "/librarian/books/add", "/librarian/books/edit", "/librarian/books/delete"})
public class BookServlet extends HttpServlet {

    private static final int PAGE_SIZE = 8;
    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/librarian/books" -> list(req, resp);
                case "/librarian/books/add" -> {
                    req.setAttribute("book", new Book());
                    req.setAttribute("mode", "add");
                    req.setAttribute("categories", bookDAO.findAllCategories());
                    req.getRequestDispatcher("/WEB-INF/views/librarian/book-form.jsp").forward(req, resp);
                }
                case "/librarian/books/edit" -> {
                    int id = Integer.parseInt(req.getParameter("id"));
                    Book b = bookDAO.findById(id);
                    if (b == null) { resp.sendError(404); return; }
                    req.setAttribute("book", b);
                    req.setAttribute("mode", "edit");
                    req.setAttribute("categories", bookDAO.findAllCategories());
                    req.getRequestDispatcher("/WEB-INF/views/librarian/book-form.jsp").forward(req, resp);
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
                case "/librarian/books/add" -> add(req, resp);
                case "/librarian/books/edit" -> update(req, resp);
                case "/librarian/books/delete" -> delete(req, resp);
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String q = req.getParameter("q");
        String category = req.getParameter("category");
        int page = parseInt(req.getParameter("page"), 1);
        int total = bookDAO.count(q, category);
        int pages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        page = Math.min(page, pages);
        req.setAttribute("books", bookDAO.search(q, category, (page - 1) * PAGE_SIZE, PAGE_SIZE));
        req.setAttribute("total", total);
        req.setAttribute("page", page);
        req.setAttribute("pages", pages);
        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("category", category == null ? "all" : category);
        req.setAttribute("categories", bookDAO.findAllCategories());
        req.getRequestDispatcher("/WEB-INF/views/librarian/books.jsp").forward(req, resp);
    }

    private void add(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Book b = extract(req, new Book());
        List<String> errors = validate(b, null);
        if (!errors.isEmpty()) {
            req.setAttribute("book", b);
            req.setAttribute("mode", "add");
            req.setAttribute("errors", errors);
            req.setAttribute("categories", bookDAO.findAllCategories());
            req.getRequestDispatcher("/WEB-INF/views/librarian/book-form.jsp").forward(req, resp);
            return;
        }
        bookDAO.insert(b);
        resp.sendRedirect(req.getContextPath() + "/librarian/books?msg=book-added");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));
        Book existing = bookDAO.findById(id);
        if (existing == null) { resp.sendError(404); return; }
        Book b = extract(req, existing);
        List<String> errors = validate(b, id);
        if (!errors.isEmpty()) {
            req.setAttribute("book", b);
            req.setAttribute("mode", "edit");
            req.setAttribute("errors", errors);
            req.setAttribute("categories", bookDAO.findAllCategories());
            req.getRequestDispatcher("/WEB-INF/views/librarian/book-form.jsp").forward(req, resp);
            return;
        }
        bookDAO.update(b);
        resp.sendRedirect(req.getContextPath() + "/librarian/books?msg=book-updated");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));
        bookDAO.delete(id);
        resp.sendRedirect(req.getContextPath() + "/librarian/books?msg=book-deleted");
    }

    private Book extract(HttpServletRequest req, Book b) {
        b.setIsbn(trim(req.getParameter("isbn")));
        b.setTitle(trim(req.getParameter("title")));
        b.setAuthor(trim(req.getParameter("author")));
        b.setPublisher(trim(req.getParameter("publisher")));
        b.setCategory(trim(req.getParameter("category")));
        b.setShelf(trim(req.getParameter("shelf")));
        b.setTotalCopies(parseInt(req.getParameter("totalCopies"), b.getTotalCopies() <= 0 ? 1 : b.getTotalCopies()));
        // Keep availability consistent when total changes on edit
        int currentlyIssued = b.getTotalCopies() - b.getAvailableCopies();
        if (currentlyIssued < 0) currentlyIssued = 0;
        b.setAvailableCopies(Math.max(0, b.getTotalCopies() - currentlyIssued));
        b.setPrice(parseDouble(req.getParameter("price"), b.getPrice()));
        return b;
    }

    private List<String> validate(Book b, Integer excludeId) {
        List<String> errors = new ArrayList<>();
        if (b.getTitle() == null || b.getTitle().length() < 2) errors.add("Title must be at least 2 characters");
        if (b.getAuthor() == null || b.getAuthor().isBlank()) errors.add("Author is required");
        if (b.getTotalCopies() <= 0) errors.add("Total copies must be at least 1");
        try {
            if (bookDAO.isbnExists(b.getIsbn(), excludeId)) errors.add("ISBN already exists");
        } catch (Exception e) {
            errors.add("Validation failed: " + e.getMessage());
        }
        return errors;
    }

    private String trim(String v) { return v == null ? null : v.trim(); }

    private int parseInt(String v, int def) {
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }

    private double parseDouble(String v, double def) {
        try { return Double.parseDouble(v); } catch (Exception e) { return def; }
    }
}
