package com.alstonsmith.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a person who can create teams, join teams, and author notes.
 * The password hash is stored rather than the raw password for security.
 */
public class User {
    /** Unique account identifier used in API routes and persistence files. */
    private String id;
    /** Display name the user chooses for their account. */
    private String username;
    /** Contact email used for account identity and UI display. */
    private String email;
    /** Encrypted version of the account password. */
    private String passwordHash;
    /** Teams that this user belongs to so permission checks can be resolved quickly. */
    private Set<String> teamIds = new HashSet<>();

    public User() {
    }

    public User(String id, String username, String email, String passwordHash) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<String> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(Set<String> teamIds) {
        this.teamIds = teamIds;
    }
}
