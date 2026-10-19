package com.alstonsmith.dto;

/**
 * Payload used when a note author edits an existing message.
 */
public class UpdateNoteRequest {
    /** User requesting the update; must be the author of the note. */
    private String userId;
    /** New title for the note. */
    private String title;
    /** Updated message text. */
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
