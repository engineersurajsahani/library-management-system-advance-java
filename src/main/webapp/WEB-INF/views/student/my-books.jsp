<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Books"/>
<c:set var="activeNav" value="mybooks"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main">
    <header class="page-head">
      <div>
        <h1 class="page-title">📖 My Books</h1>
        <p class="page-sub">Your active loans and borrowing history</p>
      </div>
      <a class="btn btn-outline ripple" href="${pageContext.request.contextPath}/student/books">🔍 Find more</a>
    </header>

    <%@ include file="../common/flash.jsp" %>

    <c:if test="${not empty overdueNotice}">
      <div class="banner banner--error">⏰ You have overdue books — return them soon to avoid extra fine.</div>
    </c:if>

    <div class="panel">
      <h2 class="panel__title">🔄 Currently Borrowed</h2>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr><th>Book</th><th>Issued</th><th>Due</th><th>Running Fine</th><th class="ta-r">Action</th></tr>
          </thead>
          <tbody>
            <c:forEach var="i" items="${activeIssues}">
              <tr class="row-in">
                <td><strong><c:out value="${i.bookTitle}"/></strong><div class="muted small"><c:out value="${i.bookAuthor}"/></div></td>
                <td><c:out value="${i.issueDate}"/></td>
                <td><span class="chip chip-amber"><c:out value="${i.dueDate}"/></span></td>
                <td>₹<c:out value="${i.fine}"/></td>
                <td class="ta-r">
                  <form method="post" action="${pageContext.request.contextPath}/student/return"
                        onsubmit="return confirm('Return this book?')">
                    <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
                    <input type="hidden" name="issueId" value="${i.id}">
                    <button class="btn btn-sm btn-secondary ripple" type="submit">↩️ Return</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty activeIssues}">
              <tr><td colspan="5" class="empty">No active loans. Browse the <a href="${pageContext.request.contextPath}/student/books">catalog</a>.</td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>

    <div class="panel">
      <h2 class="panel__title">🗂️ History</h2>
      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr><th>Book</th><th>Issued</th><th>Returned</th><th>Fine Paid</th></tr>
          </thead>
          <tbody>
            <c:forEach var="i" items="${history}">
              <tr class="row-in">
                <td><c:out value="${i.bookTitle}"/></td>
                <td><c:out value="${i.issueDate}"/></td>
                <td><c:out value="${i.returnDate}"/></td>
                <td>
                  <c:choose>
                    <c:when test="${i.fine > 0}"><span class="chip ${i.finePaid ? 'chip-green' : 'chip-red'}">₹<c:out value="${i.fine}"/> ${i.finePaid ? 'paid' : 'unpaid'}</span></c:when>
                    <c:otherwise><span class="chip chip-green">None</span></c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty history}">
              <tr><td colspan="4" class="empty">No history yet.</td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </main>
</div>

<%@ include file="../common/footer.jsp" %>
