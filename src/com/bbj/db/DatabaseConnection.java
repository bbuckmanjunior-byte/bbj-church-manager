package com.bbj.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {

        Connection conn = null;

        try {

            // Get Railway environment variables
            String host = System.getenv("MYSQLHOST");
            String port = System.getenv("MYSQLPORT");
            String database = System.getenv("MYSQLDATABASE");
            String user = System.getenv("MYSQLUSER");
            String password = System.getenv("MYSQLPASSWORD");

            // Debug output - list ALL environment variables that start with MYSQL
            System.out.println("\n========== DATABASE CONNECTION DEBUG ==========");
            System.out.println("MYSQLHOST: " + host);
            System.out.println("MYSQLPORT: " + port);
            System.out.println("MYSQLDATABASE: " + database);
            System.out.println("MYSQLUSER: " + user);
            System.out.println("MYSQLPASSWORD: " + (password != null ? "***HIDDEN***" : "null"));
            
            // Check for alternate naming conventions (in case Railway uses different names)
            System.out.println("\nAlternate env var checks:");
            System.out.println("RAILWAY_PRIVATE_DOMAIN: " + System.getenv("RAILWAY_PRIVATE_DOMAIN"));
            System.out.println("DB_HOST: " + System.getenv("DB_HOST"));
            System.out.println("MYSQL_HOST: " + System.getenv("MYSQL_HOST"));
            System.out.println("================================================\n");

            // Validate that we have all required values
            if (host == null || host.isEmpty() || host.contains("${{")) {
                throw new SQLException("MYSQLHOST not set or is a template variable: " + host);
            }
            if (port == null || port.isEmpty()) {
                throw new SQLException("MYSQLPORT not set");
            }
            if (database == null || database.isEmpty()) {
                throw new SQLException("MYSQLDATABASE not set");
            }
            if (user == null || user.isEmpty()) {
                throw new SQLException("MYSQLUSER not set");
            }

            // MySQL connection URL
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&serverTimezone=UTC&connectTimeout=10000";

            System.out.println("Connecting to: " + url);
            System.out.println("User: " + user);

            // Connect
            conn = DriverManager.getConnection(url, user, password);

            System.out.println("✅ Database Connected Successfully\n");

        } catch (Exception e) {

            System.out.println("❌ Database Connection Failed");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();

            throw new SQLException("Database connection failed: " + e.getMessage(), e);

        }

        return conn;
    }
}
