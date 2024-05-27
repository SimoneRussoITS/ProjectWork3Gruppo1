package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.*;
import org.acme.persistence.repository.CourseRepository;
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
    private final HashCalculator hashCalculator;
    private final CourseRepository courseRepository;

    private final SessionRepository sessionRepository;

    public AuthenticationService(UserRepository userRepository, UserService userService, HashCalculator hashCalculator, CourseRepository courseRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.hashCalculator = hashCalculator;
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
    }

    public int login(String email, String password) throws WrongCredentialException, SessionCreatedException {

        String hash = hashCalculator.calculateHash(password);

        Optional<User> maybeUser = userRepository.findByCredentials(email, hash);
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

    public CreateUserResponse register(CreateUserRequest userRequest) {
        try {
            // Calcola l'hash della password
            String password = userRequest.getPassword();
            String hash = hashCalculator.calculateHash(password);

            // Crea un nuovo oggetto User
            User user = new User();
            user.setName(userRequest.getName());
            user.setSurname(userRequest.getSurname());
            user.setEmail(userRequest.getEmail());
            user.setPasswordHash(hash);

            // Imposta il ruolo e lo stato
            user.setRole(Role.STUDENT);
            user.setState(State.INACTIVE);

            // Imposta courseSelected se presente
            if (userRequest.getCourseId() != 0) {
                // Supponiamo che tu abbia un metodo per ottenere il corso da un ID
                Course course = courseRepository.getCourseById(userRequest.getCourseId());
                user.setCourseSelected(course);
            } else {
                user.setCourseSelected(null);
            }

            // Salva l'utente nel repository
            User createdUser = userRepository.createUser(user);

            // Costruisce la risposta
            CreateUserResponse response = new CreateUserResponse();
            response.setId(createdUser.getId());
            response.setName(createdUser.getName());
            response.setSurname(createdUser.getSurname());
            response.setEmail(createdUser.getEmail());
            response.setRole(createdUser.getRole());
            response.setState(createdUser.getState());
            response.setCourseSelected(createdUser.getCourseSelected());

            return response;
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
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
        response.setRole(user.getRole());
        response.setState(user.getState());
        response.setCourseSelected(user.getCourseSelected());
        // Aggiungi altre informazioni necessarie al profilo dell'utente

        return response;
    }

}