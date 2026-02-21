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

public class EventServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter(); Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, title, event_date, location, description FROM events ORDER BY event_date DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            out.print("{\"events\":[");
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    out.print(",");
                out.print(String.format(
                        "{\"id\":%d,\"title\":\"%s\",\"event_date\":\"%s\",\"location\":\"%s\",\"description\":\"%s\"}",
                        rs.getInt("id"), escape(rs.getString("title")), rs.getString("event_date"),
                        escape(rs.getString("location")), escape(rs.getString("description"))));
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
                response.getWriter().print("{\"error\":\"Only admins can delete events\"}");
                return;
            }
            
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"ID is required\"}");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM events WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(idStr));
                ps.executeUpdate();
                response.getWriter().print("{\"success\":true,\"message\":\"Event deleted successfully\"}");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            }
            return;
        }
        
        if (!"admin".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"error\":\"Only admins can create events\"}");
            return;
        }
        
        String title = request.getParameter("title");
        String eventDate = request.getParameter("event_date");
        String location = request.getParameter("location");
        String description = request.getParameter("description");
        
        if (title == null || title.trim().isEmpty() || eventDate == null || eventDate.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Title and date are required\"}");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            int userId = (Integer) session.getAttribute("userId");
            String sql = "INSERT INTO events (title, event_date, location, description, created_by) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setString(2, eventDate);
            ps.setString(3, location != null ? location : "");
            ps.setString(4, description != null ? description : "");
            ps.setInt(5, userId);
            ps.executeUpdate();
            
            response.getWriter().print("{\"success\":true,\"message\":\"Event created successfully\"}");
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
