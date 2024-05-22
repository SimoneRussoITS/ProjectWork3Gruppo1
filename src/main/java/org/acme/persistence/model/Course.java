package org.acme.persistence.model;

public class Course {
    private int idCourse;
    private String name;
    private Category category;
    private State state;

    public int getIdCourse() {
        return idCourse;
    }

    public void setIdCourse(int idCourse) {
        this.idCourse = idCourse;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum Category {
        PROGRAMMING, NEW_TECHNOLOGIES, COMMUNICATION
    }

    public enum State {
        ACTIVE, INACTIVE, PENDING, DROPPED
    }

}
