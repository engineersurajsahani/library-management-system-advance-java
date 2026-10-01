<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${mode == 'add' ? 'Add Student' : 'Edit Student'}"/>
<c:set var="activeNav" value="students"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main main--narrow">
    <header class="page-head">
      <div>
        <h1 class="page-title">${mode == 'add' ? '➕ Add Student' : '✏️ Edit Student'}</h1>
        <p class="page-sub">${mode == 'add' ? 'Create a new student account with login credentials' : 'Update student details'}</p>
      </div>
      <a class="btn btn-outline ripple" href="${pageContext.request.contextPath}/librarian/students">← Back</a>
    </header>

    <c:if test="${not empty errors}">
      <div class="banner banner--error">
        <strong>Please fix the following:</strong>
        <ul class="banner__list">
          <c:forEach var="e" items="${errors}"><li><c:out value="${e}"/></li></c:forEach>
        </ul>
      </div>
    </c:if>

    <div class="panel card-3d" data-tilt>
      <form method="post" class="form-grid" action="${pageContext.request.contextPath}/librarian/students/${mode == 'add' ? 'add' : 'edit'}">
        <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
        <c:if test="${mode == 'edit'}">
          <input type="hidden" name="id" value="${student.id}">
        </c:if>

        <label class="field">
          <span class="field__label">Full Name *</span>
          <input class="field__input" name="name" required minlength="3" value="${student.name}">
        </label>

        <label class="field">
          <span class="field__label">Roll No *</span>
          <input class="field__input" name="rollNo" required value="${student.rollNo}" placeholder="ROLL-002">
        </label>

        <label class="field">
          <span class="field__label">Email *</span>
          <input class="field__input" type="email" name="email" required value="${student.email}">
        </label>

        <label class="field">
          <span class="field__label">Phone</span>
          <input class="field__input" name="phone" value="${student.phone}">
        </label>

        <label class="field">
          <span class="field__label">Department</span>
          <input class="field__input" name="department" value="${student.department}" placeholder="Computer Science">
        </label>

        <label class="field">
          <span class="field__label">Semester</span>
          <select class="field__input" name="semester">
            <c:forEach begin="1" end="8" var="sem">
              <option value="${sem}" ${student.semester == sem ? 'selected' : ''}>Semester ${sem}</option>
            </c:forEach>
          </select>
        </label>

        <c:if test="${mode == 'add'}">
          <label class="field">
            <span class="field__label">Username *</span>
            <input class="field__input" name="username" required minlength="4" placeholder="rahul.k">
          </label>
          <label class="field">
            <span class="field__label">Password *</span>
            <input class="field__input" type="password" name="password" required minlength="6" placeholder="min 6 chars">
          </label>
        </c:if>

        <div class="form-actions">
          <button class="btn btn-primary btn-lg ripple" type="submit">
            ${mode == 'add' ? '➕ Create Student' : '💾 Save Changes'}
          </button>
        </div>
      </form>
    </div>
  </main>
</div>

<%@ include file="../common/footer.jsp" %>
