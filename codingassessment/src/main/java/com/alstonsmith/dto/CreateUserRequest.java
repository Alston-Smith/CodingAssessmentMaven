package com.alstonsmith.dto;

/**
 * API payload used when a new account is created from the GUI or an external client.
 */
public class CreateUserRequest {
    /** Account name shown in the application. */
    private String username;
    /** Contact email for the new user. */
    private String email;
    /** Plain-text password that is validated and then encrypted before storage. */
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
