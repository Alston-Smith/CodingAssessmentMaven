package com.alstonsmith.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a collaboration group. Team membership controls whether a user can
 * view or post messages in the team.
 */
public class Team {
    /** Unique team identifier used in URLs and persistence storage. */
    private String id;
    /** Friendly team name shown in the GUI and used for duplicate checks. */
    private String name;
    /** Optional description that explains the team purpose. */
    private String description;
    /** Members allowed to read and post notes in this team. */
    private Set<String> memberIds = new HashSet<>();

    public Team() {
    }

    public Team(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(Set<String> memberIds) {
        this.memberIds = memberIds;
    }
}
