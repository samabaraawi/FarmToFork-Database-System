package com.farmtofork.phase3prototype;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/farmtofork";
    private static final String USER = "root";
    private static final String password = System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, password);
    }
}
