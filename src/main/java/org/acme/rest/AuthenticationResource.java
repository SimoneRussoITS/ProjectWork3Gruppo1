package org.acme.rest;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;
import org.acme.rest.model.LoginRequest;
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
    public CreateUserResponse register(CreateUserRequest user) {
        return authenticationService.register(user);
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest) throws WrongCredentialException, SessionCreatedException {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        int session = authenticationService.login(email, password);
        NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE").path("/").value(String.valueOf(session)).build();
        return Response.ok()
                .cookie(sessionCookie)
                .build();
    }

    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("SESSION_COOKIE") int sessionId) {
        authenticationService.logout(sessionId);
        NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE").path("/").build();
        return Response.ok()
                .cookie(sessionCookie)
                .build();
    }
}

