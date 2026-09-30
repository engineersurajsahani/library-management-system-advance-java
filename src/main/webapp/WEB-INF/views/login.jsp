<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Login — Library Management System"/>
<%@ include file="common/head.jsp" %>

<main class="login-wrap">
  <section class="login-card card-3d" data-tilt>
    <div class="login-orb" aria-hidden="true"></div>
    <div class="login-badge">📘</div>
    <h1 class="login-title">Library Portal</h1>
    <p class="login-sub">Sign in to manage or borrow books</p>

    <c:if test="${not empty param.msg and param.msg == 'logged-out'}">
      <div class="banner banner--info">👋 You have been logged out safely.</div>
    </c:if>

    <c:if test="${not empty error}">
      <div class="banner banner--error">⚠️ <c:out value="${error}"/></div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login" class="login-form">
      <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
      <label class="field">
        <span class="field__label">Username</span>
        <input class="field__input" type="text" name="username" required autofocus autocomplete="username" placeholder="admin / student">
      </label>
      <label class="field">
        <span class="field__label">Password</span>
        <input class="field__input" type="password" name="password" required autocomplete="current-password" placeholder="••••••••">
      </label>
      <button class="btn btn-primary btn-block btn-lg ripple" type="submit">Login →</button>
    </form>

    <div class="login-hint">
      <div class="login-hint__row"><span class="chip chip-blue">Librarian</span> admin / admin123</div>
      <div class="login-hint__row"><span class="chip chip-cyan">Student</span> student / student123</div>
    </div>
  </section>
</main>

<%@ include file="common/footer.jsp" %>
