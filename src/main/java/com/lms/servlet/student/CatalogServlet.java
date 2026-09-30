package com.lms.servlet.student;

import com.lms.dao.BookDAO;
import com.lms.model.Book;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Book catalog: server-rendered list + AJAX JSON search endpoint. */
@WebServlet("/student/books")
public class CatalogServlet extends HttpServlet {

    private static final int PAGE_SIZE = 9;
    private final BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accept = req.getHeader("Accept");
        boolean wantsJson = req.getParameter("format") != null && "json".equals(req.getParameter("format"));

        try {
            if (wantsJson) {
                ajaxSearch(req, resp);
                return;
            }
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
            req.getRequestDispatcher("/WEB-INF/views/student/catalog.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void ajaxSearch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String q = req.getParameter("q");
        String category = req.getParameter("category");
        List<Book> books = bookDAO.search(q, category, 0, 12);

        List<Map<String, Object>> items = new ArrayList<>();
        for (Book b : books) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("title", b.getTitle());
            m.put("author", b.getAuthor());
            m.put("isbn", b.getIsbn());
            m.put("category", b.getCategory());
            m.put("shelf", b.getShelf());
            m.put("availableCopies", b.getAvailableCopies());
            m.put("totalCopies", b.getTotalCopies());
            items.add(m);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        StringBuilder json = new StringBuilder("{\"count\":").append(items.size()).append(",\"books\":[");

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> m = items.get(i);
            if (i > 0) json.append(',');
            json.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : m.entrySet()) {
                if (!first) json.append(',');
                first = false;
                json.append('"').append(e.getKey()).append("\":");
                Object v = e.getValue();
                if (v instanceof Number) {
                    json.append(v);
                } else {
                    json.append('"').append(escapeJson(String.valueOf(v))).append('"');
                }
            }
            json.append('}');
        }
        json.append("]}");
        resp.getWriter().write(json.toString());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private int parseInt(String v, int def) {
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }
}
