<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Student Dashboard"/>
<c:set var="activeNav" value="dashboard"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main">
    <header class="page-head">
      <div>
        <h1 class="page-title">👋 Welcome, <c:out value="${student.name}"/></h1>
        <p class="page-sub"><c:out value="${student.department}"/> • Semester <c:out value="${student.semester}"/> • <c:out value="${student.rollNo}"/></p>
      </div>
      <div class="page-actions">
        <a class="btn btn-primary ripple" href="${pageContext.request.contextPath}/student/books">🔍 Search Books</a>
      </div>
    </header>

    <%@ include file="../common/flash.jsp" %>

    <section class="stats-grid">
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-blue">📚</div>
        <div>
          <div class="stat-card__num">${totalBooks}</div>
          <div class="stat-card__label">Titles in Library</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-green">✅</div>
        <div>
          <div class="stat-card__num">${availableNow}</div>
          <div class="stat-card__label">Copies Available</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-amber">📖</div>
        <div>
          <div class="stat-card__num">${myActive}</div>
          <div class="stat-card__label">Books with Me</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-red">⏰</div>
        <div>
          <div class="stat-card__num">${myOverdue}</div>
          <div class="stat-card__label">Overdue</div>
        </div>
      </div>
    </section>

    <section class="grid-2">
      <div class="panel card-3d" data-tilt>
        <h2 class="panel__title">⏰ Due Soon (next 7 days)</h2>
        <c:choose>
          <c:when test="${empty dueSoon}">
            <p class="empty">You have no books due this week. 🎉</p>
          </c:when>
          <c:otherwise>
            <table class="table">
              <thead><tr><th>Book</th><th>Due Date</th></tr></thead>
              <tbody>
                <c:forEach var="i" items="${dueSoon}">
                  <tr class="row-in">
                    <td><c:out value="${i.bookTitle}"/></td>
                    <td><span class="chip chip-amber"><c:out value="${i.dueDate}"/></span></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </div>

      <div class="panel">
        <h2 class="panel__title">🆕 Recently Added</h2>
        <div class="mini-cards">
          <c:forEach var="b" items="${recentBooks}">
            <a class="mini-card card-3d" data-tilt href="${pageContext.request.contextPath}/student/books?q=${b.title}">
              <span class="mini-card__ico">📗</span>
              <span class="mini-card__title"><c:out value="${b.title}"/></span>
              <span class="mini-card__sub"><c:out value="${b.author}"/></span>
            </a>
          </c:forEach>
        </div>
      </div>
    </section>
  </main>
</div>

<%@ include file="../common/footer.jsp" %>
