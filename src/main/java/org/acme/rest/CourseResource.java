package org.acme.rest;

import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.persistence.model.Course;
import org.acme.persistence.model.Role;
import org.acme.persistence.repository.CourseRepository;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.AuthenticationService;
import org.acme.service.exception.WrongCredentialException;

import java.sql.SQLException;
import java.util.List;

@Path("/courses")
public class CourseResource {
    private final CourseRepository courseRepository;
    private final AuthenticationService authenticationService;

    public CourseResource(CourseRepository courseRepository, AuthenticationService authenticationService) {
        this.courseRepository = courseRepository;
        this.authenticationService = authenticationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Course> getAllCourses() {
        return courseRepository.getAllCourses();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCourse(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, Course course) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            courseRepository.createCourse(course);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
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

    @PUT
    @Path("/{courseId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateCourse(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("courseId") int courseId, JsonObject courseRequest) throws SQLException, WrongCredentialException {
        String name = courseRequest.getString("name");
        String category = courseRequest.getString("category");

        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            courseRepository.updateCourse(courseId, name, category);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }

    @DELETE
    @Path("/{courseId}")
    public Response deleteCourse(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("courseId") int courseId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            courseRepository.deleteCourse(courseId);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }
}
