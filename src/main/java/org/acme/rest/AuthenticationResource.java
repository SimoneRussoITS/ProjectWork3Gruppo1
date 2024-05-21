package org.acme.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.acme.service.AuthenticationService;

@Path("/auth")
public class AuthenticationResource {

    AuthenticationService authenticationService = new AuthenticationService();

    public AuthenticationResource(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @POST
    @Path("/login")
    public Response login(@FormParam("name") String name, @FormParam("surname") String surname, @FormParam("email") String email, @FormParam("password") String password) {
        int session = authenticationService.login(name, surname, email, password);
        NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE").value(String.valueOf(session)).build();
        return Response.ok()
                .cookie(sessionCookie)
                .build();
    }
}
