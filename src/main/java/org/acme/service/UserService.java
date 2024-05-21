package org.acme.service;

import org.acme.persistence.model.User;
import org.acme.persistence.repository.UserRepository;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean checkPassword(User user, String password) {
        return user.getPasswordHash().equals(password);
    }
}
