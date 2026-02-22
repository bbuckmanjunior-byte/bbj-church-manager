package com.bbj.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {

        try {
            // Try using full JDBC URL first (Railway preferred)
            String jdbcUrl = System.getenv("MYSQL_URL");
            String user = System.getenv("MYSQLUSER");
            String password = System.getenv("MYSQLPASSWORD");

            if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
                System.out.println("✅ Using MYSQL_URL for connection: " + jdbcUrl);
                return DriverManager.getConnection(jdbcUrl, user, password);
            }

            // Fallback: build URL manually
            String host = System.getenv("MYSQLHOST");
            String port = System.getenv("MYSQLPORT");
            String database = System.getenv("MYSQLDATABASE");

            if (host == null || host.isEmpty()) host = "localhost";
            if (port == null || port.isEmpty()) port = "3306";
            if (database == null || database.isEmpty()) database = "railway";
            if (user == null || user.isEmpty()) user = "root";
            if (password == null) password = "";

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&serverTimezone=UTC";

            System.out.println("✅ Connecting using fallback URL: " + url);

            return DriverManager.getConnection(url, user, password);

        } catch (SQLException e) {
            System.out.println("❌ Database Connection Failed");
            e.printStackTrace();
            throw new SQLException("Database connection failed: " + e.getMessage(), e);
        }
    }
}
