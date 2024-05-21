package org.acme.service.exception;

public class WrongCredentialException extends Exception {
    public WrongCredentialException() {
        super("Wrong credentials.");
    }
}
