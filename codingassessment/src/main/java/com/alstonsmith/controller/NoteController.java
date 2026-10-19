package com.alstonsmith.controller;

import com.alstonsmith.dto.AddMemberRequest;
import com.alstonsmith.dto.CreateNoteRequest;
import com.alstonsmith.dto.CreateTeamRequest;
import com.alstonsmith.dto.CreateUserRequest;
import com.alstonsmith.dto.DeleteNoteRequest;
import com.alstonsmith.dto.UpdateNoteRequest;
import com.alstonsmith.model.Note;
import com.alstonsmith.model.Team;
import com.alstonsmith.model.User;
import com.alstonsmith.service.NoteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller that exposes the shared notes application API.
 * Clients can create accounts, manage teams, and post or edit notes through these endpoints.
 */
@RestController
@RequestMapping("/api")
public class NoteController {
    /** Business logic provider for account, team, and note operations. */
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * Simple health check used by the GUI and by operators to confirm the backend is running.
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    /**
     * Returns all users currently known to the service.
     */
    @GetMapping("/users")
    public List<User> getUsers() {
        return noteService.getUsers();
    }

    /**
     * Creates a user account from the JSON payload posted by the desktop client.
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody CreateUserRequest request) {
        return noteService.createUser(request.getUsername(), request.getEmail(), request.getPassword());
    }

    /**
     * Fetches a single user record by identifier. Used by the GUI for account selection and validation.
     */
    @GetMapping("/users/{userId}")
    public User getUser(@PathVariable("userId") String userId) {
        return noteService.getUser(userId);
    }

    /**
     * Validates a password for the selected account before allowing team or message actions.
     */
    @PostMapping("/users/{userId}/verify-password")
    public Map<String, Boolean> verifyPassword(@PathVariable("userId") String userId, @RequestBody Map<String, String> requestBody) {
        String password = requestBody == null ? null : requestBody.get("password");
        return Map.of("valid", noteService.verifyPasswordForUser(userId, password));
    }

    /**
     * Creates a new team and automatically adds the configured creator to the team membership.
     */
    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public Team createTeam(@RequestBody CreateTeamRequest request) {
        return noteService.createTeam(request.getName(), request.getDescription(), request.getUserId());
    }

    /**
     * Returns every team that currently exists in the shared notes service.
     */
    @GetMapping("/teams")
    public List<Team> getTeams() {
        return noteService.getTeams();
    }

    /**
     * Adds an existing user to the selected team.
     */
    @PostMapping("/teams/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Team addMember(@PathVariable("teamId") String teamId, @RequestBody AddMemberRequest request) {
        return noteService.addMemberToTeam(teamId, request.getUserId());
    }

    /**
     * Lists all notes belonging to a team, ordered newest first.
     */
    @GetMapping("/teams/{teamId}/notes")
    public List<Note> getTeamNotes(@PathVariable("teamId") String teamId) {
        return noteService.getTeamNotes(teamId);
    }

    /**
     * Creates a note in the selected team and checks that the author is a team member.
     */
    @PostMapping("/teams/{teamId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public Note createNote(@PathVariable("teamId") String teamId, @RequestBody CreateNoteRequest request) {
        return noteService.createNote(teamId, request.getUserId(), request.getTitle(), request.getContent());
    }

    /**
     * Returns every note a user is allowed to see across all teams they can access.
     */
    @GetMapping("/users/{userId}/notes")
    public List<Note> getVisibleNotes(@PathVariable("userId") String userId) {
        return noteService.getVisibleNotesForUser(userId);
    }

    /**
     * Updates a note only when the caller is the original author.
     */
    @PutMapping("/notes/{noteId}")
    public Note updateNote(@PathVariable("noteId") String noteId, @RequestBody UpdateNoteRequest request) {
        return noteService.updateNote(noteId, request.getUserId(), request.getTitle(), request.getContent());
    }

    /**
     * Deletes a note when the caller is the original author.
     */
    @DeleteMapping("/notes/{noteId}")
    public void deleteNote(@PathVariable("noteId") String noteId, @RequestBody DeleteNoteRequest request) {
        noteService.deleteNote(noteId, request.getUserId());
    }
}
