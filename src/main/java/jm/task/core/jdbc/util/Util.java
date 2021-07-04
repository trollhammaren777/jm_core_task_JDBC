package jm.task.core.jdbc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Util {

    // реализуйте настройку соеденения с БД
    private static String DB_DRIVER = "com.mysql.jdbc.Driver";
    private static String DBMS = "mysql";
    private static String serverName = "localhost";
    private static String portNumber = "3306";
    private static String userName = "root";
    private static String password = "root";
    private static String url = "jdbc:" +
            DBMS + "://" + serverName + ":" + portNumber +
            "/?useSSL=false"; // not-product use

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Properties connectionProps = new Properties();
            connectionProps.put("user", userName);
            connectionProps.put("password", password);
            connection = DriverManager.getConnection(url, connectionProps);
        } catch (SQLException e) {
            System.out.println("Could not get the connection to the DB");
            e.printStackTrace();
        }
        return connection;
    }
}





