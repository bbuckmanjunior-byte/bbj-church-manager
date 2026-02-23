package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class AnnouncementServlet extends HttpServlet {
    private static final String ATTACHMENTS_DIR = "announcements_files";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String[] ALLOWED_EXTENSIONS = {".pdf", ".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".mp3", ".wav"};
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter(); Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id, title, content, file_path, created_at FROM announcements ORDER BY created_at DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            out.print("{\"announcements\":[");
            boolean first = true;
            while (rs.next()) {
                if (!first)
                    out.print(",");
                out.print(String.format("{\"id\":%d,\"title\":\"%s\",\"content\":\"%s\",\"file_path\":%s,\"created_at\":\"%s\"}",
                        rs.getInt("id"), escape(rs.getString("title")), escape(rs.getString("content")),
                        rs.getString("file_path") != null ? "\"" + escape(rs.getString("file_path")) + "\"" : "null",
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

        if (ServletFileUpload.isMultipartContent(request)) {
            // Handle file upload
            handleMultipartUpload(request, response, session);
        } else {
            // Handle regular form (text only)
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            
            // Allow empty text fields
            title = title != null ? title.trim() : "";
            content = content != null ? content.trim() : "";
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                int userId = (Integer) session.getAttribute("userId");
                String sql = "INSERT INTO announcements (title, content, created_by) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, title != null ? title : "");
                ps.setString(2, content != null ? content : "");
                ps.setInt(3, userId);
                ps.executeUpdate();
                
                response.getWriter().print("{\"success\":true,\"message\":\"Announcement created successfully\"}");
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    private void handleMultipartUpload(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        try {
            ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
            upload.setFileSizeMax(MAX_FILE_SIZE);
            List<FileItem> items = upload.parseRequest(request);

            String title = null;
            String content = null;
            String fileName = null;
            InputStream uploadedFile = null;

            for (FileItem item : items) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString();
                    if ("title".equals(fieldName)) title = fieldValue;
                    else if ("content".equals(fieldName)) content = fieldValue;
                } else {
                    fileName = new File(item.getName()).getName();
                    uploadedFile = item.getInputStream();
                    
                    if (item.getSize() > MAX_FILE_SIZE) {
                        response.getWriter().print("{\"error\":\"File size exceeds 50MB limit\"}");
                        return;
                    }
                    
                    // Validate file extension
                    if (!isAllowedFile(fileName)) {
                        response.getWriter().print("{\"error\":\"File type not allowed. Allowed: PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, MP3, WAV\"}");
                        return;
                    }
                }
            }

            // Allow empty text if file is uploaded, but require at least one
            String titleTrim = (title != null ? title.trim() : "");
            String contentTrim = (content != null ? content.trim() : "");
            
            if (titleTrim.isEmpty() && contentTrim.isEmpty() && (uploadedFile == null || fileName == null)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"error\":\"Please provide either text content or upload a file\"}");
                return;
            }

            String filePath = null;
            if (uploadedFile != null && fileName != null) {
                // Create announcements directory if it doesn't exist
                Path uploadDir = Paths.get(getServletContext().getRealPath("/"), ATTACHMENTS_DIR);
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                // Generate unique file name
                String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                Path fileSavePath = uploadDir.resolve(uniqueFileName);

                // Save file
                Files.copy(uploadedFile, fileSavePath);
                filePath = ATTACHMENTS_DIR + "/" + uniqueFileName;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                int userId = (Integer) session.getAttribute("userId");
                String sql = "INSERT INTO announcements (title, content, file_path, created_by) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, title != null ? title : "");
                ps.setString(2, content != null ? content : "");
                ps.setString(3, filePath);
                ps.setInt(4, userId);
                ps.executeUpdate();

                response.getWriter().print("{\"success\":true,\"message\":\"Announcement created successfully\",\"file_path\":\"" + (filePath != null ? filePath : "") + "\"}");
            }
        } catch (FileUploadException e) {
            response.getWriter().print("{\"error\":\"File upload error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } catch (Exception e) {
            response.getWriter().print("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    private boolean isAllowedFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
