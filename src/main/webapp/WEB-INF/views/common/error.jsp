<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="errorCode" value="${not empty errorCode ? errorCode : (not empty requestScope['jakarta.servlet.error.status_code'] ? requestScope['jakarta.servlet.error.status_code'] : 500)}"/>
<c:set var="errorTitle" value="${not empty errorTitle ? errorTitle : (errorCode == 404 ? 'Page Not Found' : (errorCode == 403 ? 'Access Denied' : 'Something Went Wrong'))}"/>
<c:set var="errorDesc" value="${not empty errorDesc ? errorDesc : (errorCode == 404 ? 'The page you are looking for does not exist or was moved.' : (errorCode == 403 ? 'You do not have permission to view this page.' : 'An unexpected error occurred. Please try again.'))}"/>
<!DOCTYPE html>
<html lang="en" class="theme-blue">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${errorCode} — Library Management System</title>
  <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;600;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">
</head>
<body class="app-body error-body">
  <main class="error-wrap">
    <section class="error-card card-3d" data-tilt>
      <div class="error-code">${errorCode}</div>
      <h1 class="error-title">${errorTitle}</h1>
      <p class="error-sub">${errorDesc}</p>
      <div class="error-actions">
        <a class="btn btn-primary ripple" href="${pageContext.request.contextPath}/login">🏠 Home</a>
        <a class="btn btn-outline ripple" href="javascript:history.back()">← Go Back</a>
      </div>
    </section>
  </main>
</body>
</html>
