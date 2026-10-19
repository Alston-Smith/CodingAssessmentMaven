package com.alstonsmith.service;

import com.alstonsmith.model.Note;
import com.alstonsmith.model.Team;
import com.alstonsmith.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class NoteService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Path DEFAULT_DATA_FILE = Paths.get(System.getProperty("user.dir"), "shared-notes-data.json");

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Team> teams = new ConcurrentHashMap<>();
    private final Map<String, Note> notes = new ConcurrentHashMap<>();
    private final Path dataFile;

    public NoteService() {
        this(DEFAULT_DATA_FILE);
    }

    public NoteService(Path dataFile) {
        this.dataFile = dataFile;
        loadData();
    }

    public User createUser(String username, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String id = generateId("user");
        User user = new User(id, username, email);
        users.put(id, user);
        saveData();
        return user;
    }

    public Team createTeam(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Team name is required");
        }

        String id = generateId("team");
        Team team = new Team(id, name, description);
        teams.put(id, team);
        saveData();
        return team;
    }

    public Team addMemberToTeam(String teamId, String userId) {
        Team team = requireTeam(teamId);
        User user = requireUser(userId);

        team.getMemberIds().add(userId);
        user.getTeamIds().add(teamId);
        saveData();
        return team;
    }

    public Note createNote(String teamId, String userId, String title, String content) {
        Team team = requireTeam(teamId);
        requireUser(userId);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content is required");
        }
        if (!team.getMemberIds().contains(userId)) {
            throw new IllegalArgumentException("Only team members can create notes in this team");
        }

        String id = generateId("note");
        Instant now = Instant.now();
        Note note = new Note(id, title, content, userId, teamId, now, now);
        notes.put(id, note);
        saveData();
        return note;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public List<Team> getTeams() {
        return new ArrayList<>(teams.values());
    }

    public List<Note> getTeamNotes(String teamId) {
        requireTeam(teamId);
        return notes.values().stream()
                .filter(note -> teamId.equals(note.getTeamId()))
                .sorted(Comparator.comparing(Note::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Note> getVisibleNotesForUser(String userId) {
        requireUser(userId);

        return notes.values().stream()
                .filter(note -> isUserAllowedToSeeNote(userId, note))
                .sorted(Comparator.comparing(Note::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Note updateNote(String noteId, String userId, String title, String content) {
        Note note = requireNote(noteId);

        if (!note.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("Only the author may update this note");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content is required");
        }

        note.setTitle(title);
        note.setContent(content);
        note.setUpdatedAt(Instant.now());
        saveData();
        return note;
    }

    public void deleteNote(String noteId, String userId) {
        Note note = requireNote(noteId);

        if (!note.getAuthorId().equals(userId)) {
            throw new IllegalArgumentException("Only the author may delete this note");
        }

        notes.remove(noteId);
        saveData();
    }

    public User getUser(String userId) {
        return requireUser(userId);
    }

    public Team getTeam(String teamId) {
        return requireTeam(teamId);
    }

    public Note getNote(String noteId) {
        return requireNote(noteId);
    }

    private boolean isUserAllowedToSeeNote(String userId, Note note) {
        if (note.getAuthorId().equals(userId)) {
            return true;
        }

        Team team = teams.get(note.getTeamId());
        return team != null && team.getMemberIds().contains(userId);
    }

    private User requireUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user;
    }

    private Team requireTeam(String teamId) {
        Team team = teams.get(teamId);
        if (team == null) {
            throw new IllegalArgumentException("Team not found: " + teamId);
        }
        return team;
    }

    private Note requireNote(String noteId) {
        Note note = notes.get(noteId);
        if (note == null) {
            throw new IllegalArgumentException("Note not found: " + noteId);
        }
        return note;
    }

    private void saveData() {
        try {
            Path parent = dataFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            DataFileSnapshot snapshot = new DataFileSnapshot();
            snapshot.users = new ArrayList<>(users.values());
            snapshot.teams = new ArrayList<>(teams.values());
            snapshot.notes = new ArrayList<>(notes.values());
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), snapshot);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save note data to " + dataFile, ex);
        }
    }

    private void loadData() {
        if (dataFile == null || !Files.exists(dataFile)) {
            return;
        }

        try {
            if (Files.size(dataFile) == 0) {
                return;
            }
            DataFileSnapshot snapshot = OBJECT_MAPPER.readValue(dataFile.toFile(), DataFileSnapshot.class);
            if (snapshot == null) {
                return;
            }

            users.clear();
            if (snapshot.users != null) {
                for (User user : snapshot.users) {
                    if (user != null && user.getId() != null) {
                        users.put(user.getId(), user);
                    }
                }
            }

            teams.clear();
            if (snapshot.teams != null) {
                for (Team team : snapshot.teams) {
                    if (team != null && team.getId() != null) {
                        teams.put(team.getId(), team);
                    }
                }
            }

            notes.clear();
            if (snapshot.notes != null) {
                for (Note note : snapshot.notes) {
                    if (note != null && note.getId() != null) {
                        notes.put(note.getId(), note);
                    }
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load note data from " + dataFile, ex);
        }
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static final class DataFileSnapshot {
        public List<User> users = new ArrayList<>();
        public List<Team> teams = new ArrayList<>();
        public List<Note> notes = new ArrayList<>();
    }
}
