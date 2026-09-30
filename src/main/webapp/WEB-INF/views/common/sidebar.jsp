<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="sidebar" id="sidebar">
  <div class="sidebar__brand card-3d" data-tilt>
    <div class="brand-orb" aria-hidden="true"></div>
    <span class="brand-text">LMS<span class="brand-accent">.</span></span>
  </div>

  <nav class="sidebar__nav">
    <c:choose>
      <c:when test="${role == 'ADMIN'}">
        <a class="nav-link ${activeNav == 'dashboard' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/librarian/dashboard">
          <span class="nav-ico">📊</span> Dashboard
        </a>
        <a class="nav-link ${activeNav == 'books' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/librarian/books">
          <span class="nav-ico">📚</span> Books
        </a>
        <a class="nav-link ${activeNav == 'students' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/librarian/students">
          <span class="nav-ico">🎓</span> Students
        </a>
        <a class="nav-link ${activeNav == 'issues' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/librarian/issues">
          <span class="nav-ico">🔄</span> Issue / Return
        </a>
      </c:when>
      <c:otherwise>
        <a class="nav-link ${activeNav == 'dashboard' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/student/dashboard">
          <span class="nav-ico">📊</span> Dashboard
        </a>
        <a class="nav-link ${activeNav == 'catalog' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/student/books">
          <span class="nav-ico">🔍</span> Search Books
        </a>
        <a class="nav-link ${activeNav == 'mybooks' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/student/my-books">
          <span class="nav-ico">📖</span> My Books
        </a>
        <a class="nav-link ${activeNav == 'profile' ? 'is-active' : ''}" href="${pageContext.request.contextPath}/student/profile">
          <span class="nav-ico">👤</span> Profile
        </a>
      </c:otherwise>
    </c:choose>
  </nav>

  <form class="sidebar__logout" method="post" action="${pageContext.request.contextPath}/logout">
    <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
    <button type="submit" class="btn btn-outline btn-block ripple">
      <span class="nav-ico">🚪</span> Logout
    </button>
  </form>
</aside>
<button class="sidebar-toggle ripple" id="sidebarToggle" aria-label="Toggle menu">☰</button>
