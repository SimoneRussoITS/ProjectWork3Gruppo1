package org.acme.rest;

import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.persistence.model.*;
import org.acme.persistence.repository.UserRepository;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.AuthenticationService;
import org.acme.service.UserService;
import org.acme.service.exception.WrongCredentialException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Path("/users")
public class UserResource {
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final UserService userService;

    public UserResource(UserRepository userRepository, AuthenticationService authenticationService, UserService userService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CreateUserResponse getUser(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws WrongCredentialException, SQLException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.STUDENT) {
            CreateUserResponse u = authenticationService.getProfile(sessionId);
            return u;
        } else {
            throw new WrongCredentialException();
        }
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CreateUserResponse> getUsers(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return userRepository.getAllUsers();
        } else {
            throw new WrongCredentialException();
        }
    }

    @POST
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
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CreateUserResponse getUserById(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("userId") int userId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return userService.getUserById(userId);
        } else {
            throw new WrongCredentialException();
        }
    }

    @PUT
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("userId") int userId, JsonObject userRequest) throws SQLException, WrongCredentialException {
        State state = State.valueOf(userRequest.getString("state"));
        int courseId = userRequest.getInt("courseId");
        Role role = Role.valueOf(userRequest.getString("role"));

        CreateUserResponse userLogged = authenticationService.getProfile(sessionId);
        if (userLogged.getRole() == Role.ADMIN) {
            userRepository.updateUser(userId, state, courseId, role);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }

    @DELETE
    @Path("/{userId}")
    public Response deleteUser(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("userId") int userId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            userRepository.deleteUser(userId);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }
}
