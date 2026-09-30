package com.lms.dao;

import com.lms.model.Student;
import com.lms.util.DBUtil;
import com.lms.util.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Data access for students (with joined users for credentials). */
public class StudentDAO {

    private static final String BASE_SELECT =
        "SELECT s.*, u.username FROM students s JOIN users u ON s.user_id = u.id";

    public List<Student> search(String query, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            sql.append(" WHERE s.name LIKE ? OR s.email LIKE ? OR s.roll_no LIKE ?");
            String like = "%" + query.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        sql.append(" ORDER BY s.id DESC LIMIT ? OFFSET ?");
        params.add(limit); params.add(offset);

        List<Student> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            DBUtil.bind(ps, params.toArray());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int count(String query) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM students s");
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            sql.append(" WHERE s.name LIKE ? OR s.email LIKE ? OR s.roll_no LIKE ?");
            String like = "%" + query.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            DBUtil.bind(ps, params.toArray());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Student findById(int id) throws SQLException {
        String sql = BASE_SELECT + " WHERE s.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public Student findByUserId(int userId) throws SQLException {
        String sql = BASE_SELECT + " WHERE s.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean rollNoExists(String rollNo, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE roll_no = ? AND (? IS NULL OR id != ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, rollNo, excludeId, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean emailExists(String email, Integer excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE email = ? AND (? IS NULL OR id != ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, email, excludeId, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setUserId(rs.getInt("user_id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setRollNo(rs.getString("roll_no"));
        s.setDepartment(rs.getString("department"));
        s.setSemester(rs.getInt("semester"));
        s.setActive(rs.getInt("active") == 1);
        s.setUsername(rs.getString("username"));
        return s;
    }

    /**
     * Create student + login user atomically.
     * @return the created student, or null if insert failed.
     */
    public Student create(Student s, String username, String plainPassword) throws SQLException {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            int userId;
            String userSql = "INSERT INTO users (username, password_hash, role) VALUES (?,?, 'STUDENT')";
            try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                DBUtil.bind(ps, username, SecurityUtil.hashPassword(plainPassword));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No user id generated");
                    userId = keys.getInt(1);
                }
            }
            int studentId;
            String studentSql = "INSERT INTO students (user_id, name, email, phone, roll_no, department, semester, active) VALUES (?,?,?,?,?,?,?,1)";
            try (PreparedStatement ps = conn.prepareStatement(studentSql, Statement.RETURN_GENERATED_KEYS)) {
                DBUtil.bind(ps, userId, s.getName(), s.getEmail(), s.getPhone(), s.getRollNo(), s.getDepartment(), s.getSemester());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No student id generated");
                    studentId = keys.getInt(1);
                }
            }
            conn.commit();
            return findById(studentId);
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignore) { }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) { }
                }
        }
    }

    public boolean update(Student s) throws SQLException {
        String sql = "UPDATE students SET name=?, email=?, phone=?, roll_no=?, department=?, semester=?, active=? WHERE id=?";
        return DBUtil.executeUpdate(sql,
                s.getName(), s.getEmail(), s.getPhone(), s.getRollNo(),
                s.getDepartment(), s.getSemester(), s.isActive() ? 1 : 0, s.getId()) > 0;
    }

    public boolean setActive(int studentId, boolean active) throws SQLException {
        return DBUtil.executeUpdate("UPDATE students SET active=? WHERE id=?", active ? 1 : 0, studentId) > 0;
    }

    public boolean delete(int studentId) throws SQLException {
        // users row cascades to students via FK (ON DELETE CASCADE from users side).
        Student s = findById(studentId);
        if (s == null) return false;
        return DBUtil.executeUpdate("DELETE FROM users WHERE id = ?", s.getUserId()) > 0;
    }
}
