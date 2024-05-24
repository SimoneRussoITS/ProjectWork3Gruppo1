package org.acme.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.persistence.model.Candidate;
import org.acme.persistence.model.Role;
import org.acme.persistence.repository.CandidateRepository;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.AuthenticationService;
import org.acme.service.exception.WrongCredentialException;

import java.sql.SQLException;
import java.util.List;

@Path("/candidates")
public class CandidateResource {

    private final CandidateRepository candidateRepository;
    private final AuthenticationService authenticationService;

    public CandidateResource(CandidateRepository candidateRepository, AuthenticationService authenticationService) {
        this.candidateRepository = candidateRepository;
        this.authenticationService = authenticationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Candidate> getTestCandidates() {
        return candidateRepository.getAllTestCandidates();
    }

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Candidate> getCandidateByUserId(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("userId") int userId) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            return candidateRepository.getCandidateByUserId(userId);
        } else {
            throw new WrongCredentialException();
        }
    }

    @PUT
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCandidate(@CookieParam("SESSION_COOKIE") @DefaultValue("-1") int sessionId, @PathParam("userId") int userId, @FormParam("testState") String testState) throws SQLException, WrongCredentialException {
        CreateUserResponse user = authenticationService.getProfile(sessionId);
        if (user.getRole() == Role.ADMIN) {
            candidateRepository.updateCandidate(userId, testState);
            return Response.ok().build();
        } else {
            throw new WrongCredentialException();
        }
    }
}
