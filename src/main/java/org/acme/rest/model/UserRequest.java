package org.acme.rest.model;

import org.acme.persistence.model.Role;
import org.acme.persistence.model.State;

public class UserRequest {
    private State state;
    private int courseId;
    private Role role;

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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
