package org.acme.persistence.repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionRepository {
    private final DataSource dataSource;

    public SessionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int insertSession(int idUser) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO session (session_id) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1,idUser);
                ps.executeUpdate();
                ResultSet key = ps.getGeneratedKeys();
                if (key.next()) {
                    int id = key.getInt(1);
                    return id;
                }
            }
        }
        throw new SQLException("Cannot insert new session for user " + idUser);
    }
}
