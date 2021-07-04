package jm.task.core.jdbc;

import jm.task.core.jdbc.dao.UserDaoJDBCImpl;
import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.service.UserService;
import jm.task.core.jdbc.service.UserServiceImpl;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // реализуйте алгоритм здесь
//        UserService userService = new UserServiceImpl(new UserDaoJDBCImpl());
        UserService userService = new   UserServiceImpl(new UserDaoJDBCImpl("mydb", "users_JDBC",
                false, false, false,
                "id", "name", "lastName", "age"));
        userService.createUsersTable();
        userService.saveUser("Maxim", "Maximovich", (byte) 23);
        userService.saveUser("Boris", "Borisovich", (byte) 24);
        userService.saveUser("Ravil", "Ravilovich", (byte) 25);
        userService.saveUser("Rinat", "Rinatovich", (byte) 26);
        System.out.println("\nUsers:");
        List<User> users = userService.getAllUsers();
        for (User user: users) {
            System.out.println(user.toString());
        }
        userService.cleanUsersTable();
        userService.dropUsersTable();
    }
}
