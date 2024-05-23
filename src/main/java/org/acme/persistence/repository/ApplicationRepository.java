package org.acme.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.Application;
import org.acme.persistence.model.State;

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
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, user_id, created_at, state FROM application")) {
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var application = new Application();
                        application.setId(resultSet.getInt("id"));
                        application.setIdUser(resultSet.getInt("user_id"));
                        application.setCreatedAt(resultSet.getTimestamp("created_at"));
                        application.setState(State.valueOf(resultSet.getString("state")));
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
                try (PreparedStatement statement = connection.prepareStatement("SELECT id, user_id, created_at, state FROM application WHERE user_id = ?")) {
                    statement.setInt(1, userId);
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        var application = new Application();
                        application.setId(resultSet.getInt("id"));
                        application.setIdUser(resultSet.getInt("user_id"));
                        application.setCreatedAt(resultSet.getTimestamp("created_at"));
                        application.setState(State.valueOf(resultSet.getString("state")));
                        applications.add(application);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return applications;
    }

    public void updateApplication(int userId, Application application) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("UPDATE application SET state = ? WHERE user_id = ?")) {
                    statement.setString(1, application.getState().name());
                    statement.setInt(2, userId);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
