package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;
import com.bbj.services.CloudinaryStorageService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ProfileServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"error\":\"Not authenticated\"}");
            return;
        }
        
        int userId = (Integer) session.getAttribute("userId");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, first_name, last_name, email, phone, gender, profile_picture FROM users WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                JSONObject json = new JSONObject();
                json.put("id", rs.getInt("id"));
                json.put("first_name", rs.getString("first_name"));
                json.put("last_name", rs.getString("last_name"));
                json.put("email", rs.getString("email"));
                json.put("phone", rs.getString("phone"));
                json.put("gender", rs.getString("gender"));
                json.put("profile_picture", rs.getString("profile_picture"));
                response.getWriter().print(json.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"error\":\"User not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print(new JSONObject().put("error", e.getMessage()).toString());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().println(new JSONObject().put("error", "Not authenticated").toString());
            return;
        }

        response.setContentType("application/json");
        int userId = (Integer) session.getAttribute("userId");

        try {
            if (ServletFileUpload.isMultipartContent(request)) {
                String firstName = null;
                String lastName = null;
                String phone = null;
                String gender = null;
                InputStream uploadedFile = null;
                String fileName = null;

                ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                upload.setFileSizeMax(10 * 1024 * 1024); // 10MB max
                List<FileItem> items = upload.parseRequest(request);

                for (FileItem item : items) {
                    if (item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String fieldValue = item.getString();
                        if ("first_name".equals(fieldName)) firstName = fieldValue;
                        else if ("last_name".equals(fieldName)) lastName = fieldValue;
                        else if ("phone".equals(fieldName)) phone = fieldValue;
                        else if ("gender".equals(fieldName)) gender = fieldValue;
                    } else {
                        fileName = new File(item.getName()).getName();
                        uploadedFile = item.getInputStream();
                        
                        if (item.getSize() > 10 * 1024 * 1024) {
                            response.getWriter().println(new JSONObject().put("error", "File size exceeds 10MB limit").toString());
                            return;
                        }
                    }
                }

                try (Connection conn = DatabaseConnection.getConnection()) {
                    // Update basic profile info
                    String updateSql = "UPDATE users SET first_name=?, last_name=?, phone=?, gender=?";
                    String picturePath = null;
                    
                    if (uploadedFile != null && fileName != null) {
                        try {
                            System.out.println("Attempting to upload profile picture to Cloudinary...");
                            // Upload to Cloudinary
                            picturePath = CloudinaryStorageService.uploadProfilePicture(uploadedFile, fileName, userId);
                            System.out.println("Picture uploaded successfully: " + picturePath);
                            updateSql += ", profile_picture=?";
                        } catch (Exception e) {
                            System.err.println("Cloudinary upload failed: " + e.getMessage());
                            e.printStackTrace();
                            // Continue without picture if upload fails - don't block profile update
                            JSONObject json = new JSONObject();
                            json.put("success", false);
                            json.put("error", "Picture upload failed: " + e.getMessage() + ". Profile data was updated but picture was not saved.");
                            response.getWriter().println(json.toString());
                            return;
                        }
                    }
                    
                    updateSql += " WHERE id = ?";
                    
                    PreparedStatement ps = conn.prepareStatement(updateSql);
                    ps.setString(1, firstName != null ? firstName : "");
                    ps.setString(2, lastName != null ? lastName : "");
                    ps.setString(3, phone != null ? phone : "");
                    ps.setString(4, gender != null ? gender : "");
                    
                    if (picturePath != null) {
                        ps.setString(5, picturePath);
                        ps.setInt(6, userId);
                    } else {
                        ps.setInt(5, userId);
                    }
                    
                    ps.executeUpdate();

                    JSONObject json = new JSONObject();
                    json.put("success", true);
                    json.put("message", "Profile updated successfully");
                    if (picturePath != null) {
                        json.put("picture_path", picturePath);
                    }
                    response.getWriter().println(json.toString());
                }
            } else {
                response.getWriter().println(new JSONObject().put("error", "Invalid request").toString());
            }
        } catch (FileUploadException e) {
            response.getWriter().println(new JSONObject().put("error", "File upload error: " + e.getMessage()).toString());
            e.printStackTrace();
        } catch (Exception e) {
            response.getWriter().println(new JSONObject().put("error", e.getMessage()).toString());
            e.printStackTrace();
        }
    }
}
