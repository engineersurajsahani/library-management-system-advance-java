<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Profile"/>
<c:set var="activeNav" value="profile"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main main--narrow">
    <header class="page-head">
      <div>
        <h1 class="page-title">👤 My Profile</h1>
        <p class="page-sub">Your details and account settings</p>
      </div>
    </header>

    <%@ include file="../common/flash.jsp" %>

    <c:if test="${not empty errors}">
      <div class="banner banner--error">
        <ul class="banner__list">
          <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
        </ul>
      </div>
    </c:if>

    <div class="panel card-3d" data-tilt>
      <h2 class="panel__title">🎓 Student Details</h2>
      <div class="detail-grid">
        <div class="detail"><span class="detail__label">Name</span><span class="detail__value"><c:out value="${student.name}"/></span></div>
        <div class="detail"><span class="detail__label">Roll No</span><span class="detail__value"><c:out value="${student.rollNo}"/></span></div>
        <div class="detail"><span class="detail__label">Email</span><span class="detail__value"><c:out value="${student.email}"/></span></div>
        <div class="detail"><span class="detail__label">Phone</span><span class="detail__value"><c:out value="${student.phone}"/></span></div>
        <div class="detail"><span class="detail__label">Department</span><span class="detail__value"><c:out value="${student.department}"/></span></div>
        <div class="detail"><span class="detail__label">Semester</span><span class="detail__value"><c:out value="${student.semester}"/></span></div>
      </div>
    </div>

    <div class="panel card-3d" data-tilt>
      <h2 class="panel__title">🔑 Change Password</h2>
      <form method="post" class="form-grid" action="${pageContext.request.contextPath}/student/profile">
        <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
        <label class="field">
          <span class="field__label">Current Password</span>
          <input class="field__input" type="password" name="currentPassword" required>
        </label>
        <label class="field">
          <span class="field__label">New Password</span>
          <input class="field__input" type="password" name="newPassword" required minlength="6">
        </label>
        <label class="field">
          <span class="field__label">Confirm New Password</span>
          <input class="field__input" type="password" name="confirmPassword" required minlength="6">
        </label>
        <div class="form-actions">
          <button class="btn btn-primary ripple" type="submit">💾 Update Password</button>
        </div>
      </form>
    </div>
  </main>
</div>

<%@ include file="../common/footer.jsp" %>
