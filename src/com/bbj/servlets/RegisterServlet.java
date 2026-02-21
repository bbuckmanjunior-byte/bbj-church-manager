package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String gender = request.getParameter("gender");
            String phone = request.getParameter("phone");
            String password = request.getParameter("password");

            // Debug logging
            System.out.println("DEBUG: firstName=" + firstName);
            System.out.println("DEBUG: lastName=" + lastName);
            System.out.println("DEBUG: gender=" + gender);
            System.out.println("DEBUG: phone=" + phone);
            System.out.println("DEBUG: password=" + (password != null ? password.length() : "null"));

            // Validation with detailed error messages
            if (firstName == null || firstName.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JSONObject json = new JSONObject();
                json.put("message", "First name is required.");
                out.print(json);
                return;
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JSONObject json = new JSONObject();
                json.put("message", "Last name is required.");
                out.print(json);
                return;
            }
            if (gender == null || gender.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JSONObject json = new JSONObject();
                json.put("message", "Gender is required.");
                out.print(json);
                return;
            }
            if (phone == null || phone.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JSONObject json = new JSONObject();
                json.put("message", "Phone is required.");
                out.print(json);
                return;
            }
            if (password == null || password.length() < 8) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JSONObject json = new JSONObject();
                json.put("message", "Password is required and must be at least 8 characters.");
                out.print(json);
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                // Generate email from first name with duplicate checking
                String generatedEmail = generateEmail(conn, firstName);

                // Create username (email-based)
                String username = generatedEmail;

                // Hash password
                String hashedPassword = hash(password);

                // Insert user (columns present in users table) and return generated id
                String sql = "INSERT INTO users (username, email, password, first_name, last_name, gender, phone, role, profile_complete) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'member', TRUE)";
                PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, username);
                ps.setString(2, generatedEmail);
                ps.setString(3, hashedPassword);
                ps.setString(4, firstName.trim());
                ps.setString(5, lastName.trim());
                ps.setString(6, gender.trim());
                ps.setString(7, phone.trim());
                int updated = ps.executeUpdate();
                int userId = -1;
                try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) userId = keys.getInt(1);
                }
                ps.close();

                // Create verification token
                String token = java.util.UUID.randomUUID().toString().replace("-", "");
                java.sql.Timestamp expires = new java.sql.Timestamp(System.currentTimeMillis() + 1000L * 60 * 60 * 24); // 24h
                String insertToken = "INSERT INTO verification_tokens (user_id, token, expires_at) VALUES (?, ?, ?)";
                PreparedStatement tps = conn.prepareStatement(insertToken);
                tps.setInt(1, userId);
                tps.setString(2, token);
                tps.setTimestamp(3, expires);
                tps.executeUpdate();
                tps.close();

                // Return success with generated email and token (token is returned for testing/email simulation)
                response.setStatus(HttpServletResponse.SC_OK);
                JSONObject json = new JSONObject();
                json.put("success", true);
                json.put("email", generatedEmail);
                json.put("verificationToken", token);
                json.put("message", "Account created successfully! A verification token was generated.");
                out.print(json);

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JSONObject json = new JSONObject();
                json.put("message", "Registration failed: " + e.getMessage());
                out.print(json);
                e.printStackTrace();
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject json = new JSONObject();
            json.put("message", "An error occurred during registration");
            out.print(json);
            e.printStackTrace();
        }
    }

    private String generateEmail(Connection conn, String firstName) throws Exception {
        String baseName = firstName.toLowerCase().trim();
        String email = baseName + "@bbj.com";
        int index = 0;

        while (emailExists(conn, email)) {
            email = baseName + index + "@bbj.com";
            index++;
        }

        return email;
    }

    private boolean emailExists(Connection conn, String email) throws Exception {
        String sql = "SELECT COUNT(*) as count FROM users WHERE email = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("count") > 0;
        }
        return false;
    }

    private String hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
