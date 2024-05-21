package org.acme.service;

import io.vertx.ext.auth.impl.hash.SHA512;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.Session;
import org.acme.persistence.model.User;
import org.acme.persistence.repository.SessionRepository;
import org.acme.persistence.repository.UserRepository;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.exception.SessionCreatedException;
import org.acme.service.exception.WrongCredentialException;

import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class AuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());
    private final UserRepository userRepository;
    private final UserService userService;
    private final SessionRepository sessionRepository;

    public AuthenticationService(UserRepository userRepository, UserService userService, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.sessionRepository = sessionRepository;
    }

    public int login(String name, String surname, String email, String password) throws WrongCredentialException, SessionCreatedException {
        String hash = hashCalculation(password);

        Optional<User> maybeUser = userRepository.findByCredentials(name, surname, email, hash);
        if (maybeUser.isPresent()) {
            LOGGER.log(Level.INFO, "User found: " + email);
            User user = maybeUser.get();
            try {
                int session = sessionRepository.insertSession(user.getId());
                LOGGER.info("Session created with ID: " + session);
                return session;
            } catch (SQLException e) {
                LOGGER.severe("Failed to create session.");
                throw new SessionCreatedException(e);
            }

        } else {
            LOGGER.log(Level.INFO, "User not found: " + email);
            throw new WrongCredentialException();
        }
    }

    public CreateUserResponse register(CreateUserRequest user) {
        String password = user.getPassword();
        String hash = hashCalculation(password);
        User u = new User();
        u.setName(user.getName());
        u.setSurname(user.getSurname());
        u.setEmail(user.getEmail());
        u.setPasswordHash(hash);
        User createdUser = userRepository.createUser(u);
        CreateUserResponse cur = new CreateUserResponse();
        cur.setId(createdUser.getId());
        cur.setName(createdUser.getName());
        cur.setSurname(createdUser.getSurname());
        cur.setEmail(createdUser.getEmail());
        return cur;
    }


    private String hashCalculation(String password) {
        SHA512 a = new SHA512();
        return a.hash(null, password);
    }


    public void logout(int sessionId) {
        sessionRepository.delete(sessionId);
    }

    public CreateUserResponse getProfile(int sessionId) {
        Session s = sessionRepository.getSessionById(sessionId);
        int userId = s.getUserId();
        return userService.getUserById(userId);
    }
}