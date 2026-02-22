package com.bbj.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final int MAX_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 2000; // 2 seconds

    public static Connection getConnection() throws SQLException {
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String database = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String password = System.getenv("MYSQLPASSWORD");

        if (host == null) host = "localhost";
        if (port == null) port = "3306";
        if (database == null) database = "railway";
        if (user == null) user = "root";
        if (password == null) password = "";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                     + "?useSSL=false&serverTimezone=UTC";

        SQLException lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                System.out.println("🔗 Attempting to connect to MySQL (" + attempt + "/" + MAX_ATTEMPTS + ") at " + host + ":" + port);
                Connection conn = DriverManager.getConnection(url, user, password);
                System.out.println("✅ Database connection successful!");
                return conn;
            } catch (SQLException e) {
                System.out.println("❌ Attempt " + attempt + " failed: " + e.getMessage());
                lastException = e;
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrupted during retry delay", ie);
                }
            }
        }

        throw new SQLException("Failed to connect to database after " + MAX_ATTEMPTS + " attempts", lastException);
    }
}
