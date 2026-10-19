package com.alstonsmith.dto;

/**
 * API payload for creating a new team. The creator's userId is required so the
 * team can automatically assign membership to the person creating it.
 */
public class CreateTeamRequest {
    /** Team display name. */
    private String name;
    /** Optional team description shown in the UI. */
    private String description;
    /** Account that is creating the team and is automatically added as a member. */
    private String userId;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
