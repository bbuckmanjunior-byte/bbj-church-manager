package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

public class ResetServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"error\":\"Not authenticated\"}");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"error\":\"Only admins can reset data\"}");
            return;
        }
        
        String confirm = request.getParameter("confirm");
        if (!"yes".equals(confirm)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Confirmation required. Pass confirm=yes\"}");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Delete all records from these tables
            String[] tables = {"announcements", "events", "sermons", "member_profiles"};
            
            for (String table : tables) {
                String sql = "DELETE FROM " + table;
                Statement st = conn.createStatement();
                st.execute(sql);
            }
            
            // Delete all members but keep admin account
            String deleteMembersSql = "DELETE FROM users WHERE role = 'member'";
            Statement stmt = conn.createStatement();
            stmt.execute(deleteMembersSql);
            
            response.getWriter().print("{\"success\":true,\"message\":\"All data has been reset successfully. Admin account preserved.\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
