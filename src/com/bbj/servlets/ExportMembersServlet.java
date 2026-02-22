package com.bbj.servlets;

import com.bbj.db.DatabaseConnection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExportMembersServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("role") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("Unauthorized");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("Only admins can export members");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             Workbook workbook = new XSSFWorkbook()) {
            
            // Create sheet
            Sheet sheet = workbook.createSheet("Members");
            
            // Create header row with styling
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            String[] headers = {"ID", "Username", "First Name", "Last Name", "Email", "Phone", "Address", "Gender", "Role", "Profile Complete", "Created At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Set column widths
            sheet.setColumnWidth(0, 5 * 256);
            sheet.setColumnWidth(1, 15 * 256);
            sheet.setColumnWidth(2, 15 * 256);
            sheet.setColumnWidth(3, 15 * 256);
            sheet.setColumnWidth(4, 20 * 256);
            sheet.setColumnWidth(5, 15 * 256);
            sheet.setColumnWidth(6, 25 * 256);
            sheet.setColumnWidth(7, 10 * 256);
            sheet.setColumnWidth(8, 10 * 256);
            sheet.setColumnWidth(9, 15 * 256);
            sheet.setColumnWidth(10, 20 * 256);
            
            // Fetch and populate data
            String sql = "SELECT id, username, first_name, last_name, email, phone, address, gender, role, profile_complete, created_at FROM users ORDER BY first_name";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            
            CellStyle dataStyle = createDataStyle(workbook);
            int rowNum = 1;
            
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                
                Cell idCell = row.createCell(0);
                idCell.setCellValue(rs.getInt("id"));
                idCell.setCellStyle(dataStyle);
                
                Cell usernameCell = row.createCell(1);
                usernameCell.setCellValue(rs.getString("username") != null ? rs.getString("username") : "");
                usernameCell.setCellStyle(dataStyle);
                
                Cell firstNameCell = row.createCell(2);
                firstNameCell.setCellValue(rs.getString("first_name") != null ? rs.getString("first_name") : "");
                firstNameCell.setCellStyle(dataStyle);
                
                Cell lastNameCell = row.createCell(3);
                lastNameCell.setCellValue(rs.getString("last_name") != null ? rs.getString("last_name") : "");
                lastNameCell.setCellStyle(dataStyle);
                
                Cell emailCell = row.createCell(4);
                emailCell.setCellValue(rs.getString("email") != null ? rs.getString("email") : "");
                emailCell.setCellStyle(dataStyle);
                
                Cell phoneCell = row.createCell(5);
                phoneCell.setCellValue(rs.getString("phone") != null ? rs.getString("phone") : "");
                phoneCell.setCellStyle(dataStyle);
                
                Cell addressCell = row.createCell(6);
                addressCell.setCellValue(rs.getString("address") != null ? rs.getString("address") : "");
                addressCell.setCellStyle(dataStyle);
                
                Cell genderCell = row.createCell(7);
                genderCell.setCellValue(rs.getString("gender") != null ? rs.getString("gender") : "");
                genderCell.setCellStyle(dataStyle);
                
                Cell roleCell = row.createCell(8);
                roleCell.setCellValue(rs.getString("role") != null ? rs.getString("role") : "");
                roleCell.setCellStyle(dataStyle);
                
                Cell profileCell = row.createCell(9);
                profileCell.setCellValue(rs.getBoolean("profile_complete") ? "Yes" : "No");
                profileCell.setCellStyle(dataStyle);
                
                Cell createdCell = row.createCell(10);
                createdCell.setCellValue(rs.getString("created_at") != null ? rs.getString("created_at") : "");
                createdCell.setCellStyle(dataStyle);
            }
            
            // Set response headers
            String fileName = "Members_" + new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date()) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            
            // Write workbook to response
            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("Error exporting members: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }
}
