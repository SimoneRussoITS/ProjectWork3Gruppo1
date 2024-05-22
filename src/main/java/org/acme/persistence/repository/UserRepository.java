package org.acme.persistence.repository;

import org.acme.persistence.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;

import javax.sql.DataSource;
import java.sql.*;
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
            String sql = "INSERT INTO user (name, surname, email, password, role, course_selected) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                // Log the prepared statement and user details
                System.out.println("Preparing statement: " + sql);
                System.out.println("User details: " + user);

                statement.setString(1, user.getName());
                statement.setString(2, user.getSurname());
                statement.setString(3, user.getEmail());
                statement.setString(4, user.getPasswordHash());

                // Assuming courseId is the ID of the selected course
                if (user.getCourseSelected() != null) {
                    statement.setInt(5, user.getCourseSelected().getIdCourse());
                } else {
                    statement.setNull(5, Types.INTEGER);
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
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, surname, email, password, role, course_selected FROM user WHERE email = ? AND password = ?")) {
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
                            user.setCourseSelected((Course) resultSet.getObject("course_selected"));
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
        String sql = "SELECT u.id, u.name, u.surname, u.email, u.role, u.course_selected, c.id as course_id, c.name as course_name, c.category, c.state " +
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

                    int courseId = rs.getInt("course_id");
                    if (courseId != 0) {
                        Course course = new Course();
                        course.setIdCourse(courseId);
                        course.setName(rs.getString("course_name"));
                        course.setCategory(Category.valueOf(rs.getString("category")));
                        course.setState(State.valueOf(rs.getString("state")));
                        user.setCourseSelected(course);
                    } else {
                        user.setCourseSelected(null); // Gestione del caso in cui non c'è un corso
                    }

                    System.out.println("User found: " + user.getName());
                    if (user.getCourseSelected() != null) {
                        System.out.println("Course found: " + user.getCourseSelected().getName());
                    } else {
                        System.out.println("No course selected for user.");
                    }
                } else {
                    System.out.println("User not found with id: " + userId);
                }
            }
        }
        return user;
    }




    public List<User> getAllUsers() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, surname, email, role, course_selected FROM user")) {
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var user = new User();
                        user.setId(resultSet.getInt("id"));
                        user.setName(resultSet.getString("name"));
                        user.setSurname(resultSet.getString("surname"));
                        user.setEmail(resultSet.getString("email"));
                        user.setRole(resultSet.getObject("role", Role.class));
                        user.setCourseSelected(resultSet.getObject("courses_selected", Course.class));
                        return List.of(user);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return List.of();
    }
}
