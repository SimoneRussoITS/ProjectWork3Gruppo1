package org.acme.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.persistence.model.Application;
import org.acme.persistence.model.Role;
import org.acme.persistence.model.State;
import org.acme.persistence.repository.ApplicationRepository;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.AuthenticationService;
import org.acme.service.exception.WrongCredentialException;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@Path("/applications")
public class ApplicationResource {
    private final ApplicationRepository applicationRepository;
    private final AuthenticationService authenticationService;

    public ApplicationResource(ApplicationRepository applicationRepository, AuthenticationService authenticationService) {
        this.applicationRepository = applicationRepository;
        this.authenticationService = authenticationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Application> getMyApplications(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return applicationRepository.getAllApplications();
        } else if (user.getRole() == Role.STUDENT) {
            return applicationRepository.getApplicationsByUserId(user.getId());
        } else {
            throw new WrongCredentialException();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createApplication(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId,
                                      @FormParam("courseName") String courseName) throws SQLException, WrongCredentialException {
        Logger log = Logger.getLogger(String.valueOf(AuthenticationResource.class));
        log.info("Received courseName: " + courseName);

        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.STUDENT) {
            applicationRepository.createApplication(user.getId(), courseName);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }


    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Application> getApplicationsByUserId(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("userId") int userId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return applicationRepository.getApplicationsByUserId(userId);
        } else {
            throw new WrongCredentialException();
        }
    }

    @PUT
    @Path("/{userId}/{applicationId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateApplication(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("userId") int userId, @PathParam("applicationId") int applicationId, @FormParam("state") State stateUpdated) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            applicationRepository.updateApplication(userId, applicationId, stateUpdated);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }
}
