package com.buseasy.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Provides a JDBC connection to the MySQL database.
 * Reads credentials from db.properties — never hardcoded.
 */
public class DBConnection {

    private static String url;
    private static String user;
    private static String password;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException("db.properties not found on classpath");
            }
            props.load(input);

            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port");
            String name = props.getProperty("db.name");
            user     = props.getProperty("db.user");
            password = props.getProperty("db.password");

            url = "jdbc:mysql://" + host + ":" + port + "/" + name
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
    }

    /**
     * Opens and returns a new JDBC connection.
     * The caller is responsible for closing the connection.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
