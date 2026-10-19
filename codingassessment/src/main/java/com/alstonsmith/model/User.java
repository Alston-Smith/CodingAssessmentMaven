package com.alstonsmith.model;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String id;
    private String username;
    private String email;
    private String passwordHash;
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
