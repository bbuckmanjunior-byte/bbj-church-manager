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
import java.util.UUID;

public class PasswordResetRequestServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String email = request.getParameter("email");
        JSONObject json = new JSONObject();

        if (email == null || email.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.put("success", false).put("message", "Email is required");
            response.getWriter().println(json.toString());
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String qry = "SELECT id FROM users WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(qry);
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                json.put("success", false).put("message", "No account with that email");
                response.getWriter().println(json.toString());
                return;
            }

            int userId = rs.getInt("id");
            String token = UUID.randomUUID().toString().replace("-", "");
            Timestamp expires = new Timestamp(System.currentTimeMillis() + 1000L * 60 * 60 * 24); // 24h

            String ins = "INSERT INTO password_reset_tokens (user_id, token, expires_at) VALUES (?, ?, ?)";
            PreparedStatement ips = conn.prepareStatement(ins);
            ips.setInt(1, userId);
            ips.setString(2, token);
            ips.setTimestamp(3, expires);
            ips.executeUpdate();

            // For now return token in response (simulate sending email)
            json.put("success", true).put("message", "Reset token generated").put("token", token);
            response.getWriter().println(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            json.put("success", false).put("message", "Server error");
            response.getWriter().println(json.toString());
        }
    }
}
