package org.acme.persistence.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.persistence.model.Candidate;
import org.acme.persistence.model.TestState;
import org.acme.persistence.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CandidateRepository {

    private final DataSource dataSource;

    public CandidateRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Candidate> getAllTestCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT c.id, c.user_id, c.state, u.name, u.surname, u.email FROM candidate AS c JOIN user AS u ON c.user_id = u.ID")) {
                    newCandidate(candidates, statement);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return candidates;
    }

    public List<Candidate> getCandidateByUserId(int userId) {
        List<Candidate> candidates = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT c.id, c.user_id, c.state, u.name, u.surname, u.email " +
                    "FROM candidate AS c " +
                    "JOIN user AS u ON c.user_id = u.id " +
                    "WHERE c.user_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, userId);  // Usa userId per impostare il parametro della query
                newCandidate(candidates, statement);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return candidates;
    }

    private void newCandidate(List<Candidate> candidates, PreparedStatement statement) throws SQLException {
        var resultSet = statement.executeQuery();
        while (resultSet.next()) {
            var candidate = new Candidate();
            candidate.setCandidateId(resultSet.getInt("id"));
            candidate.setIdUser(resultSet.getInt("user_id"));
            candidate.setTestState(TestState.valueOf(resultSet.getString("state")));
            var user = new User();
            user.setId(resultSet.getInt("user_id"));
            user.setName(resultSet.getString("name"));
            user.setSurname(resultSet.getString("surname"));
            user.setEmail(resultSet.getString("email"));
            candidate.setUser(user);
            candidates.add(candidate);
        }
    }

    public void updateCandidate(int userId, String testState) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("UPDATE candidate SET state = ? WHERE user_id = ?")) {
                    statement.setString(1, testState);
                    statement.setInt(2, userId);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
