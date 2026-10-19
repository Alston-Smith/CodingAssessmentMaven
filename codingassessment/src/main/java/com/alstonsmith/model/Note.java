package com.alstonsmith.model;

import java.time.Instant;

/**
 * A message posted inside a team. Notes retain the author, team, and timestamps
 * so permissions and activity history can be enforced.
 */
public class Note {
    /** Unique note identifier used in REST update/delete routes. */
    private String id;
    /** Title shown in the GUI list for quick scanning. */
    private String title;
    /** Main message body written by the author. */
    private String content;
    /** User who created the note; only the author may update or delete it. */
    private String authorId;
    /** Team that owns this note. */
    private String teamId;
    /** When the note was first created. */
    private Instant createdAt;
    /** When the note was last edited. */
    private Instant updatedAt;

    public Note() {
    }

    public Note(String id, String title, String content, String authorId, String teamId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.teamId = teamId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
