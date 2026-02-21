<%@ page import="java.sql.*" %>
<%@ page import="com.bbj.db.DatabaseConnection" %>
<%
    try (Connection conn = DatabaseConnection.getConnection()) {
        try {
            String sql = "ALTER TABLE users ADD COLUMN gender VARCHAR(50)";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            out.println("<h2>✓ Gender column added successfully!</h2>");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate column")) {
                out.println("<h2>✓ Gender column already exists</h2>");
            } else {
                out.println("<h2>Error: " + e.getMessage() + "</h2>");
            }
        }
        
        // Now check all columns
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getColumns(null, null, "users", null);
        
        out.println("<h3>Current Users Table Columns:</h3>");
        out.println("<table border='1'>");
        out.println("<tr><th>Column Name</th><th>Type</th></tr>");
        
        while (rs.next()) {
            String colName = rs.getString("COLUMN_NAME");
            String colType = rs.getString("TYPE_NAME");
            out.println("<tr><td>" + colName + "</td><td>" + colType + "</td></tr>");
        }
        out.println("</table>");
        
    } catch (Exception e) {
        out.println("<h2>Error: " + e.getMessage() + "</h2>");
        e.printStackTrace(new java.io.PrintWriter(out));
    }
%>
