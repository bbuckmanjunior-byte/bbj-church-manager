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
        
        java.util.function.Function<String[], String> firstEnv = (keys) -> {
            for (String k : keys) {
                String v = System.getenv(k);
                if (v != null && !v.isEmpty() && !v.contains("$")) return v;
            }
            return null;
        };

        String dbUser = firstEnv.apply(new String[]{"MYSQL_USER", "DB_USER"});
        String dbPassword = firstEnv.apply(new String[]{"MYSQL_PASSWORD", "DB_PASSWORD"});
        String dbHost = firstEnv.apply(new String[]{"MYSQL_HOST", "DB_HOST"});
        String dbPort = firstEnv.apply(new String[]{"MYSQL_PORT", "DB_PORT"});
        String dbName = firstEnv.apply(new String[]{"MYSQL_DATABASE", "DB_NAME"});

        // Normalize defaults
        if (dbName == null) dbName = "railway";
        if (dbUser == null) dbUser = "root";
        if (dbPassword == null) dbPassword = "";
        if (dbPort == null) dbPort = "3306";
        if (dbHost == null) dbHost = "localhost";

        System.out.println("DEBUG: Attempting to connect using dbHost=" + dbHost);

        // Simple approach: just try the connection directly with retry logic
        SQLException lastException = null;
        String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&serverTimezone=UTC&connectTimeout=5000";

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                System.out.println("Connection attempt " + attempt + "/3: " + dbUrl);
                Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                System.out.println("SUCCESS: Connected to database!");
                return conn;
            } catch (SQLException e) {
                lastException = e;
                System.out.println("Attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < 3) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        throw new SQLException("Failed to connect to database after 3 attempts", lastException);
    }
}
