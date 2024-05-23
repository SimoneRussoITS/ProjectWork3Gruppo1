package org.acme.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.persistence.model.Application;
import org.acme.persistence.model.Course;
import org.acme.persistence.model.Role;
import org.acme.persistence.model.User;
import org.acme.persistence.repository.ApplicationRepository;
import org.acme.persistence.repository.CourseRepository;
import org.acme.persistence.repository.UserRepository;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.AuthenticationService;
import org.acme.service.UserService;
import org.acme.service.exception.SessionCreatedException;
import org.acme.service.exception.WrongCredentialException;

import java.sql.SQLException;
import java.util.List;

@Path("/auth")
public class AuthenticationResource {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    public AuthenticationResource(AuthenticationService authenticationService, UserService userService, CourseRepository courseRepository, UserRepository userRepository, ApplicationRepository applicationRepository) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    @POST
    @Path("/register")
    public CreateUserResponse register(CreateUserRequest user) {
        return authenticationService.register(user);
    }

    @POST
    @Path("/login")
    public Response login(@FormParam("email") String email, @FormParam("password") String password) throws WrongCredentialException, SessionCreatedException {
        int session = authenticationService.login(email, password);
        NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE").value(String.valueOf(session)).build();
        return Response.ok()
                .cookie(sessionCookie)
                .build();
    }

    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("SESSION_COOKIE") int sessionId) {
        authenticationService.logout(sessionId);
        NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE").build();
        return Response.ok()
                .cookie(sessionCookie)
                .build();
    }

    @GET
    @Path("/profile")
    public CreateUserResponse getProfile(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws WrongCredentialException, SQLException {
        if (sessionId == -1) {
            throw new WrongCredentialException();
        }
        return authenticationService.getProfile(sessionId);
    }

    @GET
    @Path("/profile/admin/users")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CreateUserResponse> getUsers(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return userRepository.getAllUsers();
        } else {
            throw new WrongCredentialException();
        }
    }

    @GET
    @Path("/profile/admin/users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CreateUserResponse getUserById(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("userId") int userId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return userService.getUserById(userId);
        } else {
            throw new WrongCredentialException();
        }
    }

    @DELETE
    @Path("/profile/admin/users/{userId}")
    public Response deleteUser(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("userId") int userId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            userRepository.deleteUser(userId);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }

    @POST
    @Path("/profile/admin/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, User user) throws SQLException, WrongCredentialException {
        CreateUserResponse userLogged = authenticationService.getProfile(sessionId);
        if (userLogged.getRole() == Role.ADMIN) {
            userRepository.createUser(user);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }

    @GET
    @Path("/profile/admin/courses")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Course> getCourses(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return courseRepository.getAllCourses();

        } else {
            throw new WrongCredentialException();
        }
    }

    @GET
    @Path("/profile/admin/courses/{courseId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Course getCourseById(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("courseId") int courseId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return courseRepository.getCourseById(courseId);
        } else {
            throw new WrongCredentialException();
        }
    }

    @DELETE
    @Path("/profile/admin/courses/{courseId}")
    public Response deleteCourse(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("courseId") int courseId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            courseRepository.deleteCourse(courseId);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }

    @POST
    @Path("/profile/admin/courses")
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
    @Path("/profile/admin/applications")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Application> getApplications(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return applicationRepository.getAllApplications();
        } else {
            throw new WrongCredentialException();
        }
    }
}

