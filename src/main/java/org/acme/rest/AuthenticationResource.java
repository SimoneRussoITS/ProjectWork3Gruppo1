package org.acme.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.AuthenticationService;
import org.acme.service.exception.SessionCreatedException;
import org.acme.service.exception.WrongCredentialException;

@Path("/auth")
public class AuthenticationResource {

    private final AuthenticationService authenticationService;

    public AuthenticationResource(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public CreateUserResponse register(CreateUserRequest user) {
        return authenticationService.register(user);
    }

    @POST
    @Path("/login")
    public Response login(@FormParam("name") String name, @FormParam("surname") String surname, @FormParam("email") String email, @FormParam("password") String password) throws WrongCredentialException, SessionCreatedException {
        int session = authenticationService.login(name, surname, email, password);
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
    public CreateUserResponse getProfile(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws WrongCredentialException {
        if (sessionId == -1) {
            throw new WrongCredentialException();
        }
        return authenticationService.getProfile(sessionId);
    }
}
