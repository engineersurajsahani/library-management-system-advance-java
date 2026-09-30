<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Issue / Return Books"/>
<c:set var="activeNav" value="issues"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main">
    <header class="page-head">
      <div>
        <h1 class="page-title">🔄 Issue / Return</h1>
        <p class="page-sub">Circulation desk — loan period 14 days, fine ₹5/day</p>
      </div>
    </header>

    <%@ include file="../common/flash.jsp" %>

    <section class="grid-2">
      <div class="panel card-3d" data-tilt>
        <h2 class="panel__title">➕ Issue a Book</h2>
        <form method="post" action="${pageContext.request.contextPath}/librarian/issues/issue" class="form-grid">
          <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">

          <label class="field">
            <span class="field__label">Student</span>
            <select class="field__input" name="studentId" required>
              <option value="">— Select student —</option>
              <c:forEach var="s" items="${students}">
                <option value="${s.id}"><c:out value="${s.name}"/> (<c:out value="${s.rollNo}"/>)</option>
              </c:forEach>
            </select>
          </label>

          <label class="field">
            <span class="field__label">Book</span>
            <select class="field__input" name="bookId" required>
              <option value="">— Select book —</option>
              <c:forEach var="b" items="${availableBooks}">
                <option value="${b.id}"><c:out value="${b.title}"/> — <c:out value="${b.author}"/> [${b.availableCopies} left]</option>
              </c:forEach>
            </select>
          </label>

          <button class="btn btn-primary ripple" type="submit">📘 Issue Book</button>
        </form>
      </div>

      <div class="panel card-3d" data-tilt>
        <h2 class="panel__title">🔎 Quick Return (by Issue ID)</h2>
        <form method="post" action="${pageContext.request.contextPath}/librarian/issues/return" class="form-grid">
          <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
          <label class="field">
            <span class="field__label">Issue ID</span>
            <input class="field__input" name="issueId" type="number" min="1" required placeholder="e.g. 12">
          </label>
          <button class="btn btn-secondary ripple" type="submit">↩️ Return Book</button>
        </form>
        <p class="hint">Fine is calculated automatically: ₹5 per day after the due date.</p>
      </div>
    </section>

    <div class="panel">
      <h2 class="panel__title">📋 Currently Issued (${activeIssuesCount})</h2>
      <form class="toolbar" method="get" action="${pageContext.request.contextPath}/librarian/issues">
        <input class="field__input toolbar__search" type="text" name="q" value="${q}" placeholder="Search student, roll no or book…">
        <button class="btn btn-secondary ripple" type="submit">🔍</button>
      </form>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr><th>ID</th><th>Student</th><th>Book</th><th>Issued</th><th>Due</th><th>Fine</th><th class="ta-r">Action</th></tr>
          </thead>
          <tbody>
            <c:forEach var="i" items="${activeIssues}">
              <tr class="row-in">
                <td class="muted mono">#${i.id}</td>
                <td><strong><c:out value="${i.studentName}"/></strong> <span class="muted">(<c:out value="${i.studentRoll}"/>)</span></td>
                <td><c:out value="${i.bookTitle}"/></td>
                <td><c:out value="${i.issueDate}"/></td>
                <td>
                  <span class="chip ${i.dueDate < today ? 'chip-red' : 'chip-blue'}"><c:out value="${i.dueDate}"/></span>
                </td>
                <td>₹<c:out value="${i.fine}"/></td>
                <td class="ta-r actions-cell">
                  <form class="inline-form" method="post" action="${pageContext.request.contextPath}/librarian/issues/return"
                        onsubmit="return confirm('Mark this book as returned?')">
                    <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
                    <input type="hidden" name="issueId" value="${i.id}">
                    <button class="btn btn-sm btn-outline ripple" type="submit">↩️ Return</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty activeIssues}">
              <tr><td colspan="7" class="empty">No books currently issued.</td></tr>
            </c:if>
          </tbody>
        </table>
      </div>

      <c:if test="${pages > 1}">
        <nav class="pagination">
          <c:forEach begin="1" end="${pages}" var="p">
            <a class="page-btn ${p == page ? 'is-active' : ''}" href="?q=${q}&page=${p}">${p}</a>
          </c:forEach>
        </nav>
      </c:if>
    </div>
  </main>
</div>

<%@ include file="../common/footer.jsp" %>
