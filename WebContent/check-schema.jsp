<%@ page import="java.sql.*" %>
<%@ page import="com.bbj.db.DatabaseConnection" %>
<%
    try (Connection conn = DatabaseConnection.getConnection()) {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getColumns(null, null, "users", null);
        
        out.println("<h1>Users Table Columns</h1>");
        out.println("<table border='1'>");
        out.println("<tr><th>Column Name</th><th>Type</th><th>Size</th></tr>");
        
        while (rs.next()) {
            String colName = rs.getString("COLUMN_NAME");
            String colType = rs.getString("TYPE_NAME");
            int colSize = rs.getInt("COLUMN_SIZE");
            out.println("<tr><td>" + colName + "</td><td>" + colType + "</td><td>" + colSize + "</td></tr>");
        }
        out.println("</table>");
        
    } catch (Exception e) {
        out.println("<h2>Error: " + e.getMessage() + "</h2>");
        e.printStackTrace(new java.io.PrintWriter(out));
    }
%>
