<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Search Books"/>
<c:set var="activeNav" value="catalog"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main">
    <header class="page-head">
      <div>
        <h1 class="page-title">🔍 Search Books</h1>
        <p class="page-sub">${total} titles • type to search live</p>
      </div>
      <div class="page-actions">
        <a class="btn btn-outline ripple" href="${pageContext.request.contextPath}/student/my-books">📖 My Books</a>
      </div>
    </header>

    <%@ include file="../common/flash.jsp" %>

    <div class="panel">
      <form class="toolbar" id="catalogToolbar" method="get" action="${pageContext.request.contextPath}/student/books">
        <input class="field__input toolbar__search" type="text" name="q" id="searchInput"
               value="${q}" placeholder="Search by title, author, ISBN or category…" autocomplete="off">
        <select class="field__input toolbar__select" name="category" id="categorySelect">
          <option value="all" ${category == 'all' ? 'selected' : ''}>All Categories</option>
          <c:forEach var="cat" items="${categories}">
            <option value="${cat}" ${category == cat ? 'selected' : ''}><c:out value="${cat}"/></option>
          </c:forEach>
        </select>
        <button class="btn btn-secondary ripple" type="submit">🔍 Search</button>
      </form>

      <div class="catalog-grid" id="catalogGrid">
        <c:forEach var="b" items="${books}">
          <%-- Category-based badge color + emoji --%>
          <c:set var="catLower" value="${fn:toLowerCase(b.category)}"/>
          <c:choose>
            <c:when test="${fn:contains(catLower, 'programming')}">
              <c:set var="catClass" value="chip-cat-blue"/><c:set var="catEmoji" value="💻"/>
            </c:when>
            <c:when test="${fn:contains(catLower, 'database')}">
              <c:set var="catClass" value="chip-cat-indigo"/><c:set var="catEmoji" value="🗄️"/>
            </c:when>
            <c:when test="${fn:contains(catLower, 'algorithm')}">
              <c:set var="catClass" value="chip-cat-violet"/><c:set var="catEmoji" value="🧮"/>
            </c:when>
            <c:when test="${fn:contains(catLower, 'network')}">
              <c:set var="catClass" value="chip-cat-teal"/><c:set var="catEmoji" value="🌐"/>
            </c:when>
            <c:when test="${fn:contains(catLower, 'math')}">
              <c:set var="catClass" value="chip-cat-pink"/><c:set var="catEmoji" value="📐"/>
            </c:when>
            <c:when test="${catLower == 'ai' or fn:contains(catLower, 'intelligence')}">
              <c:set var="catClass" value="chip-cat-amber"/><c:set var="catEmoji" value="🤖"/>
            </c:when>
            <c:when test="${catLower == 'web'}">
              <c:set var="catClass" value="chip-cat-cyan"/><c:set var="catEmoji" value="🕸️"/>
            </c:when>
            <c:when test="${fn:contains(catLower, 'framework')}">
              <c:set var="catClass" value="chip-cat-green"/><c:set var="catEmoji" value="🌱"/>
            </c:when>
            <c:when test="${fn:contains(catLower, 'software') or fn:contains(catLower, 'engineering')}">
              <c:set var="catClass" value="chip-cat-slate"/><c:set var="catEmoji" value="🖥️"/>
            </c:when>
            <c:when test="${fn:contains(catLower, 'computer')}">
              <c:set var="catClass" value="chip-cat-deep"/><c:set var="catEmoji" value="🎓"/>
            </c:when>
            <c:otherwise>
              <c:set var="catClass" value="chip-cyan"/><c:set var="catEmoji" value="📖"/>
            </c:otherwise>
          </c:choose>

          <article class="book-card card-3d" data-tilt>
            <div class="book-card__cover">
              <span class="book-card__emoji">${catEmoji}</span>
              <span class="chip ${catClass} book-card__cat"><c:out value="${b.category}"/></span>
            </div>
            <div class="book-card__body">
              <h3 class="book-card__title"><c:out value="${b.title}"/></h3>
              <p class="book-card__author">✍️ <c:out value="${b.author}"/></p>
              <div class="book-card__meta">
                <span class="muted mono small"><c:out value="${b.isbn}"/></span>
                <%-- 3-state availability: green (>1) / amber (1 left) / red (0) --%>
                <c:choose>
                  <c:when test="${b.availableCopies > 1}">
                    <span class="chip chip-green avail"><span class="avail-dot"></span><c:out value="${b.availableCopies}"/> available</span>
                  </c:when>
                  <c:when test="${b.availableCopies == 1}">
                    <span class="chip chip-amber avail"><span class="avail-dot"></span>Only 1 left!</span>
                  </c:when>
                  <c:otherwise>
                    <span class="chip chip-red"><span class="avail-dot avail-dot--off"></span>Not available</span>
                  </c:otherwise>
                </c:choose>
              </div>
              <div class="book-card__actions">
                <c:if test="${b.availableCopies > 0}">
                  <form method="post" action="${pageContext.request.contextPath}/student/issue"
                        onsubmit="return confirm('Issue this book for 14 days?')">
                    <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
                    <input type="hidden" name="bookId" value="${b.id}">
                    <button class="btn btn-sm btn-primary ripple" type="submit">📘 Issue</button>
                  </form>
                </c:if>
                <a class="btn btn-sm btn-outline ripple" href="${pageContext.request.contextPath}/student/books?q=${b.title}">Details</a>
              </div>
            </div>
          </article>
        </c:forEach>
        <c:if test="${empty books}">
          <div class="empty empty--wide">No books match your search. 🔎</div>
        </c:if>
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

<script>
  window.__LMS__ = {
    ctx: '${pageContext.request.contextPath}',
    csrf: '<%= com.lms.util.CsrfUtil.getToken(session) %>'
  };
</script>
<%@ include file="../common/footer.jsp" %>
