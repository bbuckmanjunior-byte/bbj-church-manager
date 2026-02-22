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
        
        // Resolve database configuration from environment variables.
        java.util.function.Function<String[], String> firstEnv = (keys) -> {
            for (String k : keys) {
                String v = System.getenv(k);
                if (v != null && !v.isEmpty()) return v;
            }
            return null;
        };

        // Check if Railway provides MYSQL_URL (public database URL)
        String dbUrl = firstEnv.apply(new String[]{"MYSQL_URL", "MYSQL_PUBLIC_URL", "DATABASE_URL", "DB_URL"});
        String dbUser = firstEnv.apply(new String[]{"MYSQL_USER", "DB_USER"});
        String dbPassword = firstEnv.apply(new String[]{"MYSQL_PASSWORD", "MYSQL_ROOT_PASSWORD", "DB_PASSWORD"});

        if (dbUrl == null || dbUrl.isEmpty()) {
            // No URL provided - build from components
            String dbHost = firstEnv.apply(new String[]{"MYSQL_HOST", "DB_HOST"});
            String dbPort = firstEnv.apply(new String[]{"MYSQL_PORT", "DB_PORT"});
            String dbName = firstEnv.apply(new String[]{"MYSQL_DATABASE", "DB_NAME"});

            if (dbHost == null) dbHost = "localhost";
            if (dbPort == null) dbPort = "3306";
            if (dbName == null) dbName = "church_manager";
            if (dbUser == null) dbUser = "root";
            if (dbPassword == null) dbPassword = "fire@1532";

            // Try public access first (if Railway sets RAILWAY_STATIC_URL)
            String publicDomain = System.getenv("RAILWAY_STATIC_URL");
            if (publicDomain != null && !publicDomain.isEmpty()) {
                // Extract the domain without protocol/port
                String domain = publicDomain.replace("https://", "").replace("http://", "").split(":")[0];
                System.out.println("DEBUG: Attempting public domain connection to: " + domain);
                dbUrl = "jdbc:mysql://" + domain + ":3306/" + dbName + "?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            } else {
                // Fall back to private domain
                System.out.println("DEBUG: Attempting private domain connection to: " + dbHost);
                dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
            }
        }

        // DEBUG: Log the connection parameters
        System.out.println("===== DATABASE CONNECTION DEBUG =====");
        System.out.println("URL: " + dbUrl);
        System.out.println("User: " + dbUser);
        System.out.println("Password: " + (dbPassword != null && dbPassword.length() > 0 ? "SET" : "NOT SET"));
        System.out.println("====================================");

        // Add connection retry logic
        SQLException lastException = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                System.out.println("Database connection attempt " + attempt + "/3...");
                Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                System.out.println("Successfully connected to database!");
                return conn;
            } catch (SQLException e) {
                lastException = e;
                System.out.println("Connection attempt " + attempt + " failed: " + e.getMessage());
                if (attempt < 3) {
                    try {
                        Thread.sleep(2000); // Wait 2 seconds before retrying
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // All retries failed
        throw new SQLException("Failed to connect to database after 3 attempts", lastException);
    }
}
