<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${mode == 'add' ? 'Add Book' : 'Edit Book'}"/>
<c:set var="activeNav" value="books"/>
<%@ include file="../common/head.jsp" %>

<div class="layout">
  <%@ include file="../common/sidebar.jsp" %>

  <main class="main main--narrow">
    <header class="page-head">
      <div>
        <h1 class="page-title">${mode == 'add' ? '➕ Add Book' : '✏️ Edit Book'}</h1>
        <p class="page-sub">${mode == 'add' ? 'Add a new title to the library catalog' : 'Update book details and copies'}</p>
      </div>
      <a class="btn btn-outline ripple" href="${pageContext.request.contextPath}/librarian/books">← Back</a>
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
      <form method="post" class="form-grid" action="${pageContext.request.contextPath}/librarian/books/${mode == 'add' ? 'add' : 'edit'}">
        <input type="hidden" name="_csrf" value="<%= com.lms.util.CsrfUtil.getToken(session) %>">
        <c:if test="${mode == 'edit'}">
          <input type="hidden" name="id" value="${book.id}">
        </c:if>

        <label class="field">
          <span class="field__label">Title *</span>
          <input class="field__input" name="title" required minlength="2" value="${book.title}">
        </label>

        <label class="field">
          <span class="field__label">Author *</span>
          <input class="field__input" name="author" required value="${book.author}">
        </label>

        <label class="field">
          <span class="field__label">ISBN</span>
          <input class="field__input" name="isbn" value="${book.isbn}" placeholder="978-…">
        </label>

        <label class="field">
          <span class="field__label">Publisher</span>
          <input class="field__input" name="publisher" value="${book.publisher}">
        </label>

        <label class="field">
          <span class="field__label">Category</span>
          <input class="field__input" name="category" value="${book.category}" list="catList" placeholder="Programming">
          <datalist id="catList">
            <c:forEach var="cat" items="${categories}">
              <option value="${cat}"></option>
            </c:forEach>
          </datalist>
        </label>

        <label class="field">
          <span class="field__label">Shelf</span>
          <input class="field__input" name="shelf" value="${book.shelf}" placeholder="A1">
        </label>

        <label class="field">
          <span class="field__label">Total Copies *</span>
          <input class="field__input" type="number" name="totalCopies" min="1" required value="${book.totalCopies <= 0 ? 1 : book.totalCopies}">
        </label>

        <label class="field">
          <span class="field__label">Price (₹)</span>
          <input class="field__input" type="number" step="0.01" min="0" name="price" value="${book.price}">
        </label>

        <div class="form-actions">
          <button class="btn btn-primary btn-lg ripple" type="submit">
            ${mode == 'add' ? '➕ Add Book' : '💾 Save Changes'}
          </button>
        </div>
      </form>
    </div>
  </main>
</div>

<%@ include file="../common/footer.jsp" %>
