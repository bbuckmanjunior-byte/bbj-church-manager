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

            // Debug output (optional)
            System.out.println("DEBUG: Connecting to database...");
            System.out.println("Host: " + host);
            System.out.println("Port: " + port);
            System.out.println("Database: " + database);

            // MySQL connection URL
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&serverTimezone=UTC";

            // Connect
            conn = DriverManager.getConnection(url, user, password);

            System.out.println("✅ Database Connected Successfully");

        } catch (Exception e) {

            System.out.println("❌ Database Connection Failed");
            e.printStackTrace();

            throw new SQLException("Database connection failed");

        }

        return conn;
    }
}
