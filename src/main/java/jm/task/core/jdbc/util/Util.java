package jm.task.core.jdbc.util;

import jm.task.core.jdbc.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

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
    private static String DBName = "mydbtest";
    private static final SessionFactory sessionFactory = null;

    public static Connection getConnection() {
        Connection connection = null;
        try {
            Properties connectionProps = new Properties();
            connectionProps.put("user", userName);
            connectionProps.put("password", password);
            connection = DriverManager.getConnection(url, connectionProps);
        } catch (SQLException e) {
            System.out.printf("Could not get the connection to the DB: %s", DBName);
            e.printStackTrace();
        }
        return connection;
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            Configuration configuration = new Configuration();
            Properties properties = new Properties();
            properties.put(Environment.HBM2DDL_AUTO, "update");
            properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQL5Dialect");
            properties.put(Environment.DRIVER, DB_DRIVER);
            properties.put(Environment.USER, userName);
            properties.put(Environment.PASS, password);
            properties.put(Environment.URL, "jdbc:mysql://localhost:3306?useSSL=false&serverTimezone=UTC");
            properties.put(Environment.SHOW_SQL, true);
            properties.put(Environment.FORMAT_SQL, true);
            properties.put(Environment.USE_SQL_COMMENTS, true);
            properties.put(Environment.DEFAULT_SCHEMA, DBName);
            properties.put(Environment.LOG_SESSION_METRICS, true);
            properties.put(Environment.TRANSACTION_STRATEGY, "org.hibernate.transaction.JDBCTransactionFactory");
            properties.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
            configuration.setProperties(properties);
            configuration.addAnnotatedClass(User.class);
            final ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();
            try {
                return configuration.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
            }
        }
        return sessionFactory;
    }
}





