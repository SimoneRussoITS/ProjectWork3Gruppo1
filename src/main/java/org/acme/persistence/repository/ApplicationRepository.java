package org.acme.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.Application;
import org.acme.persistence.model.State;
import org.acme.persistence.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
                try (PreparedStatement statement = connection.prepareStatement("SELECT a.id, a.user_id, a.created_at, a.state, a.course_name, u.name, u.surname, u.email FROM application AS a JOIN user AS u ON a.user_id = u.ID")) {
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var application = new Application();
                        application.setId(resultSet.getInt("id"));
                        application.setIdUser(resultSet.getInt("user_id"));
                        application.setCreatedAt(resultSet.getTimestamp("created_at"));
                        application.setState(State.valueOf(resultSet.getString("state")));
                        application.setCourseName(resultSet.getString("course_name"));
                        var user = new User();
                        user.setId(resultSet.getInt("user_id"));
                        user.setName(resultSet.getString("name"));
                        user.setSurname(resultSet.getString("surname"));
                        user.setEmail(resultSet.getString("email"));
                        application.setUser(user);
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
                try (PreparedStatement statement = connection.prepareStatement("SELECT a.id, a.user_id, a.created_at, a.state, a.course_name, u.name, u.surname, u.email FROM application AS a JOIN user AS u ON a.user_id = u.ID WHERE user_id = ?")) {
                    statement.setInt(1, userId);
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var application = new Application();
                        application.setId(resultSet.getInt("id"));
                        application.setIdUser(resultSet.getInt("user_id"));
                        application.setCreatedAt(resultSet.getTimestamp("created_at"));
                        application.setState(State.valueOf(resultSet.getString("state")));
                        application.setCourseName(resultSet.getString("course_name"));
                        var user = new User();
                        user.setId(resultSet.getInt("user_id"));
                        user.setName(resultSet.getString("name"));
                        user.setSurname(resultSet.getString("surname"));
                        user.setEmail(resultSet.getString("email"));
                        application.setUser(user);
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
            try (PreparedStatement s = connection.prepareStatement("SELECT state FROM candidate WHERE user_id = ?")) {
                s.setInt(1, userId);
                var resultSet = s.executeQuery();
                if ("PASSED".equals(resultSet.getString("state"))) {
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
                        // Update the state in the user table
                        try (PreparedStatement statement = connection.prepareStatement("UPDATE user SET state = ? WHERE id = ?")) {
                            statement.setString(1, String.valueOf(stateUpdated));
                            statement.setInt(2, userId);
                            statement.executeUpdate();
                        }
                    }
                } else if ("FAILED".equals(resultSet.getString("state"))) {
                    throw new RuntimeException("The candidate has not passed the test");
                } else {
                    throw new RuntimeException("The candidate has not taken the test yet");
                }
            }
        } catch (
                SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void createApplicationAndCandidate(int id, String courseName) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO application (user_id, state, course_name) VALUES (?, ?, ?)")) {
                String state = "PENDING";
                statement.setInt(1, id);
                statement.setString(2, state);
                statement.setString(3, courseName);
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO candidate (user_id, state) VALUES (?, ?)")) {
                String state = "PENDING";
                statement.setInt(1, id);
                statement.setString(2, state);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
