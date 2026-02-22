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

            if (host == null) host = "localhost";
            if (port == null) port = "3306";
            if (database == null) database = "railway";
            if (user == null) user = "root";
            if (password == null) password = "";

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=false&serverTimezone=UTC";

            Connection conn_result = DriverManager.getConnection(url, user, password);

            return conn_result;

        } catch (Exception e) {

            System.out.println("❌ Database Connection Failed");
            e.printStackTrace();

            throw new SQLException("Database connection failed: " + e.getMessage(), e);

        }

    }
}
