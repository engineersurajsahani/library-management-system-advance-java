<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Books"/>
<c:set var="activeNav" value="books"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main">
    <header class="page-head">
      <div>
        <h1 class="page-title">📚 Books</h1>
        <p class="page-sub">${total} titles in the catalog</p>
      </div>
      <div class="page-actions">
        <a class="btn btn-primary ripple" href="${pageContext.request.contextPath}/librarian/books/add">➕ Add Book</a>
      </div>
    </header>

    <%@ include file="../common/flash.jsp" %>

    <div class="panel">
      <form class="toolbar" method="get" action="${pageContext.request.contextPath}/librarian/books">
        <input class="field__input toolbar__search" type="text" name="q" value="${q}"
               placeholder="Search title, author, ISBN…">
        <select class="field__input toolbar__select" name="category">
          <option value="all" ${category == 'all' ? 'selected' : ''}>All Categories</option>
          <c:forEach var="cat" items="${categories}">
            <option value="${cat}" ${category == cat ? 'selected' : ''}><c:out value="${cat}"/></option>
          </c:forEach>
        </select>
        <button class="btn btn-secondary ripple" type="submit">🔍 Search</button>
        <c:if test="${not empty q or category != 'all'}">
          <a class="btn btn-outline ripple" href="${pageContext.request.contextPath}/librarian/books">Clear</a>
        </c:if>
      </form>

      <div class="table-wrap">
        <table class="table">
          <thead>
            <tr>
              <th>#</th><th>Title</th><th>Author</th><th>ISBN</th><th>Category</th>
              <th>Copies</th><th>Shelf</th><th class="ta-r">Actions</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="b" items="${books}" varStatus="st">
              <tr class="row-in">
                <td class="muted">${(page-1)*8 + st.index + 1}</td>
                <td><strong><c:out value="${b.title}"/></strong><div class="muted small"><c:out value="${b.publisher}"/></div></td>
                <td><c:out value="${b.author}"/></td>
                <td class="muted mono"><c:out value="${b.isbn}"/></td>
                <td><span class="chip chip-cyan"><c:out value="${b.category}"/></span></td>
                <td>
                  <span class="chip ${b.availableCopies > 0 ? 'chip-green' : 'chip-red'}">${b.availableCopies}/${b.totalCopies}</span>
                </td>
                <td><c:out value="${b.shelf}"/></td>
                <td class="ta-r actions-cell">
                  <a class="icon-btn" title="Edit" href="${pageContext.request.contextPath}/librarian/books/edit?id=${b.id}">✏️</a>
                  <form class="inline-form" method="post" action="${pageContext.request.contextPath}/librarian/books/delete"
                        onsubmit="return confirm('Delete this book?')">
                    <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
                    <input type="hidden" name="id" value="${b.id}">
                    <button class="icon-btn icon-btn-danger" title="Delete" type="submit">🗑️</button>
                  </form>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty books}">
              <tr><td colspan="8" class="empty">No books found.</td></tr>
            </c:if>
          </tbody>
        </table>
      </div>

      <c:if test="${pages > 1}">
        <nav class="pagination">
          <c:forEach begin="1" end="${pages}" var="p">
            <a class="page-btn ${p == page ? 'is-active' : ''}"
               href="?q=${q}&category=${category}&page=${p}">${p}</a>
          </c:forEach>
        </nav>
      </c:if>
    </div>
  </main>
</div>

<%@ include file="../common/footer.jsp" %>
