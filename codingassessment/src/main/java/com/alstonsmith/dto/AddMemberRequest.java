package com.alstonsmith.dto;

/**
 * Payload used when adding an existing user to a team.
 */
public class AddMemberRequest {
    /** User account to add to the selected team. */
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
