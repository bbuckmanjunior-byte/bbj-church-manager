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

        String dbUrl = null;
        String dbUser = firstEnv.apply(new String[]{"MYSQL_USER", "DB_USER"});
        String dbPassword = firstEnv.apply(new String[]{"MYSQL_PASSWORD", "DB_PASSWORD"});

        // Try to build connection string by prioritizing public access
        String dbHost = firstEnv.apply(new String[]{"MYSQL_HOST", "DB_HOST"});
        String dbPort = firstEnv.apply(new String[]{"MYSQL_PORT", "DB_PORT"});
        String dbName = firstEnv.apply(new String[]{"MYSQL_DATABASE", "DB_NAME"});

        // Normalize defaults
        if (dbName == null) dbName = "railway";
        if (dbUser == null) dbUser = "root";
        if (dbPassword == null) dbPassword = "";
        if (dbPort == null) dbPort = "3306";
        if (dbHost == null) dbHost = "localhost";

        System.out.println("DEBUG: dbHost=" + dbHost + ", dbPort=" + dbPort + ", dbName=" + dbName);

        // Railway MySQL connection - try multiple strategies
        SQLException lastException = null;

        // Strategy 1: Try private domain with retries
        if (dbHost.contains("railway.internal")) {
            dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName 
                + "?useSSL=false&serverTimezone=UTC&connectTimeout=5000";
            System.out.println("Strategy 1: Attempting private domain connection");
            lastException = attemptConnection(dbUrl, dbUser, dbPassword, 3);
            if (lastException == null) return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        }

        // Strategy 2: Try public proxy (if Rail way provides one via metadata)
        // Extract just the subdomain from private domain for public proxy
        if (dbHost.contains("railway.internal")) {
            String projectName = dbHost.replace(".railway.internal", "").replace("${{RAILWAY_PRIVATE_DOMAIN}}", "bbjproject");
            String publicProxy = projectName + ".proxy.railway.app";
            dbUrl = "jdbc:mysql://" + publicProxy + ":3306/" + dbName
                + "?useSSL=true&serverTimezone=UTC&connectTimeout=5000&allowPublicKeyRetrieval=true";
            System.out.println("Strategy 2: Attempting public proxy connection to " + publicProxy);
            lastException = attemptConnection(dbUrl, dbUser, dbPassword, 2);
            if (lastException == null) return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        }

        // Strategy 3: Try localhost (for local development)
        if (!dbHost.contains("railway")) {
            dbUrl = "jdbc:mysql://localhost:3306/" + dbName
                + "?useSSL=false&serverTimezone=UTC";
            System.out.println("Strategy 3: Attempting localhost connection");
            lastException = attemptConnection(dbUrl, dbUser, dbPassword, 2);
            if (lastException == null) return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        }

        // All strategies failed
        System.out.println("===== DATABASE CONNECTION FAILED =====");
        System.out.println("All connection strategies failed");
        System.out.println("Last error: " + (lastException != null ? lastException.getMessage() : "Unknown"));
        System.out.println("=====================================");
        
        throw new SQLException("Unable to connect to database using any strategy", lastException);
    }

    private static SQLException attemptConnection(String url, String user, String password, int attempts) {
        for (int i = 1; i <= attempts; i++) {
            try {
                System.out.println("  Attempt " + i + "/" + attempts + ": " + url);
                Connection conn = DriverManager.getConnection(url, user, password);
                System.out.println("  SUCCESS! Connected to database");
                conn.close(); // Just test the connection
                return null;
            } catch (SQLException e) {
                System.out.println("  Failed: " + e.getMessage());
                if (i < attempts) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (i == attempts) {
                    return e;
                }
            }
        }
        return null;
}
