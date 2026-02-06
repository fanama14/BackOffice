package com.backoffice.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/gestion_hotel";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres"; // À adapter selon votre configuration

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL introuvable", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
