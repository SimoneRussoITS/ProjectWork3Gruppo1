package org.acme.persistence.model;

import java.util.List;
import java.util.Objects;

public class User {
    private int id;
    private String name;
    private String surname;
    private String email;
    private String passwordHash;
    private List<Course> coursesSelected;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Course> getCoursesSelected() {
        return coursesSelected;
    }

    public void setCoursesSelected(List<Course> coursesSelected) {
        this.coursesSelected = coursesSelected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(name, user.name) && Objects.equals(surname, user.surname) && Objects.equals(email, user.email) && Objects.equals(passwordHash, user.passwordHash) && Objects.equals(coursesSelected, user.coursesSelected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surname, email, passwordHash, coursesSelected);
    }
}

