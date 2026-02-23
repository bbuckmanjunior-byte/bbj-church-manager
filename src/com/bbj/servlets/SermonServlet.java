package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class SermonServlet extends HttpServlet {
    private static final String SERMON_UPLOAD_DIR = "sermons";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null || !"admin".equals(session.getAttribute("role"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().println(new JSONObject().put("error", "Unauthorized").toString());
            return;
        }

        response.setContentType("application/json");
        
        String action = request.getParameter("action");
        
        if ("delete".equals(action)) {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println(new JSONObject().put("error", "ID is required").toString());
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                int id = Integer.parseInt(idStr);
                
                // Get file path before deleting
                String sql = "SELECT file_path FROM sermons WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    String filePath = rs.getString("file_path");
                    
                    // Delete from database
                    String deleteSql = "DELETE FROM sermons WHERE id = ?";
                    PreparedStatement deletePs = conn.prepareStatement(deleteSql);
                    deletePs.setInt(1, id);
                    deletePs.executeUpdate();
                    
                    // Delete file
                    try {
                        Path sermonPath = Paths.get(getServletContext().getRealPath("/"), filePath);
                        if (Files.exists(sermonPath)) {
                            Files.delete(sermonPath);
                        }
                    } catch (Exception e) {
                        // Ignore file deletion errors
                    }
                    
                    response.getWriter().println(new JSONObject().put("success", true).put("message", "Sermon deleted successfully").toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().println(new JSONObject().put("error", "Sermon not found").toString());
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println(new JSONObject().put("error", e.getMessage()).toString());
            }
            return;
        }

        try {
            if (ServletFileUpload.isMultipartContent(request)) {
                int userId = (int) session.getAttribute("userId");
                String title = null;
                String description = null;
                String fileType = null;
                long fileSize = 0;
                String fileName = null;
                InputStream uploadedFile = null;

                ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
                upload.setFileSizeMax(100 * 1024 * 1024); // 100MB max
                List<FileItem> items = upload.parseRequest(request);

                for (FileItem item : items) {
                    if (item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String fieldValue = item.getString();
                        if ("title".equals(fieldName)) title = fieldValue;
                        else if ("description".equals(fieldName)) description = fieldValue;
                    } else {
                        fileName = new File(item.getName()).getName();
                        fileType = item.getContentType();
                        fileSize = item.getSize();
                        uploadedFile = item.getInputStream();

                        if (fileSize > 100 * 1024 * 1024) {
                            throw new Exception("File size exceeds 100MB limit");
                        }
                    }
                }

                if (title == null || title.trim().isEmpty() || uploadedFile == null) {
                    JSONObject json = new JSONObject();
                    json.put("success", false);
                    json.put("error", "Title and file are required");
                    response.getWriter().println(json.toString());
                    return;
                }

                // Create sermons directory if it doesn't exist
                Path uploadDir = Paths.get(getServletContext().getRealPath("/"), SERMON_UPLOAD_DIR);
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                // Generate unique file name
                String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                Path filePath = uploadDir.resolve(uniqueFileName);

                // Save file
                Files.copy(uploadedFile, filePath);

                // Save to database
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "INSERT INTO sermons (title, description, file_path, file_size, file_type, uploaded_by) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, title);
                    ps.setString(2, description);
                    ps.setString(3, SERMON_UPLOAD_DIR + "/" + uniqueFileName); // Relative path
                    ps.setLong(4, fileSize);
                    ps.setString(5, fileType);
                    ps.setInt(6, userId);
                    ps.executeUpdate();

                    JSONObject json = new JSONObject();
                    json.put("success", true);
                    json.put("message", "Sermon uploaded successfully");
                    json.put("fileName", uniqueFileName);
                    response.getWriter().println(json.toString());
                }
            } else {
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("error", "Invalid request");
                response.getWriter().println(json.toString());
            }
        } catch (FileUploadException e) {
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", "File upload error: " + e.getMessage());
            response.getWriter().println(json.toString());
            e.printStackTrace();
        } catch (Exception e) {
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("error", "Error: " + e.getMessage());
            response.getWriter().println(json.toString());
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        String action = request.getParameter("action");

        if ("list".equals(action)) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT id, title, description, file_path, file_size, file_type, uploaded_by, created_at FROM sermons ORDER BY created_at DESC";
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();

                StringBuilder json = new StringBuilder("{\"sermons\": [");
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    first = false;
                    json.append("{");
                    json.append("\"id\":").append(rs.getInt("id")).append(",");
                    json.append("\"title\":\"").append(escapeJson(rs.getString("title"))).append("\",");
                    json.append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",");
                    json.append("\"file_path\":\"").append(escapeJson(rs.getString("file_path"))).append("\",");
                    json.append("\"file_size\":").append(rs.getLong("file_size")).append(",");
                    json.append("\"file_type\":\"").append(escapeJson(rs.getString("file_type"))).append("\",");
                    json.append("\"created_at\":\"").append(rs.getString("created_at")).append("\"");
                    json.append("}");
                }
                json.append("]}");

                response.getWriter().println(json.toString());
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println(new JSONObject().put("error", e.getMessage()).toString());
            }
        } else {
            // Download sermon file
            String filePath = request.getParameter("file");
            if (filePath != null && !filePath.isEmpty()) {
                try {
                    Path sermonPath = Paths.get(getServletContext().getRealPath("/"), filePath);
                    if (Files.exists(sermonPath)) {
                        String fileName = sermonPath.getFileName().toString();
                        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
                        response.setContentType("application/octet-stream");
                        Files.copy(sermonPath, response.getOutputStream());
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    e.printStackTrace();
                }
            }
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}

