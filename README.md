# 📚 Library Management System

A **production-ready** Library Management System built with:

- ☕ **Java 17 + Jakarta Servlets 6 + JSP + JSTL**
- 🗄️ **SQLite** (zero-config database, file-based)
- 🎨 **Material Design • Blue Theme • 3D Animations & Transitions**
- 🐳 **Docker + Render** deployment with persistent disk

---

## ✨ Features

### 👨‍💼 Librarian (Admin)
- Dashboard with live stats: titles, copies, availability, issued, overdue, fines pending/collected
- **Students management**: add (with login creation), edit, activate/deactivate, delete, search, pagination
- **Books management**: add, edit, delete, search by title/author/ISBN/category, categories filter
- **Issue books** to students (14-day loan, availability checks, duplicate-issue guard)
- **Return books** with automatic fine calculation (**₹5/day late**), fine collection marking
- Overdue book tracking and recent activity feed

### 🎓 Student
- Personal dashboard: books with me, overdue count, due-soon widget, recently added books
- **Search books** — server-rendered **+ live AJAX search** (title/author/ISBN/category)
- **Issue books** self-service (max 4 active loans guard)
- **Return books** and see live running fine
- Borrowing history with fine status
- Profile page + **change password**

### 🔐 Security (production-grade)
- PBKDF2-HMAC-SHA256 password hashing (120k iterations, per-user salt)
- CSRF protection on **all** POST requests (synchronizer token + constant-time compare)
- Role-based access control (ADMIN / STUDENT) via servlet filters
- Session fixation protection (session regenerate on login), HttpOnly cookies
- Login rate limiting (10 failures / 10 min / IP)
- SQL injection safe (PreparedStatement everywhere), XSS safe (JSTL `<c:out>` escaping)
- Open-redirect guard on `?next=` parameter

---

## 🚀 Run Locally (Docker — recommended)

```bash
docker build -t lms .
docker run --rm -p 8080:8080 -v lms-data:/data lms
```

Open **http://localhost:8080**

## 🔧 Run Locally (plain Maven + Tomcat 10.1)

```bash
mvn clean package
# deploy target/library.war to Tomcat 10.1 (Jakarta EE 10 required)
```

Ya phir IDE (IntelliJ/Eclipse) me Tomcat 10.1 ke saath project run karein.

---

## ☁️ Deploy on Render

### Option A — Blueprint (one click)
1. Push this repo to GitHub.
2. On Render: **New → Blueprint**, select the repo — `render.yaml` is auto-detected:
   - Docker web service
   - **1 GB persistent disk** mounted at `/data` (SQLite file survives deploys)
   - Health check at `/health`

### Option B — Manual
1. **New → Web Service** → connect your repo.
2. Runtime: **Docker** (Render builds the Dockerfile automatically).
3. Add a **Persistent Disk**: mount path `/data`, size 1 GB.
4. Environment variable: `LMS_DB_PATH=/data/lms.db`
5. Health check path: `/health`
6. Deploy 🚀

> ⚠️ **Important:** Without the persistent disk, SQLite data resets on every deploy/restart.

---

## 🔑 Default Credentials

| Role      | Username  | Password     |
|-----------|-----------|--------------|
| Librarian | `admin`   | `admin123`   |
| Student   | `student` | `student123` |

> Change these immediately after first login (student password can be changed from Profile).

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/lms/
│   │   ├── dao/          # Data access (SQLite, PreparedStatement)
│   │   ├── filter/       # AuthFilter (roles), CsrfFilter
│   │   ├── model/        # JavaBeans: User, Student, Book, Issue
│   │   ├── servlet/      # Controllers (login, health, librarian/, student/)
│   │   └── util/         # DBUtil, SecurityUtil, CsrfUtil, FineUtil
│   ├── webapp/
│   │   ├── css/          # Material blue theme + 3D animations
│   │   ├── js/           # Tilt, ripple, toasts, AJAX search
│   │   ├── WEB-INF/
│   │   │   ├── views/    # JSPs (librarian/, student/, common/)
│   │   │   └── web.xml
│   │   └── index.jsp
│   └── ...
├── Dockerfile            # Multi-stage: Maven build → Tomcat 10.1
└── render.yaml           # Render Blueprint with persistent disk
```

---

## 🧠 Tech Notes

- **SQLite via JDBC** (`sqlite-jdbc`) — schema auto-creates and seeds demo data on first run.
- Issue/return operations are **transactional** — copies can never go negative.
- JSTL taglib URI is `jakarta.tags.core` (Jakarta EE 10 / Tomcat 10.1).
- Fine formula: `₹5 × days overdue`, computed live and frozen at return time.
