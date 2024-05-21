package org.acme.service.exception;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SessionCreatedException extends Exception {
    public SessionCreatedException(SQLException e) {
        super(e);
    }
    }
