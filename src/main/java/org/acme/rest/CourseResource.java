package org.acme.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.persistence.model.Course;
import org.acme.persistence.repository.CourseRepository;

import java.util.List;

@Path("/courses")
public class CourseResource {
    private final CourseRepository courseRepository;

    public CourseResource(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Course> getAllCourses() {
        return courseRepository.getAllCourses();
    }

    @GET
    @Path("/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Course> getCoursesByCategory(@PathParam("category") String category) {
        return courseRepository.getCoursesByCategory(category.toUpperCase());
    }

    @GET
    @Path("/{courseId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Course getCourseById(@PathParam("courseId") int courseId) {
        return courseRepository.getCourseById(courseId);
    }
}
