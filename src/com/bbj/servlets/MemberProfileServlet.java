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
import java.util.Random;

public class MemberProfileServlet extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null || !"member".equals(session.getAttribute("role"))) {
            response.sendRedirect("login.html");
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String city = request.getParameter("city");
        String state = request.getParameter("state");
        String postalCode = request.getParameter("postalCode");

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Generate email from first and last name
            String generatedEmail = generateEmail(firstName, lastName);

            // Check if profile exists
            String checkSql = "SELECT id FROM member_profiles WHERE user_id = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setInt(1, userId);
            ResultSet checkRs = checkPs.executeQuery();

            if (checkRs.next()) {
                // Update existing profile
                String updateSql = "UPDATE member_profiles SET first_name=?, last_name=?, phone=?, address=?, city=?, state=?, postal_code=? WHERE user_id=?";
                PreparedStatement updatePs = conn.prepareStatement(updateSql);
                updatePs.setString(1, firstName);
                updatePs.setString(2, lastName);
                updatePs.setString(3, phone);
                updatePs.setString(4, address);
                updatePs.setString(5, city);
                updatePs.setString(6, state);
                updatePs.setString(7, postalCode);
                updatePs.setInt(8, userId);
                updatePs.executeUpdate();

                // Update user table
                String userUpdateSql = "UPDATE users SET first_name=?, last_name=?, phone=?, address=?, profile_complete=TRUE WHERE id=?";
                PreparedStatement userUpdatePs = conn.prepareStatement(userUpdateSql);
                userUpdatePs.setString(1, firstName);
                userUpdatePs.setString(2, lastName);
                userUpdatePs.setString(3, phone);
                userUpdatePs.setString(4, address);
                userUpdatePs.setInt(5, userId);
                userUpdatePs.executeUpdate();
            } else {
                // Insert new profile
                String insertSql = "INSERT INTO member_profiles (user_id, first_name, last_name, phone, address, city, state, postal_code, generated_email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement insertPs = conn.prepareStatement(insertSql);
                insertPs.setInt(1, userId);
                insertPs.setString(2, firstName);
                insertPs.setString(3, lastName);
                insertPs.setString(4, phone);
                insertPs.setString(5, address);
                insertPs.setString(6, city);
                insertPs.setString(7, state);
                insertPs.setString(8, postalCode);
                insertPs.setString(9, generatedEmail);
                insertPs.executeUpdate();

                // Update user table
                String userUpdateSql = "UPDATE users SET first_name=?, last_name=?, phone=?, address=?, email=?, profile_complete=TRUE WHERE id=?";
                PreparedStatement userUpdatePs = conn.prepareStatement(userUpdateSql);
                userUpdatePs.setString(1, firstName);
                userUpdatePs.setString(2, lastName);
                userUpdatePs.setString(3, phone);
                userUpdatePs.setString(4, address);
                userUpdatePs.setString(5, generatedEmail);
                userUpdatePs.setInt(6, userId);
                userUpdatePs.executeUpdate();
            }

            session.setAttribute("firstName", firstName);
            session.setAttribute("lastName", lastName);
            session.setAttribute("email", generatedEmail);
            session.setAttribute("profileComplete", true);

            // Return JSON response with generated email
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println("{\"success\": true, \"generatedEmail\": \"" + generatedEmail + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private String generateEmail(String firstName, String lastName) {
        String baseEmail = firstName.toLowerCase().trim().replace(" ", "");
        
        // Try without index first
        String email = baseEmail + "@bbj.com";
        
        // Check if email exists in database
        int index = 0;
        String checkEmail = email;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            while (emailExists(conn, checkEmail)) {
                checkEmail = baseEmail + index + "@bbj.com";
                index++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return checkEmail;
    }

    private boolean emailExists(Connection conn, String email) throws Exception {
        String sql = "SELECT id FROM users WHERE email = ? OR (SELECT id FROM member_profiles WHERE generated_email = ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, email);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
}
