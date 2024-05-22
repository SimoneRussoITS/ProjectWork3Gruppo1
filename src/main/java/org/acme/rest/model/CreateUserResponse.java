package org.acme.rest.model;

import org.acme.persistence.model.Course;
import org.acme.persistence.model.Role;

public class CreateUserResponse {
    private int id;
    private String name;
    private String surname;
    private String email;
    private Role role;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Course getCourseSelected() {
        return courseSelected;
    }

    public void setCourseSelected(Course courseSelected) {
        this.courseSelected = courseSelected;
    }
}
