package com.alstonsmith;

import com.alstonsmith.model.Note;
import com.alstonsmith.model.Team;
import com.alstonsmith.model.User;
import com.alstonsmith.service.NoteService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoteServiceTest {

    private NoteService newService() throws IOException {
        Path tempDir = Files.createTempDirectory("shared-notes-data");
        return new NoteService(tempDir.resolve("shared-notes-data.json"));
    }

    @Test
    void teamMembersCanSeeSharedNotesAndNonMembersCannot() throws IOException {
        NoteService service = newService();

        User alice = service.createUser("alice", "alice@example.com", "Password123!");
        User bob = service.createUser("bob", "bob@example.com", "Password123!");
        User carol = service.createUser("carol", "carol@example.com", "Password123!");

        Team productTeam = service.createTeam("Product", "Customer-facing planning", alice.getId());
        service.addMemberToTeam(productTeam.getId(), bob.getId());

        service.createNote(productTeam.getId(), alice.getId(), "Release checklist", "Ship on Friday");

        assertEquals(1, service.getTeamNotes(productTeam.getId()).size());
        assertEquals(1, service.getVisibleNotesForUser(bob.getId()).size());
        assertTrue(service.getVisibleNotesForUser(carol.getId()).isEmpty());
    }

    @Test
    void usersOutsideTheTeamCannotCreateNotes() throws IOException {
        NoteService service = newService();

        User alice = service.createUser("alice", "alice@example.com", "Password123!");
        User bob = service.createUser("bob", "bob@example.com", "Password123!");

        Team engineering = service.createTeam("Engineering", "Platform", alice.getId());
        service.addMemberToTeam(engineering.getId(), alice.getId());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.createNote(engineering.getId(), bob.getId(), "Incident", "Need attention")
        );

        assertEquals("Only team members can create notes in this team", ex.getMessage());
    }

    @Test
    void servicePersistsUsersTeamsAndNotesAcrossRestarts() throws IOException {
        Path tempDir = Files.createTempDirectory("shared-notes-data");
        Path dataFile = tempDir.resolve("shared-notes-data.json");

        NoteService firstService = new NoteService(dataFile);
        User alice = firstService.createUser("alice", "alice@example.com", "Password123!");
        User bob = firstService.createUser("bob", "bob@example.com", "Password123!");
        Team productTeam = firstService.createTeam("Product", "Customer-facing planning", alice.getId());
        firstService.addMemberToTeam(productTeam.getId(), bob.getId());
        firstService.createNote(productTeam.getId(), alice.getId(), "Release checklist", "Ship on Friday");

        NoteService secondService = new NoteService(dataFile);

        assertEquals(2, secondService.getUsers().size());
        assertEquals(1, secondService.getTeams().size());
        assertEquals(1, secondService.getTeamNotes(productTeam.getId()).size());
        assertEquals(1, secondService.getVisibleNotesForUser(bob.getId()).size());
    }

    @Test
    void noteAuthorCanUpdateAndDeleteTheirOwnMessage() throws IOException {
        NoteService service = newService();

        User alice = service.createUser("alice", "alice@example.com", "Password123!");
        Team productTeam = service.createTeam("Product", "Customer-facing planning", alice.getId());

        Note note = service.createNote(productTeam.getId(), alice.getId(), "Original title", "Original content");

        Note updated = service.updateNote(note.getId(), alice.getId(), "Updated title", "Updated content");
        assertEquals("Updated title", updated.getTitle());
        assertEquals("Updated content", updated.getContent());

        service.deleteNote(note.getId(), alice.getId());
        assertTrue(service.getTeamNotes(productTeam.getId()).isEmpty());
    }

    @Test
    void duplicateUsernamesAndTeamNamesAreRejected() throws IOException {
        NoteService service = newService();

        service.createUser("alice", "alice@example.com", "Password123!");
        IllegalArgumentException duplicateUser = assertThrows(
                IllegalArgumentException.class,
                () -> service.createUser("ALICE", "other@example.com", "Password123!")
        );
        assertEquals("Username already exists", duplicateUser.getMessage());

        service.createTeam("Product", "Customer-facing planning", service.createUser("bob", "bob@example.com", "Password123!").getId());
        IllegalArgumentException duplicateTeam = assertThrows(
                IllegalArgumentException.class,
                () -> service.createTeam("product", "Other description", service.createUser("carol", "carol@example.com", "Password123!").getId())
        );
        assertEquals("Team name already exists", duplicateTeam.getMessage());
    }

    @Test
    void userPasswordIsEncryptedAndCanBeValidated() throws IOException {
        NoteService service = newService();

        User user = service.createUser("alice", "alice@example.com", "Password123!");

        assertTrue(user.getPasswordHash() != null && !user.getPasswordHash().isBlank());
        assertTrue(service.verifyPassword("alice", "Password123!"));
        assertTrue(!service.verifyPassword("alice", "WrongPassword"));
    }

    @Test
    void passwordCanBeValidatedByUserId() throws IOException {
        NoteService service = newService();

        User user = service.createUser("alice", "alice@example.com", "Password123!");

        assertTrue(service.verifyPasswordForUser(user.getId(), "Password123!"));
        assertTrue(!service.verifyPasswordForUser(user.getId(), "WrongPassword"));
    }
}
