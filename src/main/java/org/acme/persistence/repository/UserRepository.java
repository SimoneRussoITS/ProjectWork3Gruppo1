package org.acme.persistence.repository;

import org.acme.persistence.model.User;
import org.acme.rest.model.CreateUserRequest;
import org.acme.rest.model.CreateUserResponse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public User createUser(User user) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO user (name, surname, email, password) VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, user.getName());
                    statement.setString(2, user.getSurname());
                    statement.setString(3, user.getEmail());
                    statement.setString(4, user.getPasswordHash());
                    statement.executeUpdate();
                    var generatedKeys = statement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        var id = generatedKeys.getInt(1);
                        user.setId(id);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public Optional<User> findByCredentials(String name, String surname, String email, String hash) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, surname, email, password FROM user WHERE name = ? AND surname = ? AND email = ? AND password = ?")) {
                    statement.setString(1, name);
                    statement.setString(2, surname);
                    statement.setString(3, email);
                    statement.setString(4, hash);
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var user = new User();
                        user.setId(resultSet.getInt("id"));
                        user.setName(resultSet.getString("name"));
                        user.setSurname(resultSet.getString("surname"));
                        user.setEmail(resultSet.getString("email"));
                        user.setPasswordHash(resultSet.getString("password"));
                        return Optional.of(user);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public User getUserById(int userId) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, name, surname, email FROM user WHERE id = ?")) {
                    statement.setInt(1, userId);
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var user = new User();
                        user.setId(resultSet.getInt("id"));
                        user.setName(resultSet.getString("name"));
                        user.setSurname(resultSet.getString("surname"));
                        user.setEmail(resultSet.getString("email"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
