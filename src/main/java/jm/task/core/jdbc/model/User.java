package jm.task.core.jdbc.model;

import javax.persistence.*;

@Entity
@Table(name="users_core_hibernate")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // генерация id через AutoIncrement в MySQL
    @Column
    private Long id;
    @Column
    private String name;
    @Column(name = "last_name")
    private String lastName;
    @Column
    private Byte age;

    public User() {

    }

    public User(String name, String lastName, Byte age) {
        this.name = name;
        this.lastName = lastName;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Byte getAge() {
        return age;
    }

    public void setAge(Byte age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return getName() + " " + getLastName() +
                ", Age: " + getAge() +
                ", ID: " + getId();
    }

}
