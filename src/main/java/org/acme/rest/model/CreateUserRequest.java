package org.acme.rest.model;

import org.acme.persistence.model.Course;
import org.acme.persistence.model.Role;
import org.acme.persistence.model.State;

public class CreateUserRequest {
    private String name;
    private String surname;
    private String email;
    private String password;
    private Role role;
    private State state;
    private int courseId;
    private Course courseSelected;

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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public Course getCourseSelected() {
        return courseSelected;
    }

    public void setCourseSelected(Course courseSelected) {
        this.courseSelected = courseSelected;
    }
}
