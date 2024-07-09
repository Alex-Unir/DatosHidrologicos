package org.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConectorDB {
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/ESPAGUA";
    //Indicar usuario que generases en tú BBDD local
    private static final String MYSQL_USER = "root";
    //Indicar password que generases en tú BBDD local
    private static final String MYSQL_PASSWORD = "mysql";

    public static Connection getMySQLConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
    }
}
