package com.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String HOST = "89.223.65.229";
    private static final String PORT = "5432";
    private static final String DATABASE = "msod_database";
    private static final String SCHEMA = "ctf_contest";
    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE + "?currentSchema=" + SCHEMA;
    private static final String USER = "revkov_db_grandmaster";
    private static final String PASSWORD = "VOf67bY6kR";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}