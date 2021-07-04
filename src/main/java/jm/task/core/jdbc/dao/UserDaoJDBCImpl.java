package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDaoJDBCImpl implements UserDao {
    private List<User> users = null;
    // fields of class with default values if not using second constructor of this class
    private String SchemaName = "mydbtest";
    private String TableName = "users_core_JDBC";
    private String field1 = "id";
    private String field2 = "name";
    private String field3 = "last_name";
    private String field4 = "age";
    private final int numberOfClassField = 4;
    private int numberOfPassedFields;
    private boolean isFieldsPassed = false;
    boolean updateAutoIncrementMode = false;
    boolean isDropSchemaAfterTableDrop = false;
    boolean isCreateSchema = false;

    public UserDaoJDBCImpl() {

    }

    public UserDaoJDBCImpl(String SchemaName, String TableName, boolean updateAutoIncrementMode,
                           boolean isDropSchemaAfterTableDrop, boolean isCreateSchema, String... passedTableFields) {
        this.isFieldsPassed = true;
        this.SchemaName = SchemaName;
        this.TableName = TableName;
        this.updateAutoIncrementMode = updateAutoIncrementMode;
        this.isDropSchemaAfterTableDrop = isDropSchemaAfterTableDrop;
        this.isCreateSchema = isCreateSchema;
        this.numberOfPassedFields = passedTableFields.length;
        this.field1 = passedTableFields[0];
        this.field2 = passedTableFields[1];
        this.field3 = passedTableFields[2];
        this.field4 = passedTableFields[3];
    }

    @Override
    public List<User> createUsersTable() {
        if (isFieldsPassed && numberOfClassField != numberOfPassedFields) {
            System.err.printf("Incorrect number of passed fields: %d. Number of fields for TABLE: %s in DB: %s is: %d." +
                            "\nPlease change the number of fields of the class and " +
                            "change the sql query in class methods OR\n" +
                            "pass the correct number of fields for the given table.",
                    numberOfPassedFields, SchemaName, TableName, numberOfClassField);
            System.exit(-1);
        }
        if  (users == null) {
            users = new ArrayList<>();
        }
        Connection con = Util.getConnection();
        try (PreparedStatement psCreateSchema = con.prepareStatement("CREATE SCHEMA IF NOT EXISTS " +
                SchemaName + ";");
             PreparedStatement psCreateTable = con.prepareStatement("CREATE TABLE IF NOT EXISTS " +
                SchemaName + "." + TableName + " (" +
                    field1 + " BIGINT(11) NOT NULL AUTO_INCREMENT," +
                    field2 + " VARCHAR(45) NOT NULL," +
                    field3 + " VARCHAR(45) NOT NULL," +
                    field4 + " INT(3) NOT NULL," +
                    "PRIMARY KEY (" + field1 + "));")) {
            con.setAutoCommit(false);
            if (isCreateSchema) {
                psCreateSchema.executeUpdate();
            }
            psCreateTable.executeUpdate();
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            UserDaoUtil.handleExceptionAndRollback(null, con, e);
        } finally {
            UserDaoUtil.closeConnection(con);
        }
        users = getAllUsers();
        return users;
    }

    @Override
    public void dropUsersTable() {
        Connection con = Util.getConnection();
        try (PreparedStatement psDropTable = con.prepareStatement(
                "DROP TABLE IF EXISTS " + SchemaName + "." + TableName + ";");
            PreparedStatement psDropSchema = con.prepareStatement("DROP SCHEMA IF EXISTS " + SchemaName + ";")) {
            con.setAutoCommit(false);
            psDropTable.executeUpdate();
            if (isDropSchemaAfterTableDrop) {
                psDropSchema.executeUpdate();
                System.out.printf("SCHEMA: %s was successfully removed. " +
                        "Please use createUsersTable() method for create new Schema.", SchemaName);
            }
            con.commit();
            con.setAutoCommit(true);
            users = null;
        } catch (SQLException e) {
            UserDaoUtil.handleExceptionAndRollback(null, con, e);
        } finally {
            UserDaoUtil.closeConnection(con);
        }
    }

    @Override
    public void saveUser(String name, String lastName, byte age) {
        users = UserDaoUtil.checkAndGetTableOfUsers(this, SchemaName, TableName, users, false);
        User user = null;
        Connection con = Util.getConnection();
        try (PreparedStatement psInsert = con.prepareStatement("INSERT INTO " + SchemaName + "." + TableName +
                "(" + field2 + "," + field3 + "," + field4 + ")" + " VALUES (?, ?, ?);");
             PreparedStatement psLastID = con.prepareStatement(
                     "SELECT MAX(id) FROM " + SchemaName + "." + TableName + ";")) {
            con.setAutoCommit(false);
            psInsert.setString(1, name);
            psInsert.setString(2, lastName);
            psInsert.setByte(3, age);
            psInsert.executeUpdate();
            user = new User(name, lastName, age);
            ResultSet rs = psLastID.executeQuery();
            if (rs.next()) {
                user.setId(Long.parseLong(rs.getString("MAX(id)")));
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            UserDaoUtil.handleExceptionAndRollback(null, con, e);
        } finally {
            UserDaoUtil.closeConnection(con);
        }
        users.add(user);
        System.out.printf("User with NAME: %s was successfully added to DB: %s in TABLE: %s\n",
                name, SchemaName, TableName);
    }

    @Override
    public void removeUserById(long id) {
        final int startingPointOnAutoUpdate = 0;
        User removedUser = UserDaoUtil.getUserIfExists(this,
                users, SchemaName, TableName, id, updateAutoIncrementMode);
        if (removedUser == null) {
            return;
        }
        Connection con = Util.getConnection();
        try (PreparedStatement psDelete = con.prepareStatement(
                "DELETE FROM " + SchemaName + "." + TableName + " " + "WHERE id = ?;");
             PreparedStatement psBatchID1 = con.prepareStatement("SET @num := ?;");
             PreparedStatement psBatchID2 = con.prepareStatement(
                     "UPDATE " + SchemaName + "." + TableName + " " + "SET id = @num := (@num + 1);");
             PreparedStatement psBatchID3 = con.prepareStatement(
                     "ALTER TABLE " + SchemaName + "." + TableName + " AUTO_INCREMENT = 1;")) {
            con.setAutoCommit(false);
            psDelete.setLong(1, id);
            psDelete.executeUpdate();
            if (updateAutoIncrementMode) {
                  psBatchID1.setInt(1, startingPointOnAutoUpdate);
                  psBatchID1.executeUpdate();
                  psBatchID2.executeUpdate();
                  psBatchID3.executeUpdate();
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            UserDaoUtil.handleExceptionAndRollback(null, con, e);
        } finally {
            UserDaoUtil.closeConnection(con);
        }
        users.remove(removedUser);
        System.out.printf("User [%s %s age: %d, id: %d] " +
                        "was successfully removed from TABLE: %s in DB: %s\n",
                removedUser.getName(), removedUser.getLastName(), removedUser.getAge(), removedUser.getId(),
                TableName, SchemaName);
    }

    /* return users in DB */
    @Override
    public List<User> getAllUsers() {
        users = UserDaoUtil.checkAndGetTableOfUsers(this,
                SchemaName, TableName, users, updateAutoIncrementMode);
        Connection con = Util.getConnection();
        if (users.size() == 0) {
            try (PreparedStatement psGetUser = con.prepareStatement(
                         "SELECT * FROM " + SchemaName + "." + TableName + ";")) {
                con.setAutoCommit(false);
                ResultSet rs = psGetUser.executeQuery();
                while (rs.next()) {
                    User user = new User(
                            rs.getString(field2),
                            rs.getString(field3),
                            rs.getByte(field4));
                    user.setId(Long.parseLong(rs.getString(field1)));
                    users.add(user);
                }
                con.commit();
                con.setAutoCommit(true);
            } catch (SQLException e) {
                UserDaoUtil.handleExceptionAndRollback(null, con, e);
            } finally {
                UserDaoUtil.closeConnection(con);
            }
        }
        return users;
    }

    @Override
    public void cleanUsersTable() {
        users = UserDaoUtil.checkAndGetTableOfUsers(this, SchemaName, TableName, users, updateAutoIncrementMode);
        Connection con = Util.getConnection();
        try (PreparedStatement psDeleteAllRows = con.prepareStatement("SET SQL_SAFE_UPDATES = 0")) {
            con.setAutoCommit(false);
            psDeleteAllRows.addBatch("DELETE FROM " + SchemaName + "." + TableName + ";");
            psDeleteAllRows.addBatch("SET SQL_SAFE_UPDATES = 1;");
            psDeleteAllRows.executeBatch();
            psDeleteAllRows.clearBatch();
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            UserDaoUtil.handleExceptionAndRollback(null, con, e);
        } finally {
            UserDaoUtil.closeConnection(con);
        }
        users.subList(0, users.size()).clear();
    }
}
