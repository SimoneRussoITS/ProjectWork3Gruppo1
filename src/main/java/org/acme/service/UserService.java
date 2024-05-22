package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.User;
import org.acme.persistence.repository.UserRepository;
import org.acme.rest.model.CreateUserResponse;

import java.sql.SQLException;

@ApplicationScoped
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    private CreateUserResponse convertToResponse(User user) {
        CreateUserResponse response = new CreateUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCourseSelected(user.getCourseSelected());
        return response;
    }

    public CreateUserResponse getUserById(int userId) throws SQLException {
        User u = userRepository.getUserById(userId);
        CreateUserResponse ur = convertToResponse(u);
        return ur;
    }
}
