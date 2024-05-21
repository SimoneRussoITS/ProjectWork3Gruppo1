package org.acme.service;

import io.vertx.ext.auth.impl.hash.SHA512;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.User;
import org.acme.persistence.repository.UserRepository;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;

@ApplicationScoped
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    private String hashCalculation(String password) {
        SHA512 a = new SHA512();
        return a.hash(null, password);
    }

    private CreateUserResponse convertToResponse(User user) {
        CreateUserResponse response = new CreateUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setEmail(user.getEmail());
        return response;
    }

    public CreateUserResponse getUserById(int userId) {
        User partecipante = userRepository.getUserById(userId);
        CreateUserResponse pr = convertToResponse(partecipante);
        return pr;
    }
}
