package org.acme.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.persistence.model.User;
import org.acme.persistence.repository.UserRepository;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.UserService;
import org.acme.persistence.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;

@Path("/users")
public class UserResource {
    private final UserRepository userRepository;

    public UserResource(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("id") int id) throws SQLException {
        return userRepository.getUserById(id);
    }
}
