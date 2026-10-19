package com.alstonsmith.dto;

/**
 * Payload for creating a note inside a specific team.
 */
public class CreateNoteRequest {
    /** Account creating the message. */
    private String userId;
    /** Message title shown in the team list. */
    private String title;
    /** Main body of the note. */
    private String content;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
