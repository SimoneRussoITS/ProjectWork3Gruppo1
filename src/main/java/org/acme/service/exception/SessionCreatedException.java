package org.acme.service.exception;

import java.sql.SQLException;

public class SessionCreatedException extends Exception {
    public SessionCreatedException(SQLException e) {
        super(e);
    }
    }
