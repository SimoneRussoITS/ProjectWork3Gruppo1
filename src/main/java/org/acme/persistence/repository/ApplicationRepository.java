package org.acme.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.Application;
import org.acme.persistence.model.State;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ApplicationRepository {
    private final DataSource dataSource;

    public ApplicationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Application> getAllApplications() {
        List<Application> applications = new ArrayList<>();
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, user_id, created_at, state, course_name FROM application")) {
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var application = new Application();
                        application.setId(resultSet.getInt("id"));
                        application.setIdUser(resultSet.getInt("user_id"));
                        application.setCreatedAt(resultSet.getTimestamp("created_at"));
                        application.setState(State.valueOf(resultSet.getString("state")));
                        application.setCourseName(resultSet.getString("course_name"));
                        applications.add(application);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return applications;
    }

    public List<Application> getApplicationsByUserId(int userId) {
        List<Application> applications = new ArrayList<>();
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, user_id, created_at, state, course_name FROM application WHERE user_id = ?")) {
                    statement.setInt(1, userId);
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var application = new Application();
                        application.setId(resultSet.getInt("id"));
                        application.setIdUser(resultSet.getInt("user_id"));
                        application.setCreatedAt(resultSet.getTimestamp("created_at"));
                        application.setState(State.valueOf(resultSet.getString("state")));
                        application.setCourseName(resultSet.getString("course_name"));
                        applications.add(application);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return applications;
    }

    public void updateApplication(int userId, int applicationId, State stateUpdated) {
        try (Connection connection = dataSource.getConnection()) {
            // Start a transaction
            connection.setAutoCommit(false);
            try {
                // Update the state in the user table
                try (PreparedStatement statement = connection.prepareStatement("UPDATE user SET state = ? WHERE id = ?")) {
                    statement.setString(1, String.valueOf(stateUpdated));
                    statement.setInt(2, userId);
                    statement.executeUpdate();
                }
                // Update user.course_selected based on the application and course tables
                try (PreparedStatement statement = connection.prepareStatement("UPDATE user JOIN application ON user.id = application.user_id JOIN course ON application.course_name = course.name SET user.course_selected = course.id WHERE user.id = ? AND application.id = ?")) {
                    statement.setInt(1, userId);
                    statement.setInt(2, applicationId);
                    statement.executeUpdate();
                }
                // Update the state in the application table
                try (PreparedStatement statement = connection.prepareStatement("UPDATE application SET state = ? WHERE user_id = ? AND id = ?")) {
                    statement.setString(1, String.valueOf(stateUpdated));
                    statement.setInt(2, userId);
                    statement.setInt(3, applicationId);
                    statement.executeUpdate();
                }
                // If the updated state is "ACCEPTED", block all other applications for the same user
                if ("ACTIVE".equals(String.valueOf(stateUpdated))) {
                    try (PreparedStatement statement = connection.prepareStatement("UPDATE application SET state = 'BLOCKED' WHERE user_id = ? AND id != ?")) {
                        statement.setInt(1, userId);
                        statement.setInt(2, applicationId);
                        statement.executeUpdate();
                    }
                }
                // Commit the transaction
                connection.commit();
            } catch (SQLException e) {
                // Rollback the transaction in case of error
                connection.rollback();
                throw e;
            } finally {
                // Restore auto-commit mode
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void createApplication(int id, String courseName) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO application (user_id, state, course_name) VALUES (?, ?, ?)")) {
                String state = "PENDING";
                statement.setInt(1, id);
                statement.setString(2, state);
                statement.setString(3, courseName);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
