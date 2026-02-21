package com.bbj.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found", e);
        }
        
        // Get database credentials from environment variables (Railway) or use local defaults
        String dbHost = System.getenv("DB_HOST") != null ? System.getenv("DB_HOST") : "localhost";
        String dbPort = System.getenv("DB_PORT") != null ? System.getenv("DB_PORT") : "1532";
        String dbName = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "church_manager";
        String dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
        String dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "fire@1532";
        
        String DB_URL = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
        
        return DriverManager.getConnection(DB_URL, dbUser, dbPassword);
    }
}
