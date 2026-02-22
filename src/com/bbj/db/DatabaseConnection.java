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
        // Priority:
        // 1) Explicit JDBC URL via MYSQL_URL / MYSQL_PUBLIC_URL / DB_URL
        // 2) Component env vars: MYSQL_* or DB_* (multiple name variants supported)
        // 3) Local defaults (kept as before for local dev)

        // Helper to return the first non-empty env value for provided keys
        java.util.function.Function<String[], String> firstEnv = (keys) -> {
            for (String k : keys) {
                String v = System.getenv(k);
                if (v != null && !v.isEmpty()) return v;
            }
            return null;
        };

        // Check for full URL first (many providers expose MYSQL_URL)
        String rawUrl = firstEnv.apply(new String[]{"MYSQL_URL", "MYSQL_PUBLIC_URL", "DB_URL", "MYSQLURL"});
        String dbUrl;
        String dbHost = null;
        String dbPort = null;
        String dbName = null;
        String dbUser = null;
        String dbPassword = null;

        if (rawUrl != null && !rawUrl.isEmpty()) {
            // Normalize to JDBC URL if necessary
            if (rawUrl.startsWith("jdbc:")) {
                dbUrl = rawUrl;
            } else if (rawUrl.startsWith("mysql://") || rawUrl.startsWith("mariadb://")) {
                dbUrl = "jdbc:" + rawUrl;
            } else {
                // Fallback - assume the value is a JDBC URL or already usable
                dbUrl = rawUrl;
            }

            // Credentials may still be provided separately
            dbUser = firstEnv.apply(new String[]{"MYSQL_USER", "MYSQLUSER", "DB_USER"});
            dbPassword = firstEnv.apply(new String[]{"MYSQL_PASSWORD", "MYSQLPASSWORD", "MYSQL_ROOT_PASSWORD", "DB_PASSWORD"});
        } else {
            // No URL provided - read components (MYSQL_* then DB_* then defaults)
            dbHost = firstEnv.apply(new String[]{"MYSQL_HOST", "MYSQLHOST", "DB_HOST"});
            dbPort = firstEnv.apply(new String[]{"MYSQL_PORT", "MYSQLPORT", "DB_PORT"});
            dbName = firstEnv.apply(new String[]{"MYSQL_DATABASE", "MYSQLDATABASE", "DB_NAME"});
            dbUser = firstEnv.apply(new String[]{"MYSQL_USER", "MYSQLUSER", "DB_USER"});
            dbPassword = firstEnv.apply(new String[]{"MYSQL_PASSWORD", "MYSQLPASSWORD", "MYSQL_ROOT_PASSWORD", "DB_PASSWORD"});

            if (dbHost == null) dbHost = "localhost";
            if (dbPort == null) dbPort = "1532";
            if (dbName == null) dbName = "church_manager";
            if (dbUser == null) dbUser = "root";
            if (dbPassword == null) dbPassword = "fire@1532";

            dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
        }

        // DEBUG: Log the connection parameters
        System.out.println("===== DATABASE CONNECTION DEBUG =====");
        System.out.println("URL: " + dbUrl);
        System.out.println("User: " + dbUser);
        System.out.println("Password: " + (dbPassword != null && dbPassword.length() > 0 ? "SET" : "NOT SET"));
        System.out.println("====================================");

        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}
