package com.lms.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Central SQLite access point for the Library Management System.
 * Creates the database file and schema on first use and seeds demo data.
 */
public final class DBUtil {

    private static final String DB_PATH = resolveDbPath();
    private static volatile boolean initialized = false;
    private static final Object INIT_LOCK = new Object();

    private DBUtil() { }

    private static String resolveDbPath() {
        String prop = System.getProperty("lms.db.path");
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        String env = System.getenv("LMS_DB_PATH");
        if (env != null && !env.isBlank()) {
            return env;
        }
        // Local dev: keep DB inside project ./data folder
        return Paths.get("data", "lms.db").toString();
    }

    static {
        try {
            // Explicit load keeps this working on classloaders with broken ServiceLoader support
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("SQLite JDBC driver not found: " + e);
        }
    }

    public static String getDbPath() {
        return DB_PATH;
    }

    /** Get a connection. Ensures schema exists (first call initializes). */
    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
        try (Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    private static void ensureInitialized() throws SQLException {
        if (initialized) {
            return;
        }
        synchronized (INIT_LOCK) {
            if (initialized) {
                return;
            }
            try {
                Path parent = Paths.get(DB_PATH).toAbsolutePath().getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
            } catch (Exception e) {
                throw new SQLException("Could not create DB directory: " + DB_PATH, e);
            }
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
                createSchema(conn);
                seed(conn);
            }
            initialized = true;
        }
    }

