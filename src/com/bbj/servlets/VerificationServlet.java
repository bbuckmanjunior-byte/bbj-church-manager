package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class VerificationServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String token = request.getParameter("token");
        JSONObject json = new JSONObject();

        if (token == null || token.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.put("success", false).put("message", "Missing token");
            response.getWriter().println(json.toString());
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT user_id, expires_at FROM verification_tokens WHERE token = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
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
            if (expires != null && expires.before(new Timestamp(System.currentTimeMillis()))) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.put("success", false).put("message", "Token expired");
                response.getWriter().println(json.toString());
                return;
            }

            // Mark user email as verified
            String update = "UPDATE users SET email_verified = TRUE WHERE id = ?";
            PreparedStatement ups = conn.prepareStatement(update);
            ups.setInt(1, userId);
            ups.executeUpdate();

            // Remove token
            String del = "DELETE FROM verification_tokens WHERE token = ?";
            PreparedStatement dp = conn.prepareStatement(del);
            dp.setString(1, token);
            dp.executeUpdate();

            json.put("success", true).put("message", "Email verified");
            response.getWriter().println(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.put("success", false).put("message", "Server error");
            response.getWriter().println(json.toString());
        }
    }
}
