package com.lms.dao;

import com.lms.model.User;
import com.lms.util.DBUtil;
import com.lms.util.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data access for users table. */
public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public User create(String username, String plainPassword, String role) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?,?,?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            DBUtil.bind(ps, username, SecurityUtil.hashPassword(plainPassword), role);
            ps.executeUpdate();
        }
        return findByUsername(username);
    }

    public boolean updatePassword(int userId, String newPlainPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        return DBUtil.executeUpdate(sql, SecurityUtil.hashPassword(newPlainPassword), userId) > 0;
    }

    public List<User> findAll(String role) throws SQLException {
        String sql = "SELECT * FROM users" + (role == null ? "" : " WHERE role = ?") + " ORDER BY id";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (role != null) {
                DBUtil.bind(ps, role);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(rs.getString("created_at"));
        return u;
    }
}
