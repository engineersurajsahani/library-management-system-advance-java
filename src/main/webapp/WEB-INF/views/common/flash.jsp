<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty msg or not empty error or not empty param.msg}">
  <%
    String flashMsg = (String) request.getAttribute("msg");
    String flashErr = (String) request.getAttribute("error");
    if (flashMsg == null) {
      String pm = request.getParameter("msg");
      String pt = request.getParameter("type");
      if (pm != null) {
        if ("error".equals(pt)) { flashErr = pm; } else { flashMsg = pm; }
      }
    }
    pageContext.setAttribute("flashMsg", flashMsg);
    pageContext.setAttribute("flashErr", flashErr);
  %>
  <div class="toast ${not empty flashErr ? 'toast--error' : 'toast--success'}" id="flashToast">
    <span class="toast__ico"><c:out value="${not empty flashErr ? '⚠️' : '✅'}"/></span>
    <span><c:out value="${not empty flashErr ? flashErr : flashMsg}"/></span>
    <button class="toast__close" onclick="this.parentElement.classList.add('toast--out')">&times;</button>
  </div>
</c:if>
