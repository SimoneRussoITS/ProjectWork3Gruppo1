package org.acme.rest.model;

import org.acme.persistence.model.Course;

import java.util.List;

public class CreateUserResponse {
    private int id;
    private String name;
    private String surname;
    private String email;
    private Course courseSelected;

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

    public Course getCourseSelected() {
        return courseSelected;
    }

    public void setCourseSelected(Course courseSelected) {
        this.courseSelected = courseSelected;
    }
}
