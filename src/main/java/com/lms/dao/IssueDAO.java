package com.lms.dao;

import com.lms.model.Issue;
import com.lms.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Issue/return operations with fine calculation and atomic copy management. */
public class IssueDAO {

    public static final int LOAN_DAYS = 14;
    public static final double FINE_PER_DAY = 5.0;

    /** Issue a book: validates availability, decrements copies, inserts row. */
    public int issueBook(int bookId, int studentId, String issuedBy) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // Lock-ish check inside transaction
            int available;
            try (PreparedStatement ps = conn.prepareStatement("SELECT available_copies FROM books WHERE id = ?")) {
                ps.setInt(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Book not found");
                    available = rs.getInt(1);
                }
            }
            if (available <= 0) {
                throw new SQLException("Book is not available right now");
            }

            // Prevent duplicate active issue of same book for same student
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM issues WHERE book_id=? AND student_id=? AND status='ISSUED'")) {
                ps.setInt(1, bookId);
                ps.setInt(2, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("This book is already issued to this student");
                    }
                }
            }

            LocalDate today = LocalDate.now();
            int id;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO issues (book_id, student_id, issue_date, due_date, status, issued_by) VALUES (?,?,?,?, 'ISSUED', ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, bookId);
                ps.setInt(2, studentId);
                ps.setString(3, today.toString());
                ps.setString(4, today.plusDays(LOAN_DAYS).toString());
                ps.setString(5, issuedBy);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No generated key");
                    id = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0")) {
                ps.setInt(1, bookId);
                if (ps.executeUpdate() == 0) throw new SQLException("Failed to decrement copies");
            }

            conn.commit();
            return id;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignore) { }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) { }
            }
        }
    }

    /** Return a book: computes fine, marks returned, increments copies. */
    public double returnBook(int issueId, String returnedBy) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String status;
            LocalDate dueDate;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT status, due_date FROM issues WHERE id = ?")) {
                ps.setInt(1, issueId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Issue record not found");
                    status = rs.getString("status");
                    dueDate = LocalDate.parse(rs.getString("due_date"));
                }
            }
            if ("RETURNED".equals(status)) {
                throw new SQLException("Book already returned");
            }

            LocalDate today = LocalDate.now();
            long lateDays = Math.max(0, ChronoUnit.DAYS.between(dueDate, today));
            double fine = lateDays * FINE_PER_DAY;

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE issues SET status='RETURNED', return_date=?, fine=?, returned_by=? WHERE id=?")) {
                ps.setString(1, today.toString());
                ps.setDouble(2, fine);
                ps.setString(3, returnedBy);
                ps.setInt(4, issueId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE books SET available_copies = MIN(total_copies, available_copies + 1) WHERE id = " +
                    "(SELECT book_id FROM issues WHERE id = ?)")) {
                ps.setInt(1, issueId);
                ps.executeUpdate();
            }

            conn.commit();
            return fine;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignore) { }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) { }
            }
        }
    }

    /** Mark fine as paid. */
    public boolean payFine(int issueId) throws SQLException {
        return DBUtil.executeUpdate("UPDATE issues SET fine_paid=1 WHERE id=? AND fine > 0", issueId) > 0;
    }

    public List<Issue> findForStudent(int studentId, boolean activeOnly) throws SQLException {
        String sql = "SELECT i.*, b.title AS book_title, b.author AS book_author, b.isbn AS book_isbn " +
                "FROM issues i JOIN books b ON i.book_id = b.id " +
                "WHERE i.student_id = ?" + (activeOnly ? " AND i.status='ISSUED'" : "") +
                " ORDER BY i.id DESC";
        return queryList(sql, studentId);
    }

    public List<Issue> findActiveForLibrarian(String query, int offset, int limit) throws SQLException {
        String sql = "SELECT i.*, b.title AS book_title, b.author AS book_author, " +
                "s.name AS student_name, s.roll_no AS student_roll " +
                "FROM issues i JOIN books b ON i.book_id = b.id JOIN students s ON i.student_id = s.id " +
                "WHERE i.status='ISSUED'";
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            sql += " AND (s.name LIKE ? OR s.roll_no LIKE ? OR b.title LIKE ?)";
            String like = "%" + query.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        sql += " ORDER BY i.due_date ASC LIMIT ? OFFSET ?";
        params.add(limit); params.add(offset);
        return queryList(sql, params.toArray());
    }

    public List<Issue> findHistory(String query, int offset, int limit) throws SQLException {
        String sql = "SELECT i.*, b.title AS book_title, s.name AS student_name, s.roll_no AS student_roll " +
                "FROM issues i JOIN books b ON i.book_id = b.id JOIN students s ON i.student_id = s.id WHERE 1=1";
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            sql += " AND (s.name LIKE ? OR s.roll_no LIKE ? OR b.title LIKE ?)";
            String like = "%" + query.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        sql += " ORDER BY i.id DESC LIMIT ? OFFSET ?";
        params.add(limit); params.add(offset);
        return queryList(sql, params.toArray());
    }

    public int countActiveForStudent(int studentId) throws SQLException {
        return DBUtil.scalar("SELECT COUNT(*) FROM issues WHERE student_id=? AND status='ISSUED'", studentId);
    }

    public int countOverdueForStudent(int studentId) throws SQLException {
        return DBUtil.scalar("SELECT COUNT(*) FROM issues WHERE student_id=? AND status='ISSUED' AND due_date < ?", studentId, LocalDate.now().toString());
    }

    /** Stats for librarian dashboard. */
    public int countActive() throws SQLException {
        return DBUtil.scalar("SELECT COUNT(*) FROM issues WHERE status='ISSUED'");
    }

    public int countOverdue() throws SQLException {
        return DBUtil.scalar("SELECT COUNT(*) FROM issues WHERE status='ISSUED' AND due_date < ?", LocalDate.now().toString());
    }

    public double totalOutstandingFine() throws SQLException {
        return DBUtil.scalar("SELECT COALESCE(SUM(fine),0) FROM issues WHERE status='ISSUED' AND fine > 0");
    }

    public int countAll() throws SQLException {
        return DBUtil.scalar("SELECT COUNT(*) FROM issues");
    }

    public double totalFineCollected() throws SQLException {
        return DBUtil.scalar("SELECT COALESCE(SUM(fine),0) FROM issues WHERE fine_paid=1");
    }

    /** Issue rows due in next 7 days for dashboard widget. */
    public List<Issue> dueSoon(int studentId, int days) throws SQLException {
        String sql = "SELECT i.*, b.title AS book_title FROM issues i JOIN books b ON i.book_id=b.id " +
                "WHERE i.student_id=? AND i.status='ISSUED' AND i.due_date <= ? ORDER BY i.due_date ASC";
        return queryList(sql, studentId, LocalDate.now().plusDays(days).toString());
    }

    public List<Issue> overdueForLibrarian(int limit) throws SQLException {
        String sql = "SELECT i.*, b.title AS book_title, s.name AS student_name, s.roll_no AS student_roll " +
                "FROM issues i JOIN books b ON i.book_id=b.id JOIN students s ON i.student_id=s.id " +
                "WHERE i.status='ISSUED' AND i.due_date < ? ORDER BY i.due_date ASC LIMIT ?";
        return queryList(sql, LocalDate.now().toString(), limit);
    }

    private List<Issue> queryList(String sql, Object... params) throws SQLException {
        List<Issue> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Issue map(ResultSet rs) throws SQLException {
        Issue i = new Issue();
        i.setId(rs.getInt("id"));
        i.setBookId(rs.getInt("book_id"));
        i.setStudentId(rs.getInt("student_id"));
        i.setIssueDate(rs.getString("issue_date"));
        i.setDueDate(rs.getString("due_date"));
        String rd = rs.getString("return_date");
        i.setReturnDate(rs.wasNull() ? null : rd);
        i.setFine(rs.getDouble("fine"));
        i.setFinePaid(rs.getInt("fine_paid") == 1);
        i.setStatus(rs.getString("status"));
        i.setIssuedBy(rs.getString("issued_by"));
        i.setReturnedBy(rs.getString("returned_by"));
        try { i.setBookTitle(rs.getString("book_title")); } catch (SQLException ignore) { }
        try { i.setBookAuthor(rs.getString("book_author")); } catch (SQLException ignore) { }
        try { i.setBookIsbn(rs.getString("book_isbn")); } catch (SQLException ignore) { }
        try { i.setStudentName(rs.getString("student_name")); } catch (SQLException ignore) { }
        try { i.setStudentRoll(rs.getString("student_roll")); } catch (SQLException ignore) { }
        return i;
    }
}
