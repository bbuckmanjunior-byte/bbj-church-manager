package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class TabServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String action = request.getParameter("action");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if ("list".equals(action)) {
                String sql = "SELECT id, name, display_label, icon, visible_to, display_order FROM tabs ORDER BY display_order";
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql);
                
                response.getWriter().print("{\"tabs\":[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) response.getWriter().print(",");
                    response.getWriter().print(String.format(
                            "{\"id\":%d,\"name\":\"%s\",\"display_label\":\"%s\",\"icon\":\"%s\"}",
                            rs.getInt("id"), rs.getString("name"), rs.getString("display_label"), rs.getString("icon")));
                    first = false;
                }
                response.getWriter().print("]}");
            } else {
                response.getWriter().print("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session == null || !"admin".equals(session.getAttribute("role"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"error\":\"Only admins can manage tabs\"}");
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("create".equals(action)) {
            String name = request.getParameter("name");
            String displayLabel = request.getParameter("display_label");
            String icon = request.getParameter("icon");
            
            if (name == null || displayLabel == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"Name and display_label are required\"}");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO tabs (name, display_label, icon, display_order) VALUES (?, ?, ?, (SELECT MAX(display_order) + 1 FROM tabs))";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setString(2, displayLabel);
                ps.setString(3, icon != null ? icon : "");
                ps.executeUpdate();
                
                response.getWriter().print("{\"success\":true,\"message\":\"Tab created successfully\"}");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Invalid action\"}");
        }
    }
}
