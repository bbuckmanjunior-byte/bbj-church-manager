package com.bbj.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        Connection conn = null;

        try {
            // Get environment variables
            String host = System.getenv("MYSQLHOST");
            String port = System.getenv("MYSQLPORT");
            String database = System.getenv("MYSQLDATABASE");
            String user = System.getenv("MYSQLUSER");
            String password = System.getenv("MYSQLPASSWORD");

            // Defaults if not set
            if (host == null) host = "localhost";
            if (port == null) port = "3306";
            if (database == null) database = "railway";
            if (user == null) user = "root";
            if (password == null) password = "";

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&serverTimezone=UTC";

            System.out.println("🌐 Connecting to MySQL at: " + url);

            conn = DriverManager.getConnection(url, user, password);

            System.out.println("✅ Database connected successfully");

            // Optional: Check Cloudinary
            String cloudinaryUrl = System.getenv("CLOUDINARY_URL");
            if (cloudinaryUrl != null && !cloudinaryUrl.isEmpty()) {
                Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);
                try {
                    // Simple test: get configuration
                    cloudinary.config.getApiKey();
                    System.out.println("☁️ Cloudinary connected successfully");
                } catch (Exception e) {
                    System.out.println("⚠️ Cloudinary connection failed: " + e.getMessage());
                }
            } else {
                System.out.println("⚠️ CLOUDINARY_URL not set");
            }

            return conn;

        } catch (Exception e) {
            System.out.println("❌ Database Connection Failed");
            e.printStackTrace();
            throw new SQLException("Database connection failed: " + e.getMessage(), e);
        }
    }
}
