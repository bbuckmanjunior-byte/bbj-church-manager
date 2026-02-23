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

            String url;
            String user;
            String password;

            // Check for TiDB Cloud configuration via environment variables
            String mysqlHost = System.getenv("MYSQLHOST");
            String mysqlPort = System.getenv("MYSQLPORT");
            String mysqlDatabase = System.getenv("MYSQLDATABASE");
            String mysqlUser = System.getenv("MYSQLUSER");
            String mysqlPassword = System.getenv("MYSQLPASSWORD");
            
            // Also check for DATABASE_URL format (mysql://user:pass@host:port/database)
            String databaseUrl = System.getenv("DATABASE_URL");

            // ONLINE (TiDB Cloud via environment variables)
            if (mysqlHost != null && !mysqlHost.isEmpty() && mysqlUser != null && !mysqlUser.isEmpty()) {

                user = mysqlUser;
                password = mysqlPassword != null ? mysqlPassword : "";
                String host = mysqlHost;
                String port = mysqlPort != null ? mysqlPort : "4000";
                String database = mysqlDatabase != null ? mysqlDatabase : "bbjdigital_db";

                url = "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?sslMode=REQUIRED&serverTimezone=UTC";

                System.out.println("Connecting to TiDB Cloud via environment variables...");

            // ONLINE (Render + TiDB Cloud via DATABASE_URL)
            } else if (databaseUrl != null && !databaseUrl.isEmpty()) {

                databaseUrl = databaseUrl.replace("mysql://", "");

                String[] parts = databaseUrl.split("@");
                String[] credentials = parts[0].split(":");
                String[] hostParts = parts[1].split("/");

                user = credentials[0];
                password = credentials[1];

                String hostPort = hostParts[0];
                String database = hostParts[1];

                url = "jdbc:mysql://" + hostPort + "/" + database
                        + "?sslMode=REQUIRED&serverTimezone=UTC";

                System.out.println("Connecting to TiDB Cloud via DATABASE_URL...");

            } else {

                // OFFLINE (Localhost)
                String host = "localhost";
                String port = "1532";
                String database = "BBJDigital_DB";
                user = "root";
                password = "fire@1532";

                url = "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?serverTimezone=UTC";

                System.out.println("Connecting to Local Database...");
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);

            System.out.println("Database connected successfully!");
            return connection;

        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
    }
}
