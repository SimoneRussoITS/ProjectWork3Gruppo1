package org.acme.rest.model;

public class ApplicationRequest {
    private String courseName;

    public String getCourseName(String courseName) {
        return this.courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

}
