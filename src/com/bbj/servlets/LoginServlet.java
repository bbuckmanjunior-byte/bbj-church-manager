package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null) {
            response.sendRedirect("login.html?error=missing");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            String sql = "SELECT id, username, password, email, first_name, last_name, role, profile_complete " +
                    "FROM users WHERE username = ? OR email = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String stored = rs.getString("password");
                String hashed = hash(password);

                if (stored.equalsIgnoreCase(hashed)) {

                    HttpSession session = request.getSession();
                    session.setAttribute("userId", rs.getInt("id"));
                    session.setAttribute("username", rs.getString("username"));
                    session.setAttribute("email", rs.getString("email"));
                    session.setAttribute("firstName", rs.getString("first_name"));
                    session.setAttribute("lastName", rs.getString("last_name"));

                    String role = rs.getString("role");
                    session.setAttribute("role", role);

                    boolean profileComplete = rs.getBoolean("profile_complete");
                    session.setAttribute("profileComplete", profileComplete);

                    if ("admin".equalsIgnoreCase(role)) {
                        response.sendRedirect("set-session.jsp");
                    } else {
                        if (!profileComplete) {
                            response.sendRedirect("member-profile.html");
                        } else {
                            response.sendRedirect("set-session.jsp");
                        }
                    }
                    return;
                }
            }

            response.sendRedirect("login.html?error=invalid");

        } catch (Exception e) {

            System.out.println("========== LOGIN ERROR ==========");
            e.printStackTrace();

            response.setContentType("text/plain");
            response.getWriter().println("Database Error:");
            e.printStackTrace(response.getWriter());
        }
    }   // ✅ THIS BRACE WAS MISSING

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.html");
    }

    private String hash(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}