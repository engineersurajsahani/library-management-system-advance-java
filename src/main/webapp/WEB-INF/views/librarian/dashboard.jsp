<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Librarian Dashboard"/>
<c:set var="activeNav" value="dashboard"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main">
    <header class="page-head">
      <div>
        <h1 class="page-title">Librarian Dashboard</h1>
        <p class="page-sub">Overview of the entire library at a glance</p>
      </div>
      <div class="page-actions">
        <a class="btn btn-primary ripple" href="${pageContext.request.contextPath}/librarian/issues">➕ Issue Book</a>
      </div>
    </header>

    <%@ include file="../common/flash.jsp" %>

    <section class="stats-grid">
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-blue">📚</div>
        <div>
          <div class="stat-card__num">${totalBooks}</div>
          <div class="stat-card__label">Book Titles</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-cyan">📦</div>
        <div>
          <div class="stat-card__num">${totalCopies}</div>
          <div class="stat-card__label">Total Copies</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-green">✅</div>
        <div>
          <div class="stat-card__num">${availableCopies}</div>
          <div class="stat-card__label">Available</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-amber">🔄</div>
        <div>
          <div class="stat-card__num">${activeIssues}</div>
          <div class="stat-card__label">Issued Out</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-red">⏰</div>
        <div>
          <div class="stat-card__num">${overdueCount}</div>
          <div class="stat-card__label">Overdue</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-indigo">🎓</div>
        <div>
          <div class="stat-card__num">${totalStudents}</div>
          <div class="stat-card__label">Students</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-amber">💰</div>
        <div>
          <div class="stat-card__num">₹<c:out value="${outstandingFine}"/></div>
          <div class="stat-card__label">Fines Pending</div>
        </div>
      </div>
      <div class="stat-card card-3d" data-tilt>
        <div class="stat-card__ico stat-ico-green">🧾</div>
        <div>
          <div class="stat-card__num">₹<c:out value="${fineCollected}"/></div>
          <div class="stat-card__label">Fines Collected</div>
        </div>
      </div>
    </section>

    <section class="grid-2">
      <div class="panel card-3d" data-tilt>
        <h2 class="panel__title">⏰ Overdue Books</h2>
        <c:choose>
          <c:when test="${empty overdueList}">
            <p class="empty">🎉 No overdue books. Everyone is on time!</p>
          </c:when>
          <c:otherwise>
            <table class="table">
              <thead>
                <tr><th>Student</th><th>Book</th><th>Due</th><th class="ta-r">Fine</th></tr>
              </thead>
              <tbody>
                <c:forEach var="i" items="${overdueList}">
                  <tr class="row-in">
                    <td><c:out value="${i.studentName}"/> <span class="muted">(<c:out value="${i.studentRoll}"/>)</span></td>
                    <td><c:out value="${i.bookTitle}"/></td>
                    <td><span class="chip chip-red"><c:out value="${i.dueDate}"/></span></td>
                    <td class="ta-r">₹<c:out value="${i.fine}"/></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </div>

      <div class="panel card-3d" data-tilt>
        <h2 class="panel__title">🧾 Recent Activity</h2>
        <c:choose>
          <c:when test="${empty recentIssues}">
            <p class="empty">No activity yet.</p>
          </c:when>
          <c:otherwise>
            <table class="table">
              <thead>
                <tr><th>Student</th><th>Book</th><th>Status</th></tr>
              </thead>
              <tbody>
                <c:forEach var="i" items="${recentIssues}">
                  <tr class="row-in">
                    <td><c:out value="${i.studentName}"/></td>
                    <td><c:out value="${i.bookTitle}"/></td>
                    <td>
                      <span class="chip ${i.status == 'ISSUED' ? 'chip-amber' : 'chip-green'}">
                        ${i.status == 'ISSUED' ? 'Issued' : 'Returned'}
                      </span>
                    </td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </c:otherwise>
        </c:choose>
      </div>
    </section>
  </main>
</div>

<%@ include file="../common/footer.jsp" %>
