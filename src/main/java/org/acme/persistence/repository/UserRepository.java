package org.acme.persistence.repository;

import org.acme.persistence.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.rest.model.CreateUserResponse;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository {

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User createUser(User user) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO user (name, surname, email, password, role, state, course_selected) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, user.getName());
                statement.setString(2, user.getSurname());
                statement.setString(3, user.getEmail());
                statement.setString(4, user.getPasswordHash());
                statement.setString(5, String.valueOf(user.getRole()));
                statement.setString(6, String.valueOf(user.getState()));

                if (user.getCourseId() != 0) {
                    statement.setInt(7, user.getCourseId());
                } else {
                    statement.setNull(7, java.sql.Types.INTEGER);
                }

                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        user.setId(id);
                        // Log the generated ID
                        System.out.println("Generated ID: " + id);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating user in the database", e);
        }
        return user;
    }


    public Optional<User> findByCredentials(String email, String hash) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, surname, email, password, role, state, course_selected FROM user WHERE email = ? AND password = ?")) {
                    statement.setString(1, email); // Imposta il primo parametro con l'email
                    statement.setString(2, hash); // Imposta il secondo parametro con l'hash della password

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            User user = new User();
                            user.setId(resultSet.getInt("id"));
                            user.setName(resultSet.getString("name"));
                            user.setSurname(resultSet.getString("surname"));
                            user.setEmail(resultSet.getString("email"));
                            user.setPasswordHash(resultSet.getString("password"));
                            user.setRole(Role.valueOf(resultSet.getString("role")));
                            user.setState(State.valueOf(resultSet.getString("state")));
                            user.setCourseId(resultSet.getInt("course_selected"));
                            return Optional.of(user);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero dell'utente per le credenziali", e);
        }
        return Optional.empty();
    }

    public User getUserById(int userId) throws SQLException {
        User user = null;
        String sql = "SELECT u.id, u.name, u.surname, u.email, u.role, u.state, u.course_selected, c.id as course_id, c.name as course_name, c.category " +
                "FROM user u " +
                "LEFT JOIN course c ON u.course_selected = c.id " +
                "WHERE u.id = ?";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setName(rs.getString("name"));
                    user.setSurname(rs.getString("surname"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(Role.valueOf(rs.getString("role")));
                    user.setState(State.valueOf(rs.getString("state")));
                    user.setCourseId(rs.getInt("course_selected"));

                    int courseId = rs.getInt("course_selected");

                    if (courseId != 0) {
                        Course course = new Course();
                        course.setIdCourse(courseId);
                        course.setName(rs.getString("course_name"));
                        course.setCategory(Category.valueOf(rs.getString("category")));
                        user.setCourseSelected(course);
                    }


                } else {
                    System.out.println("User not found with id: " + userId);
                }
            }
        }
        return user;
    }

    public List<CreateUserResponse> getAllUsers() {
        List<CreateUserResponse> users = new ArrayList<>();
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT u.id, u.name, u.surname, u.email, u.role, u.state, u.course_selected, c.id as course_id, c.name as course_name, c.category " +
                        "FROM user u " +
                        "LEFT JOIN course c ON u.course_selected = c.id")) {

                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var user = new CreateUserResponse();
                        user.setId(resultSet.getInt("id"));
                        user.setName(resultSet.getString("name"));
                        user.setSurname(resultSet.getString("surname"));
                        user.setEmail(resultSet.getString("email"));
                        user.setRole(Role.valueOf(resultSet.getString("role")));
                        user.setState(State.valueOf(resultSet.getString("state")));
                        user.setCourseId(resultSet.getInt("course_selected"));

                        int courseId = resultSet.getInt("course_selected");
                        if (courseId != 0) {
                            Course course = new Course();
                            course.setIdCourse(courseId);
                            course.setName(resultSet.getString("course_name"));
                            course.setCategory(Category.valueOf(resultSet.getString("category")));
                            user.setCourseSelected(course);
                        }

                        users.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }
}
