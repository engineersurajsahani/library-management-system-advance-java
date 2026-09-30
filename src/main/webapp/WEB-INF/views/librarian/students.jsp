<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Students"/>
<c:set var="activeNav" value="students"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main">
    <header class="page-head">
      <div>
        <h1 class="page-title">🎓 Students</h1>
        <p class="page-sub">${total} registered students</p>
      </div>
      <div class="page-actions">
        <a class="btn btn-primary ripple" href="add">➕ Add Student</a>
      </div>
    </header>

    <%@ include file="../common/flash.jsp" %>

    <div class="panel">
      <form class="toolbar" method="get" action="${pageContext.request.contextPath}/librarian/students">
        <input class="field__input toolbar__search" type="text" name="q" value="${q}"
               placeholder="Search by name, email or roll no…">
        <button class="btn btn-secondary ripple" type="submit">🔍 Search</button>
        <c:if test="${not empty q}">
          <a class="btn btn-outline ripple" href="${pageContext.request.contextPath}/librarian/students">Clear</a>
        </c:if>
      </form>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>#</th><th>Name</th><th>Roll No</th><th>Department</th><th>Sem</th>
              <th>Email</th><th>Status</th><th class="ta-r">Actions</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="s" items="${students}" varStatus="st">
              <tr class="row-in">
                <td class="muted">${(page-1)*8 + st.index + 1}</td>
                <td><strong><c:out value="${s.name}"/></strong></td>
                <td><span class="chip chip-blue"><c:out value="${s.rollNo}"/></span></td>
                <td><c:out value="${s.department}"/></td>
                <td><c:out value="${s.semester}"/></td>
                <td class="muted"><c:out value="${s.email}"/></td>
                <td>
                  <span class="chip ${s.active ? 'chip-green' : 'chip-red'}">${s.active ? 'Active' : 'Inactive'}</span>
                </td>
                <td class="ta-r actions-cell">
                  <a class="icon-btn" title="Edit" href="edit?id=${s.id}">✏️</a>
                  <form class="inline-form" method="post" action="toggle"
                        onsubmit="return confirm('Toggle active status?')">
                    <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
                    <input type="hidden" name="id" value="${s.id}">
                    <button class="icon-btn" title="Toggle" type="submit">${s.active ? '🚫' : '✔️'}</button>
    </form>
                  <form class="inline-form" method="post" action="delete"
                        onsubmit="return confirm('Delete this student? This cannot be undone.')">
                    <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
                    <input type="hidden" name="id" value="${s.id}">
                    <button class="icon-btn icon-btn-danger" title="Delete" type="submit">🗑️</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty students}">
              <tr><td colspan="8" class="empty">No students found.</td></tr>
            </c:if>
          </tbody>
        </table>
      </div>

      <c:if test="${pages > 1}">
        <nav class="pagination">
          <c:forEach begin="1" end="${pages}" var="p">
            <a class="page-btn ${p == page ? 'is-active' : ''}"
               href="?q=${q}&page=${p}">${p}</a>
          </c:forEach>
        </nav>
      </c:if>
    </div>
  </main>
</div>

<%@ include file="../common/footer.jsp" %>
