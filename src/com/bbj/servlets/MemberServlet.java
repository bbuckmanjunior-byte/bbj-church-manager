package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MemberServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String action = request.getParameter("action");

        try (PrintWriter out = response.getWriter(); Connection conn = DatabaseConnection.getConnection()) {
            HttpSession session = request.getSession(false);
            String role = session != null ? (String) session.getAttribute("role") : "guest";

            if ("list".equals(action)) {
                // List all members with limited info (only name and contact)
                String sql = "SELECT u.id, u.first_name, u.last_name, u.phone FROM users u WHERE u.role = 'member' AND u.profile_complete = TRUE ORDER BY u.first_name";
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql);
                out.print("{\"members\":[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        out.print(",");
                    out.print(String.format(
                            "{\"id\":%d,\"first_name\":\"%s\",\"last_name\":\"%s\",\"phone\":\"%s\"}",
                            rs.getInt("id"), escape(rs.getString("first_name")),
                            escape(rs.getString("last_name")), escape(rs.getString("phone"))));
                    first = false;
                }
                out.print("]}");
            } else if ("admin_list".equals(action)) {
                // List all members with full info - admin only
                if ("admin".equals(role)) {
                    String sql = "SELECT id, username, first_name, last_name, email, phone, address, role, profile_complete FROM users ORDER BY first_name";
                    Statement st = conn.createStatement();
                    ResultSet rs = st.executeQuery(sql);
                    out.print("{\"members\":[");
                    boolean first = true;
                    while (rs.next()) {
                        if (!first)
                            out.print(",");
                        out.print(String.format(
                                "{\"id\":%d,\"username\":\"%s\",\"first_name\":\"%s\",\"last_name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\",\"address\":\"%s\",\"role\":\"%s\"}",
                                rs.getInt("id"), escape(rs.getString("username")), escape(rs.getString("first_name")),
                                escape(rs.getString("last_name")), escape(rs.getString("email")),
                                escape(rs.getString("phone")), escape(rs.getString("address")), rs.getString("role")));
                        first = false;
                    }
                    out.print("]}");
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"error\":\"Unauthorized\"}");
                }
            } else {
                // Default: list all members with full info for backward compatibility
                String sql = "SELECT id, username, first_name, last_name, email, role, created_at FROM users ORDER BY created_at DESC";
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql);
                out.print("{\"members\":[");
                boolean first = true;
                while (rs.next()) {
                    if (!first)
                        out.print(",");
                    out.print(String.format(
                            "{\"id\":%d,\"username\":\"%s\",\"first_name\":\"%s\",\"last_name\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"}",
                            rs.getInt("id"), escape(rs.getString("username")), escape(rs.getString("first_name")),
                            escape(rs.getString("last_name")), escape(rs.getString("email")), rs.getString("role")));
                    first = false;
                }
                out.print("]}");
            }
        } catch (Exception e) {
            response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.getWriter().print("{\"error\":\"Not authenticated\"}");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        String action = request.getParameter("action");
        
        if ("delete".equals(action)) {
            if (!"admin".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().print("{\"error\":\"Only admins can delete members\"}");
                return;
            }
            
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"ID is required\"}");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM users WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(idStr));
                ps.executeUpdate();
                response.getWriter().print("{\"success\":true,\"message\":\"Member deleted successfully\"}");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            }
            return;
        }
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
