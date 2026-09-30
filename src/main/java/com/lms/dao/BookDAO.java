package com.lms.dao;

import com.lms.model.Book;
import com.lms.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data access for books table. */
public class BookDAO {

    public List<Book> search(String query, String category, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, query, category);
        sql.append(" ORDER BY title COLLATE NOCASE LIMIT ? OFFSET ?");
        params.add(limit); params.add(offset);

        List<Book> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            DBUtil.bind(ps, params.toArray());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int count(String query, String category) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, query, category);
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            DBUtil.bind(ps, params.toArray());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void appendFilters(StringBuilder sql, List<Object> params, String query, String category) {
        if (query != null && !query.isBlank()) {
            sql.append(" AND (title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ? OR publisher LIKE ?)");
            String like = "%" + query.trim() + "%";
            for (int i = 0; i < 5; i++) params.add(like);
        }
        if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }
    }

    public Book findById(int id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean isbnExists(String isbn, Integer excludeId) throws SQLException {
        if (isbn == null || isbn.isBlank()) return false;
        String sql = "SELECT COUNT(*) FROM books WHERE isbn = ? AND (? IS NULL OR id != ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, isbn.trim(), excludeId, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public int insert(Book b) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, publisher, category, total_copies, available_copies, shelf, price) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, b.getIsbn(), b.getTitle(), b.getAuthor(), b.getPublisher(),
                    b.getCategory(), b.getTotalCopies(), b.getAvailableCopies(), b.getShelf(), b.getPrice());
            return DBUtil.getGeneratedKey(ps);
        }
    }

    public boolean update(Book b) throws SQLException {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, publisher=?, category=?, total_copies=?, available_copies=?, shelf=?, price=? WHERE id=?";
        return DBUtil.executeUpdate(sql, b.getIsbn(), b.getTitle(), b.getAuthor(), b.getPublisher(),
                b.getCategory(), b.getTotalCopies(), b.getAvailableCopies(), b.getShelf(), b.getPrice(), b.getId()) > 0;
    }

    public boolean delete(int id) throws SQLException {
        return DBUtil.executeUpdate("DELETE FROM books WHERE id = ?", id) > 0;
    }

    public List<String> findAllCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM books WHERE category IS NOT NULL AND category != '' ORDER BY category";
        List<String> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    /** Stats for dashboards. */
    public int countAll() throws SQLException {
        return scalar("SELECT COUNT(*) FROM books");
    }

    public int totalCopies() throws SQLException {
        return scalar("SELECT COALESCE(SUM(total_copies),0) FROM books");
    }

    public int availableCopies() throws SQLException {
        return scalar("SELECT COALESCE(SUM(available_copies),0) FROM books");
    }

    public int countIssuedCopies() throws SQLException {
        return scalar("SELECT COALESCE(SUM(total_copies - available_copies),0) FROM books");
    }

    private Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setPublisher(rs.getString("publisher"));
        b.setCategory(rs.getString("category"));
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        b.setShelf(rs.getString("shelf"));
        b.setPrice(rs.getDouble("price"));
        b.setCreatedAt(rs.getString("created_at"));
        return b;
    }

    private int scalar(String sql) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
