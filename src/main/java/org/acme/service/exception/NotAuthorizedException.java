package org.acme.service.exception;

public class NotAuthorizedException extends Exception {
    public NotAuthorizedException() {
        super("Not authorized.");
    }
}
