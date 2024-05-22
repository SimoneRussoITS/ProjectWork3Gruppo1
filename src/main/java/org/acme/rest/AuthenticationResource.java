package org.acme.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.persistence.repository.UserRepository;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.AuthenticationService;
import org.acme.service.exception.SessionCreatedException;
import org.acme.service.exception.WrongCredentialException;

import java.sql.SQLException;
import java.util.List;

@Path("/auth")
public class AuthenticationResource {

    private final AuthenticationService authenticationService;
    private UserRepository userRepository;

    public AuthenticationResource(AuthenticationService authenticationService, UserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
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
    @Path("/profile/users")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CreateUserResponse> getUsers(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole().equals("ADMIN")) {
            return userRepository.getAllUsers();
        } else {
            throw new WrongCredentialException();
        }
    }

//    @GET
//    @Path("/profile/courses")
//    @Produces(MediaType.APPLICATION_JSON)
//    public List<CreateUserResponse> getCourses(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws SQLException, WrongCredentialException {
//        CreateUserResponse user = authenticationService.getProfile(sessionId);
//        if (user.getRole().equals("ADMIN")) {
//            //return userRepository.getAllCourses();
//        } else {
//            throw new WrongCredentialException();
//        }
//    }
}

