package org.acme.service;

import io.vertx.ext.auth.impl.hash.SHA512;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.Session;
import org.acme.persistence.model.User;
import org.acme.persistence.repository.SessionRepository;
import org.acme.persistence.repository.UserRepository;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;
import org.acme.service.exception.HashCalculator;
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
    private final HashCalculator hashCalculator;

    private final SessionRepository sessionRepository;

    public AuthenticationService(UserRepository userRepository, UserService userService, HashCalculator hashCalculator, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.hashCalculator = hashCalculator;
        this.sessionRepository = sessionRepository;
    }

    public int login(String email, String password) throws WrongCredentialException, SessionCreatedException {

        String hash = hashCalculator.calculateHash(password);

        Optional<User> maybeUser = userRepository.findByCredentials(email, hash);
        if (maybeUser.isPresent()) {
            LOGGER.log(Level.INFO, "User found: " + email);
            User user = maybeUser.get();
            if (email.equals("itswebdev@pw.it") && password.equals("itsincom2024")) {
                LOGGER.log(Level.INFO, "User found: " + email);
                User admin = maybeUser.get();
                try {
                    int session = sessionRepository.insertSession(admin.getId());
                    LOGGER.info("Session created with ID: " + session);
                    return session;
                } catch (SQLException e) {
                    LOGGER.severe("Failed to create session.");
                    throw new SessionCreatedException(e);
                }
            }
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
        try {

            // Calcola l'hash della password
            String password = user.getPassword();
            String hash = hashCalculator.calculateHash(password);

            // Crea un nuovo oggetto User
            User u = new User();
            u.setName(user.getName());
            u.setSurname(user.getSurname());
            u.setEmail(user.getEmail());
            u.setPasswordHash(hash);

            // Salva l'utente nel repository
            User createdUser = userRepository.createUser(u);

            // Costruisce la risposta
            CreateUserResponse cur = new CreateUserResponse();
            cur.setId(createdUser.getId());
            cur.setName(createdUser.getName());
            cur.setSurname(createdUser.getSurname());
            cur.setEmail(createdUser.getEmail());

            return cur;
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();

            // Puoi lanciare una RuntimeException o gestire l'eccezione in un altro modo
            throw new RuntimeException("Error while registering user", e);
        }
    }

    public void logout(int sessionId) {
        sessionRepository.delete(sessionId);
    }

    public CreateUserResponse getProfile(int sessionId) throws SQLException {
        Session s = sessionRepository.getSessionById(sessionId);
        int userId = s.getUserId();
        CreateUserResponse user = userService.getUserById(userId);

        // Costruisci una CreateUserResponse utilizzando i dati dell'utente
        CreateUserResponse response = new CreateUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setSurname(user.getSurname());
        response.setEmail(user.getEmail());
        response.setCourseSelected(user.getCourseSelected());
        // Aggiungi altre informazioni necessarie al profilo dell'utente

        return response;
    }

}