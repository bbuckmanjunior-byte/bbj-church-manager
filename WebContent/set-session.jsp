<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Initializing...</title>
</head>
<body>
  <script>
    <%
      String firstName = (String) session.getAttribute("firstName");
      String lastName = (String) session.getAttribute("lastName");
      String email = (String) session.getAttribute("email");
      String role = (String) session.getAttribute("role");
      String userId = session.getAttribute("userId") != null ? session.getAttribute("userId").toString() : "";
      String redirectPage = "login.html";
      
      if (firstName != null) {
        out.println("sessionStorage.setItem('firstName', '" + firstName.replace("'", "\\'") + "');");
        out.println("sessionStorage.setItem('lastName', '" + (lastName != null ? lastName.replace("'", "\\'") : "") + "');");
        out.println("sessionStorage.setItem('email', '" + (email != null ? email.replace("'", "\\'") : "") + "');");
        out.println("sessionStorage.setItem('role', '" + (role != null ? role : "") + "');");
        out.println("sessionStorage.setItem('userId', '" + userId + "');");
        
        if ("admin".equalsIgnoreCase(role)) {
          redirectPage = "admin-dashboard.html";
        } else {
          redirectPage = "member-dashboard.html";
        }
      }
      out.println("window.location.href = '" + redirectPage + "';");
    %>
  </script>
</body>
</html>
