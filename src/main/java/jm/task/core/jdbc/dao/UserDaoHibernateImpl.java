package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;

import java.util.ArrayList;
import java.util.List;

public class UserDaoHibernateImpl implements UserDao {
    private List<User> users = null;
    private String DBName = "mydbtest";
    private String DBTableName = "users_core_hibernate";
    private String EntityName1 = "User";
    boolean updateAutoIncrementMode = false;

    public UserDaoHibernateImpl() {

    }

    public UserDaoHibernateImpl(String DBName, String DBTableName, boolean updateAutoIncrementMode, String... passedEntities) {
        this.DBName = DBName;
        this.DBTableName = DBTableName;
        this.updateAutoIncrementMode = updateAutoIncrementMode;
        this.EntityName1 = passedEntities[0];
    }

    @Override
    public List<User> createUsersTable() {
        if (users == null) {
            users = new ArrayList<>();
        }
        users = getAllUsers();
        return users;
    }

    @Override
    public void dropUsersTable() {
        users = UserDaoUtil.checkAndGetTableOfUsers(this,
                getDBName(), getDBTableName(), getUsers(), getUpdateAutoIncrementMode());
        Session session = Util.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            // nativeSQL/sql-dependent solution, because hibernate doesn't support ddl operations at runtime
            // в зависимости от режима HBM2DDL_AUTO (см.Util), hibernate можеет автоматически создавать и
            // не создавать заново эту же таблицу (например, при режиме update))
            String MySQLDropTable = String.format("DROP TABLE IF EXISTS %s;", getDBName() + "." + getDBTableName());
            session.createSQLQuery(MySQLDropTable).executeUpdate();
            session.getTransaction().commit();
            users = null;
        } catch (Exception e) {
            UserDaoUtil.handleExceptionAndRollback(session, null, e);
        }
    }

    @Override
    public void saveUser(String name, String lastName, byte age) {
        users = UserDaoUtil.checkAndGetTableOfUsers(this,
                getDBName(), getDBTableName(), getUsers(), getUpdateAutoIncrementMode());
        Session session = Util.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            User user = new User(name, lastName, age);
            session.save(user);
            Criteria criteria = session.createCriteria(User.class); // using deprecated org.hibernate.Criteria
            Long id = (Long)criteria.setProjection(Projections.max("id")).uniqueResult();
            user.setId(id);
            session.getTransaction().commit();
            users.add(user);
            System.out.printf("User with NAME: %s was successfully added to DB: %s in TABLE: %s\n",
                    name, getDBName(), getDBTableName());
        } catch (Exception e) {
            UserDaoUtil.handleExceptionAndRollback(session, null, e);
        }
    }

    @Override
    public void removeUserById(long id) {
        User removedUser = UserDaoUtil.getUserIfExists(this,
                getUsers(), getDBName(), getDBTableName(), id, getUpdateAutoIncrementMode());
        if (removedUser == null) {
            return;
        }
        Session session = Util.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            session.delete(removedUser);
            session.getTransaction().commit();
        } catch (HibernateException e) {
            UserDaoUtil.handleExceptionAndRollback(session, null, e);
        }
        users.remove(removedUser);
        System.out.printf("User [%s %s age: %d, id: %d] " +
                        "was successfully removed from TABLE: %s in DB: %s\n",
                removedUser.getName(), removedUser.getLastName(), removedUser.getAge(), removedUser.getId(),
                getDBTableName(), getDBName());
    }

    @Override
    public List<User> getAllUsers() {
        users = UserDaoUtil.checkAndGetTableOfUsers(this,
                getDBName(), getDBTableName(), getUsers(), getUpdateAutoIncrementMode());
        Session session = Util.getSessionFactory().getCurrentSession();
        if (users.size() == 0) {
            try {
                session.beginTransaction();
                users = (List<User>) session.createQuery(String.format("FROM %s", getEntityName1())).list(); // hql
                session.getTransaction().commit();
            } catch (Exception e) {
                UserDaoUtil.handleExceptionAndRollback(session, null, e);
            }
        }
        return users;
    }

    @Override
    public void cleanUsersTable() {
        users = UserDaoUtil.checkAndGetTableOfUsers(this,
                getDBName(), getDBTableName(), getUsers(), getUpdateAutoIncrementMode());
        Session session = Util.getSessionFactory().getCurrentSession();
        try {
            session.beginTransaction();
            String hqlDropTable = String.format("delete from %s", "User");
            session.createQuery(hqlDropTable).executeUpdate();
            users.subList(0, users.size()).clear();
            session.getTransaction().commit();
        } catch (Exception e) {
            UserDaoUtil.handleExceptionAndRollback(session, null, e);
        }
    }

    public String getDBName() {
        return DBName;
    }

    public String getDBTableName() {
        return DBTableName;
    }

    public String getEntityName1() {
        return EntityName1;
    }

    public List<User> getUsers() {
        return users;
    }

    public boolean getUpdateAutoIncrementMode() {
        return updateAutoIncrementMode;
    }

}
