package org.acme.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.Application;
import org.acme.persistence.model.State;
import org.acme.persistence.model.User;

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
                try (PreparedStatement statement = connection.prepareStatement("SELECT a.id, a.user_id, a.created_at, a.state, a.course_name, u.name, u.surname, u.email FROM application AS a JOIN user AS u ON a.user_id = u.ID")) {
                    newApplication(applications, statement);
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
                    newApplication(applications, statement);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return applications;
    }

    private void newApplication(List<Application> applications, PreparedStatement statement) throws SQLException {
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

    public void createApplicationAndCandidate(int id, String courseName) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);  // Inizia una transazione

            try {
                // Controlla se il candidato ha già applicato per lo stesso corso
                try (PreparedStatement checkApplication = connection.prepareStatement(
                        "SELECT 1 FROM application WHERE user_id = ? AND course_name = ?")) {
                    checkApplication.setInt(1, id);
                    checkApplication.setString(2, courseName);
                    try (ResultSet resultSet = checkApplication.executeQuery()) {
                        if (resultSet.next()) {
                            throw new RuntimeException("The candidate has already applied for this course");
                        }
                    }
                }

                // Inserisce una nuova applicazione
                try (PreparedStatement insertApplication = connection.prepareStatement(
                        "INSERT INTO application (user_id, state, course_name) VALUES (?, ?, ?)")) {
                    String state = "PENDING";
                    insertApplication.setInt(1, id);
                    insertApplication.setString(2, state);
                    insertApplication.setString(3, courseName);
                    insertApplication.executeUpdate();
                }

                // Controlla se il candidato esiste nella tabella candidate
                boolean candidateExists;
                try (PreparedStatement selectCandidate = connection.prepareStatement(
                        "SELECT 1 FROM candidate WHERE user_id = ?")) {
                    selectCandidate.setInt(1, id);
                    try (ResultSet resultSet = selectCandidate.executeQuery()) {
                        candidateExists = resultSet.next();
                    }
                }

                // Se il candidato non esiste, inserisce un nuovo record nella tabella candidate
                if (!candidateExists) {
                    try (PreparedStatement insertCandidate = connection.prepareStatement(
                            "INSERT INTO candidate (user_id, state) VALUES (?, ?)")) {
                        String state = "PENDING";
                        insertCandidate.setInt(1, id);
                        insertCandidate.setString(2, state);
                        insertCandidate.executeUpdate();
                    }
                }

                connection.commit();  // Conferma la transazione
            } catch (SQLException e) {
                connection.rollback();  // Annulla la transazione in caso di errore
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateApplication(int userId, int applicationId, State stateUpdated) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement s = connection.prepareStatement("SELECT state FROM candidate WHERE user_id = ?")) {
                s.setInt(1, userId);
                var resultSet = s.executeQuery();
                if (resultSet.next()) {  // Sposta il cursore alla prima riga valida
                    String state = resultSet.getString("state");
                    if ("PASSED".equals(state)) {
                        // Aggiorna user.course_selected in base alle tabelle application e course
                        try (PreparedStatement statement = connection.prepareStatement(
                                "UPDATE user JOIN application ON user.id = application.user_id " +
                                        "JOIN course ON application.course_name = course.name " +
                                        "SET user.course_selected = course.id " +
                                        "WHERE user.id = ? AND application.id = ?")) {
                            statement.setInt(1, userId);
                            statement.setInt(2, applicationId);
                            statement.executeUpdate();
                        }

                        // Aggiorna lo stato nella tabella application
                        try (PreparedStatement statement = connection.prepareStatement(
                                "UPDATE application SET state = ? WHERE user_id = ? AND id = ?")) {
                            statement.setString(1, String.valueOf(stateUpdated));
                            statement.setInt(2, userId);
                            statement.setInt(3, applicationId);
                            statement.executeUpdate();
                        }

                        // Se lo stato aggiornato è "ACTIVE", blocca tutte le altre applicazioni per lo stesso utente
                        if ("ACTIVE".equals(String.valueOf(stateUpdated))) {
                            try (PreparedStatement statement = connection.prepareStatement(
                                    "UPDATE application SET state = 'BLOCKED' WHERE user_id = ? AND id != ?")) {
                                statement.setInt(1, userId);
                                statement.setInt(2, applicationId);
                                statement.executeUpdate();
                            }

                            // Aggiorna lo stato nella tabella user
                            try (PreparedStatement statement = connection.prepareStatement(
                                    "UPDATE user SET state = ? WHERE id = ?")) {
                                statement.setString(1, String.valueOf(stateUpdated));
                                statement.setInt(2, userId);
                                statement.executeUpdate();
                            }
                        }
                    } else if ("FAILED".equals(state)) {
                        throw new RuntimeException("The candidate has not passed the test");
                    } else {
                        throw new RuntimeException("The candidate has not taken the test yet");
                    }
                } else {
                    throw new RuntimeException("No candidate found with user_id: " + userId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
