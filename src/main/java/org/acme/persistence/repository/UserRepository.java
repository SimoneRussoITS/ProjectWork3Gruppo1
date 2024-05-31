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
                statement.setString(5, user.getRole().name());
                statement.setString(6, user.getState().name());
                if (user.getCourseSelected() != null) {
                    statement.setString(7, user.getCourseSelected().getName());
                } else {
                    statement.setNull(7, java.sql.Types.VARCHAR);
                }

                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating user failed, no rows affected.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        user.setId(id);
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

    public void deleteUser(int userId) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                // Elimina prima da user.candidate
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM user.candidate WHERE user_id = ?")) {
                    statement.setInt(1, userId);
                    statement.executeUpdate();
                }
                // Poi elimina da user.application
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM user.application WHERE user_id = ?")) {
                    statement.setInt(1, userId);
                    statement.executeUpdate();
                }
                // Infine, elimina da user.user
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM user.user WHERE id = ?")) {
                    statement.setInt(1, userId);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateUser(int userId, State state, int courseId, Role role) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("UPDATE user SET state = ?, course_selected = ?, role = ? WHERE id = ?")) {
                    statement.setString(1, String.valueOf(state));
                    statement.setInt(2, courseId);
                    statement.setString(3, String.valueOf(role));
                    statement.setInt(4, userId);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
