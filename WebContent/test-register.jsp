<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Test Registration Form</title>
</head>
<body>
    <h1>Test Registration Form</h1>
    <form action="register" method="POST">
        <input type="text" name="firstName" placeholder="First Name" required>
        <input type="text" name="lastName" placeholder="Last Name" required>
        <select name="gender" required>
            <option value="">Select Gender</option>
            <option value="Male">Male</option>
            <option value="Female">Female</option>
        </select>
        <input type="tel" name="phone" placeholder="Phone" required>
        <input type="password" name="password" placeholder="Password (8+ chars)" required>
        <button type="submit">Register</button>
    </form>
</body>
</html>
