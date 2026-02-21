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

public class AnnouncementServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter(); Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, title, content, created_at FROM announcements ORDER BY created_at DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            out.print("{\"announcements\":[");
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    out.print(",");
                out.print(String.format("{\"id\":%d,\"title\":\"%s\",\"content\":\"%s\",\"created_at\":\"%s\"}",
                        rs.getInt("id"), escape(rs.getString("title")), escape(rs.getString("content")),
                        rs.getString("created_at")));
                first = false;
            }
            out.print("]}");
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
                response.getWriter().print("{\"error\":\"Only admins can delete announcements\"}");
                return;
            }
            
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"ID is required\"}");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM announcements WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(idStr));
                ps.executeUpdate();
                response.getWriter().print("{\"success\":true,\"message\":\"Announcement deleted successfully\"}");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            }
            return;
        }
        
        if (!"admin".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"error\":\"Only admins can create announcements\"}");
            return;
        }
        
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Title and content are required\"}");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int userId = (Integer) session.getAttribute("userId");
            String sql = "INSERT INTO announcements (title, content, created_by) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setInt(3, userId);
            ps.executeUpdate();
            
            response.getWriter().print("{\"success\":true,\"message\":\"Announcement created successfully\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