    private static void createSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT NOT NULL UNIQUE," +
                "  password_hash TEXT NOT NULL," +
                "  role TEXT NOT NULL CHECK (role IN ('ADMIN','STUDENT'))," +
                "  created_at TEXT NOT NULL DEFAULT (datetime('now'))" +
                ")");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS students (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE," +
                "  name TEXT NOT NULL," +
                "  email TEXT NOT NULL UNIQUE," +
                "  phone TEXT," +
                "  roll_no TEXT NOT NULL UNIQUE," +
                "  department TEXT," +
                "  semester INTEGER DEFAULT 1," +
                "  active INTEGER NOT NULL DEFAULT 1," +
                "  created_at TEXT NOT NULL DEFAULT (datetime('now'))" +
                ")");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS books (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  isbn TEXT UNIQUE," +
                "  title TEXT NOT NULL," +
                "  author TEXT NOT NULL," +
                "  publisher TEXT," +
                "  category TEXT," +
                "  total_copies INTEGER NOT NULL DEFAULT 1," +
                "  available_copies INTEGER NOT NULL DEFAULT 1," +
                "  shelf TEXT," +
                "  price REAL DEFAULT 0," +
                "  created_at TEXT NOT NULL DEFAULT (datetime('now'))" +
                ")");

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS issues (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  book_id INTEGER NOT NULL REFERENCES books(id)," +
                "  student_id INTEGER NOT NULL REFERENCES students(id)," +
                "  issue_date TEXT NOT NULL," +
                "  due_date TEXT NOT NULL," +
                "  return_date TEXT," +
                "  fine REAL NOT NULL DEFAULT 0," +
                "  fine_paid INTEGER NOT NULL DEFAULT 0," +
                "  status TEXT NOT NULL DEFAULT 'ISSUED' CHECK (status IN ('ISSUED','RETURNED'))," +
                "  issued_by TEXT," +
                "  returned_by TEXT" +
                ")");

            // Helpful indexes
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_issues_student ON issues(student_id, status)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_issues_book ON issues(book_id, status)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_books_title ON books(title)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_books_author ON books(author)");
        }
    }

    private static void seed(Connection conn) throws SQLException {
        boolean hasUsers;
        try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users")) {
            hasUsers = rs.next() && rs.getInt(1) > 0;
        }
        if (hasUsers) {
            return;
        }

        // Default librarian: admin / admin123
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password_hash, role) VALUES (?,?,'ADMIN')")) {
            DBUtil.bind(ps, "admin", SecurityUtil.hashPassword("admin123"));
            ps.executeUpdate();
        }

        // Demo student: student / student123
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (username, password_hash, role) VALUES (?,?,'STUDENT')")) {
            DBUtil.bind(ps, "student", SecurityUtil.hashPassword("student123"));
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO students (user_id, name, email, phone, roll_no, department, semester) VALUES (2,?,?,?,?,?,3)")) {
            DBUtil.bind(ps, "Demo Student", "student@lms.local", "9800000001", "ROLL-001", "Computer Science");
            ps.executeUpdate();
        }

        // Seed books: isbn, title, author, publisher, category, copies, shelf, price
        Object[][] books = {
            {"9780134685991", "Effective Java", "Joshua Bloch", "Addison-Wesley", "Programming", 4, "A1", 850.0},
            {"9780132350884", "Clean Code", "Robert C. Martin", "Prentice Hall", "Programming", 3, "A1", 750.0},
            {"9780596009205", "Head First Java", "Kathy Sierra", "O'Reilly", "Programming", 5, "A2", 650.0},
            {"9780262033848", "Introduction to Algorithms", "Thomas H. Cormen", "MIT Press", "Algorithms", 2, "B1", 1200.0},
            {"9781449373320", "Designing Data-Intensive Applications", "Martin Kleppmann", "O'Reilly", "Databases", 3, "B2", 1400.0},
            {"9789332575404", "Operating System Concepts", "Abraham Silberschatz", "Wiley", "Computer Science", 4, "B3", 900.0},
            {"9789332518746", "Computer Networks", "Andrew S. Tanenbaum", "Pearson", "Networking", 3, "B3", 820.0},
            {"9780070702044", "Database System Concepts", "Henry F. Korth", "McGraw-Hill", "Databases", 4, "B4", 950.0},
            {"9788126554503", "Let Us C", "Yashavant Kanetkar", "BPB Publications", "Programming", 6, "A3", 350.0},
            {"9789352135565", "Data Structures and Algorithms in Java", "Robert Lafore", "Sams Publishing", "Algorithms", 2, "B1", 700.0},
            {"9789387284772", "Spring in Action", "Craig Walls", "Manning", "Frameworks", 2, "C1", 1100.0},
            {"9789351199551", "Head First Servlets and JSP", "Bryan Basham", "O'Reilly", "Web", 3, "C1", 780.0},
            {"9789332585113", "Software Engineering", "Ian Sommerville", "Pearson", "Software", 3, "D1", 880.0},
            {"9788177585863", "Discrete Mathematics", "Kenneth Rosen", "McGraw-Hill", "Mathematics", 5, "D2", 760.0},
            {"9781259009524", "Artificial Intelligence: A Modern Approach", "Stuart Russell", "Pearson", "AI", 2, "E1", 1150.0}
        };
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO books (isbn, title, author, publisher, category, total_copies, available_copies, shelf, price) VALUES (?,?,?,?,?,?,?,?,?)")) {
            for (Object[] b : books) {
                DBUtil.bind(ps, b[0], b[1], b[2], b[3], b[4], b[5], b[5], b[6], b[7]);
                ps.executeUpdate();
            }
        }

        // Demo issue history: 1 active, 1 returned
        String issuedDate = LocalDate.now().minusDays(10).toString();
        String dueDate = LocalDate.now().plusDays(4).toString();
        String pastIssue = LocalDate.now().minusDays(30).toString();
        String pastDue = LocalDate.now().minusDays(16).toString();
        String pastReturn = LocalDate.now().minusDays(15).toString();

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO issues (book_id, student_id, issue_date, due_date, status, issued_by) VALUES (1, 1, ?, ?, 'ISSUED', 'admin')")) {
            DBUtil.bind(ps, issuedDate, dueDate);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO issues (book_id, student_id, issue_date, due_date, return_date, status, issued_by, returned_by) VALUES (2, 1, ?, ?, ?, 'RETURNED', 'admin', 'admin')")) {
            DBUtil.bind(ps, pastIssue, pastDue, pastReturn);
            ps.executeUpdate();
        }
    }

    /** Simple query runner returning affected row count. */
    public static int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    /** Run multiple statements in one transaction. Returns true if all succeed. */
    public static boolean executeBatch(List<String> sqls, List<Object[]> paramsList) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            try {
                for (int i = 0; i < sqls.size(); i++) {
                    try (PreparedStatement ps = conn.prepareStatement(sqls.get(i))) {
                        if (paramsList != null && i < paramsList.size()) {
                            bind(ps, paramsList.get(i));
                        }
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignore) { }
            }
        }
    }

    public static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    /** Execute insert and return the generated row id. */
    public static int getGeneratedKey(PreparedStatement ps) throws SQLException {
        ps.executeUpdate();
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }
        throw new SQLException("Insert succeeded but no generated key was returned");
    }

    /** Scalar int query helper. */
    public static int scalar(String sql, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Delete DB file (used by tests / reset endpoint in dev only). */
    public static synchronized void resetForDev() throws Exception {
        if (!initialized) {
            return;
        }
        File f = new File(DB_PATH);
        if (f.exists()) {
            Files.delete(f.toPath());
        }
        initialized = false;
    }
}
