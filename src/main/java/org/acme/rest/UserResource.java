package org.acme.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.persistence.model.User;
import org.acme.persistence.repository.UserRepository;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.UserService;
import org.acme.persistence.repository.UserRepository;

@Path("/users")
public class UserResource {
    private final UserRepository userRepository;
    private final UserService userService;

    public UserResource(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(int id) {
        return userRepository.getUserById(id);
    }
}
