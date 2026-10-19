package com.alstonsmith;

import com.alstonsmith.model.Team;
import com.alstonsmith.model.User;
import com.alstonsmith.service.NoteService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoteServiceTest {

    @Test
    void teamMembersCanSeeSharedNotesAndNonMembersCannot() {
        NoteService service = new NoteService();

        User alice = service.createUser("alice", "alice@example.com");
        User bob = service.createUser("bob", "bob@example.com");
        User carol = service.createUser("carol", "carol@example.com");

        Team productTeam = service.createTeam("Product", "Customer-facing planning");
        service.addMemberToTeam(productTeam.getId(), alice.getId());
        service.addMemberToTeam(productTeam.getId(), bob.getId());

        service.createNote(productTeam.getId(), alice.getId(), "Release checklist", "Ship on Friday");

        assertEquals(1, service.getTeamNotes(productTeam.getId()).size());
        assertEquals(1, service.getVisibleNotesForUser(bob.getId()).size());
        assertTrue(service.getVisibleNotesForUser(carol.getId()).isEmpty());
    }

    @Test
    void usersOutsideTheTeamCannotCreateNotes() {
        NoteService service = new NoteService();

        User alice = service.createUser("alice", "alice@example.com");
        User bob = service.createUser("bob", "bob@example.com");

        Team engineering = service.createTeam("Engineering", "Platform");
        service.addMemberToTeam(engineering.getId(), alice.getId());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.createNote(engineering.getId(), bob.getId(), "Incident", "Need attention")
        );

        assertEquals("Only team members can create notes in this team", ex.getMessage());
    }
}
