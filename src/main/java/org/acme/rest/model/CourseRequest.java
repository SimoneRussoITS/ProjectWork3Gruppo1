package org.acme.rest.model;

public class CourseRequest {
    private String name;
    private String category;

    public String getName(String name) {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory(String category) {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
