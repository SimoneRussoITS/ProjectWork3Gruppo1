package org.acme.rest.model;

import org.acme.persistence.model.Role;

public class CreateUserRequest {
    private String name;
    private String surname;
    private String email;
    private String password;
    private Role role;
    private int courseSelected;


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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getCourseSelected() {
        return courseSelected;
    }

    public void setCourseSelected(int courseSelected) {
        this.courseSelected = courseSelected;
    }
}
