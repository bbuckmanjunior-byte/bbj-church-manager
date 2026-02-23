package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servlet to handle admin management operations.
 * Admins can create other admins and manage admin accounts.
 */
public class AdminManagementServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        // Check authentication and authorization
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"error\":\"Not authenticated\"}");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"error\":\"Only admins can access this resource\"}");
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("list".equals(action)) {
            listAdmins(response);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"Invalid action\"}");
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        // Check authentication and authorization
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"error\":\"Not authenticated\"}");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"error\":\"Only admins can perform admin operations\"}");
            return;
        }
        
        try {
            BufferedReader reader = request.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            JSONObject json = new JSONObject(sb.toString());
            String action = json.optString("action");
            
            if ("create".equals(action)) {
                createAdmin(json, response);
            } else if ("remove".equals(action)) {
                removeAdmin(json, response, request);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"Invalid action\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    private void listAdmins(HttpServletResponse response) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, email, username, first_name, last_name, created_at FROM users WHERE role = 'admin' ORDER BY created_at DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            JSONArray adminsArray = new JSONArray();
            while (rs.next()) {
                JSONObject admin = new JSONObject();
                admin.put("id", rs.getInt("id"));
                admin.put("email", rs.getString("email"));
                admin.put("username", rs.getString("username"));
                admin.put("first_name", rs.getString("first_name"));
                admin.put("last_name", rs.getString("last_name"));
                admin.put("created_at", rs.getTimestamp("created_at").toString());
                adminsArray.put(admin);
            }
            
            JSONObject result = new JSONObject();
            result.put("admins", adminsArray);
            response.getWriter().print(result.toString());
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void createAdmin(JSONObject json, HttpServletResponse response) {
        String email = json.optString("email");
        String username = json.optString("username");
        String firstName = json.optString("first_name");
        String lastName = json.optString("last_name");
        String password = json.optString("password");
        String phone = json.optString("phone", "");
        
        // Validate input
        if (email == null || email.trim().isEmpty() || username == null || username.trim().isEmpty() ||
            firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"All required fields must be provided\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if email already exists
            String checkEmailSql = "SELECT id FROM users WHERE email = ? OR username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkEmailSql);
            checkStmt.setString(1, email.trim());
            checkStmt.setString(2, username.trim());
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().print("{\"error\":\"Email or username already exists\"}");
                return;
            }
            
            // Create new admin
            String sql = "INSERT INTO users (username, password, email, first_name, last_name, phone, role, profile_complete, email_verified) " +
                        "VALUES (?, SHA2(?, 256), ?, ?, ?, ?, 'admin', TRUE, TRUE)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username.trim());
            ps.setString(2, password);
            ps.setString(3, email.trim());
            ps.setString(4, firstName.trim());
            ps.setString(5, lastName.trim());
            ps.setString(6, phone != null ? phone.trim() : null);
            
            ps.executeUpdate();
            response.getWriter().print("{\"success\":true,\"message\":\"Admin created successfully\"}");
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void removeAdmin(JSONObject json, HttpServletResponse response, HttpServletRequest request) {
        Long adminIdObj = json.optLong("admin_id", -1);
        if (adminIdObj == -1) {
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"admin_id is required\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        int adminId = adminIdObj.intValue();
        HttpSession session = request.getSession(false);
        int currentUserId = (Integer) session.getAttribute("userId");
        
        // Don't allow removing yourself
        if (adminId == currentUserId) {
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"You cannot remove yourself as an admin\"}");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Downgrade to member instead of deleting
            String sql = "UPDATE users SET role = 'member' WHERE id = ? AND role = 'admin'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, adminId);
            
            int updated = ps.executeUpdate();
            if (updated > 0) {
                response.getWriter().print("{\"success\":true,\"message\":\"Admin removed successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"error\":\"Admin not found\"}");
            }
        } catch (Exception e) {
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
