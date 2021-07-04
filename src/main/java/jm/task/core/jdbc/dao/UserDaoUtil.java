package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/* класс, бслуживающий имплементирующие интерфейс UserDao классы
(если бы версия jdk была 8.0, то создал бы default-методы в UserDao
или (функциональный) интерфейс с default-методами) */
public class UserDaoUtil {

    public UserDaoUtil() {

    }

    public static User getUserIfExists(UserDao userDaoImplementor, List<User> users, String DataBaseName,
                                String DataBaseTableName, long id, boolean isUpdateAutoIncrementMode) {
        users = UserDaoUtil.checkAndGetTableOfUsers(userDaoImplementor,
                DataBaseName, DataBaseTableName, users, isUpdateAutoIncrementMode);
        if (!UserDaoUtil.isIdCorrectAndExistOrRemove(userDaoImplementor,
                DataBaseName, DataBaseTableName, users, id, isUpdateAutoIncrementMode)) {
            return null;
        }
        User removedUser = null;
        for (User user : users) {
            if (id == user.getId()) {
                removedUser = user;
            }
        }
        if (removedUser == null) {
            System.out.printf("WARNING: REMOVE USER BY ID: User with ID: %d isn't exist\n", id);
            return null;
        }
        return removedUser;
    }

    public static boolean isIdCorrectAndExistOrRemove(UserDao userDaoImplementor, String DBName, String DBTableName,
                                                      List<User> users, long id, boolean isAutoUpdate) {
        if (isAutoUpdate) {
            if ((int) id > users.size() || id < 0) {
                System.out.printf("WARNING: Incorrect value of id: %d. " +
                        "User with specified id isn't exist in TABLE: %s in DB: %s\n", id, DBTableName, DBName);
                return false;
            } else if (!isIdExistInTheTable(users, id)) {
                System.out.printf("WARNING: User with id: %d was already removed " +
                        "in TABLE: %s in DB: %S\n", id, DBTableName, DBName);
                return false;
            }
        }
        return true;
    }

    public static boolean isIdExistInTheTable(List<User> users, long id) {
        for (User user: users) {
            if (id == user.getId()) {
                return true;
            }
        }
        return false;
    }

    public static List<User> checkAndGetTableOfUsers(UserDao userDao, String DBName, String DBTableName,
                                                     List<User> users, boolean updateAutoIncrementMode) {
        if (users == null) {
            users = userDao.createUsersTable();
            System.out.printf("WARNING: Table of users was successfully created. " +
                            "Please use method createUsersTable() first for creating table of users " +
                            "in User instance from TABLE: %s in DB: %s\n",
                    DBTableName, DBName);
        }
        return users;
    }


    public static void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            UserDaoUtil.printSQLException(e);
        }
    }

    public static void handleExceptionAndRollback(Session session, Connection conn, Exception ex) {
        if (ex instanceof SQLException) {
            printSQLException((SQLException) ex);
            try {
                conn.rollback();
            } catch (SQLException e) {
                printSQLException(e);
            }
        } else if (ex instanceof HibernateException) {
            printHibernateException((HibernateException) ex);
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            ex.printStackTrace();

        } else {
            ex.printStackTrace();
        }
    }

    public static void printHibernateException(HibernateException ex) {
        ex.printStackTrace(System.err);
        System.err.println("Message: " + ex.getMessage() + "\n");
        Throwable t = ex.getCause();
        while (t != null) {
            System.out.println("Cause: " + t);
            t = t.getCause();
        }
    }

    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (!ignoreSQLException(
                        ((SQLException) e).
                                getSQLState())) {
                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " +
                            ((SQLException) e).getSQLState());
                    System.err.println("Error Code: " +
                            ((SQLException) e).getErrorCode());
                    System.err.println("Message: " + e.getMessage() + "\n");
                    Throwable t = ex.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    public static boolean ignoreSQLException(String sqlState) {
        if (sqlState == null) {
            System.out.println("The SQL state is not defined!");
            return false;
        }
        // X0Y32: Jar file already exists in schema
        if (sqlState.equalsIgnoreCase("X0Y32"))
            return true;
        // 42Y55: Table already exists in schema
        if (sqlState.equalsIgnoreCase("42Y55"))
            return true;
        return false;
    }
}
