package com.bbj.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        try {
            // Get environment variables (Railway, Docker, or local defaults)
            String host = System.getenv("MYSQLHOST");
            String port = System.getenv("MYSQLPORT");
            String database = System.getenv("MYSQLDATABASE");
            String user = System.getenv("MYSQLUSER");
            String password = System.getenv("MYSQLPASSWORD");

            // Defaults for local testing
            if (host == null || host.isEmpty()) host = "localhost";
            if (port == null || port.isEmpty()) port = "3306";
            if (database == null || database.isEmpty()) database = "BBJDigital_DB";
            if (user == null || user.isEmpty()) user = "root";
            if (password == null) password = ""; // Allow empty password

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

            // Load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(url, user, password);

            System.out.println("✅ Database connection successful!");
            return connection;

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found");
            e.printStackTrace();
            throw new SQLException("Driver not found", e);
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed");
            e.printStackTrace();
            throw e;
        }
    }
}
