package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class PasswordResetServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String token = request.getParameter("token");
        String newPassword = request.getParameter("password");
        JSONObject json = new JSONObject();

        if (token == null || token.trim().isEmpty() || newPassword == null || newPassword.length() < 8) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.put("success", false).put("message", "Invalid request");
            response.getWriter().println(json.toString());
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String qry = "SELECT user_id, expires_at, used FROM password_reset_tokens WHERE token = ?";
            PreparedStatement ps = conn.prepareStatement(qry);
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.put("success", false).put("message", "Invalid token");
                response.getWriter().println(json.toString());
                return;
            }

            int userId = rs.getInt("user_id");
            Timestamp expires = rs.getTimestamp("expires_at");
            boolean used = rs.getBoolean("used");
            if (used) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.put("success", false).put("message", "Token already used");
                response.getWriter().println(json.toString());
                return;
            }
            if (expires != null && expires.before(new Timestamp(System.currentTimeMillis()))) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.put("success", false).put("message", "Token expired");
                response.getWriter().println(json.toString());
                return;
            }

            // Update password
            String hashed = hash(newPassword);
            String upd = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement ups = conn.prepareStatement(upd);
            ups.setString(1, hashed);
            ups.setInt(2, userId);
            ups.executeUpdate();

            // Mark token used
            String mark = "UPDATE password_reset_tokens SET used = TRUE WHERE token = ?";
            PreparedStatement mps = conn.prepareStatement(mark);
            mps.setString(1, token);
            mps.executeUpdate();

            json.put("success", true).put("message", "Password updated");
            response.getWriter().println(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.put("success", false).put("message", "Server error");
            response.getWriter().println(json.toString());
        }
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
