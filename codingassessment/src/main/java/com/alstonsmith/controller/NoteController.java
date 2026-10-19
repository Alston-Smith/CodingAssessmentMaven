package com.alstonsmith.controller;

import com.alstonsmith.dto.AddMemberRequest;
import com.alstonsmith.dto.CreateNoteRequest;
import com.alstonsmith.dto.CreateTeamRequest;
import com.alstonsmith.dto.CreateUserRequest;
import com.alstonsmith.dto.UpdateNoteRequest;
import com.alstonsmith.model.Note;
import com.alstonsmith.model.Team;
import com.alstonsmith.model.User;
import com.alstonsmith.service.NoteService;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping("/api")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return noteService.getUsers();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody CreateUserRequest request) {
        return noteService.createUser(request.getUsername(), request.getEmail());
    }

    @GetMapping("/users/{userId}")
    public User getUser(@PathVariable String userId) {
        return noteService.getUser(userId);
    }

    @PostMapping("/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public Team createTeam(@RequestBody CreateTeamRequest request) {
        return noteService.createTeam(request.getName(), request.getDescription());
    }

    @GetMapping("/teams")
    public List<Team> getTeams() {
        return noteService.getTeams();
    }

    @PostMapping("/teams/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Team addMember(@PathVariable String teamId, @RequestBody AddMemberRequest request) {
        return noteService.addMemberToTeam(teamId, request.getUserId());
    }

    @GetMapping("/teams/{teamId}/notes")
    public List<Note> getTeamNotes(@PathVariable String teamId) {
        return noteService.getTeamNotes(teamId);
    }

    @PostMapping("/teams/{teamId}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public Note createNote(@PathVariable String teamId, @RequestBody CreateNoteRequest request) {
        return noteService.createNote(teamId, request.getUserId(), request.getTitle(), request.getContent());
    }

    @GetMapping("/users/{userId}/notes")
    public List<Note> getVisibleNotes(@PathVariable String userId) {
        return noteService.getVisibleNotesForUser(userId);
    }

    @PutMapping("/notes/{noteId}")
    public Note updateNote(@PathVariable String noteId, @RequestBody UpdateNoteRequest request) {
        return noteService.updateNote(noteId, request.getUserId(), request.getTitle(), request.getContent());
    }
}
