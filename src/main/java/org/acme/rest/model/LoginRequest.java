package org.acme.rest.model;

public class LoginRequest {
    private String email;
    private String password;

    public String getEmail(String email) {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword(String password) {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
