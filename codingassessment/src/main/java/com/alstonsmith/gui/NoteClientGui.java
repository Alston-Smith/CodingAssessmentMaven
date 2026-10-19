package com.alstonsmith.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desktop client that talks to the Spring Boot API and lets users select an account,
 * join teams, and manage shared notes without calling the REST endpoints by hand.
 */
public class NoteClientGui extends JFrame {
    /** JSON mapper that ignores extra fields from the API to keep the GUI flexible. */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final DefaultComboBoxModel<UserOption> usersModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<TeamOption> teamsModel = new DefaultComboBoxModel<>();

    private final JComboBox<UserOption> userCombo = new JComboBox<>(usersModel);
    private final JComboBox<TeamOption> teamCombo = new JComboBox<>(teamsModel);
    private final JTextField titleField = new JTextField();
    private final JTextArea contentArea = new JTextArea(10, 40);
    private final JTextField newUserNameField = new JTextField();
    private final JTextField newUserEmailField = new JTextField();
    private final JPasswordField newUserPasswordField = new JPasswordField();
    private final JTextField newTeamNameField = new JTextField();
    private final JTextArea newTeamDescriptionArea = new JTextArea(3, 20);
    private final JButton createTeamButton = new JButton("Create Team");
    private final JTextArea statusArea = new JTextArea("Ready\n");
    private final DefaultListModel<TeamNote> teamNotesModel = new DefaultListModel<>();
    private final JList<TeamNote> teamNotesList = new JList<>(teamNotesModel);
    private final JTextArea teamNotesDetailArea = new JTextArea();
    private final Map<String, Boolean> verifiedUsers = new HashMap<>();
    private boolean suppressSelectionEvents = false;

    private final String baseUrl;

    /**
     * Creates the Swing client for the configured API base URL.
     *
     * @param baseUrl the root URL of the backend service, such as http://localhost:8080/api
     */
    public NoteClientGui(String baseUrl) {
        super("Shared Notes");
        this.baseUrl = baseUrl;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension((int) screenSize.getWidth(), (int) screenSize.getHeight()));
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Account:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        userCombo.setSelectedItem(null);
        userCombo.setPrototypeDisplayValue(new UserOption());
        userCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Select an account");
                }
                return this;
            }
        });
        form.add(userCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Team:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        teamCombo.setSelectedItem(null);
        teamCombo.setPrototypeDisplayValue(new TeamOption());
        teamCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("Select a team");
                }
                return this;
            }
        });
        form.add(teamCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        form.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        form.add(scrollPane, gbc);

        JPanel createUserPanel = new JPanel(new GridBagLayout());
        createUserPanel.setBorder(BorderFactory.createTitledBorder("Create New User"));
        GridBagConstraints userGbc = new GridBagConstraints();
        userGbc.insets = new Insets(6, 6, 6, 6);
        userGbc.fill = GridBagConstraints.HORIZONTAL;

        userGbc.gridx = 0;
        userGbc.gridy = 0;
        createUserPanel.add(new JLabel("Username:"), userGbc);

        userGbc.gridx = 1;
        userGbc.weightx = 1;
        createUserPanel.add(newUserNameField, userGbc);

        userGbc.gridx = 0;
        userGbc.gridy = 1;
        userGbc.weightx = 0;
        createUserPanel.add(new JLabel("Email:"), userGbc);

        userGbc.gridx = 1;
        userGbc.weightx = 1;
        createUserPanel.add(newUserEmailField, userGbc);

        userGbc.gridx = 0;
        userGbc.gridy = 2;
        userGbc.weightx = 0;
        createUserPanel.add(new JLabel("Password:"), userGbc);

        userGbc.gridx = 1;
        userGbc.weightx = 1;
        createUserPanel.add(newUserPasswordField, userGbc);

        JButton createUserButton = new JButton("Create User");
        createUserButton.addActionListener(e -> createUser());
        userGbc.gridx = 0;
        userGbc.gridy = 3;
        userGbc.gridwidth = 2;
        userGbc.weightx = 1;
        userGbc.fill = GridBagConstraints.HORIZONTAL;
        createUserPanel.add(createUserButton, userGbc);

        JPanel createTeamPanel = new JPanel(new GridBagLayout());
        createTeamPanel.setBorder(BorderFactory.createTitledBorder("Create New Team"));
        GridBagConstraints teamGbc = new GridBagConstraints();
        teamGbc.insets = new Insets(6, 6, 6, 6);
        teamGbc.fill = GridBagConstraints.HORIZONTAL;

        teamGbc.gridx = 0;
        teamGbc.gridy = 0;
        createTeamPanel.add(new JLabel("Team Name:"), teamGbc);

        teamGbc.gridx = 1;
        teamGbc.weightx = 1;
        createTeamPanel.add(newTeamNameField, teamGbc);

        teamGbc.gridx = 0;
        teamGbc.gridy = 1;
        teamGbc.weightx = 0;
        createTeamPanel.add(new JLabel("Description:"), teamGbc);

        teamGbc.gridx = 1;
        teamGbc.weightx = 1;
        teamGbc.fill = GridBagConstraints.BOTH;
        newTeamDescriptionArea.setLineWrap(true);
        newTeamDescriptionArea.setWrapStyleWord(true);
        createTeamPanel.add(new JScrollPane(newTeamDescriptionArea), teamGbc);

        createTeamButton.setEnabled(false);
        createTeamButton.addActionListener(e -> createTeam());
        teamGbc.gridx = 0;
        teamGbc.gridy = 2;
        teamGbc.gridwidth = 2;
        teamGbc.weightx = 1;
        teamGbc.fill = GridBagConstraints.HORIZONTAL;
        createTeamPanel.add(createTeamButton, teamGbc);

        JPanel creationPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        creationPanel.add(createUserPanel);
        creationPanel.add(createTeamPanel);

        JPanel teamOverview = new JPanel(new BorderLayout(8, 8));
        teamOverview.setBorder(BorderFactory.createTitledBorder("Team Messages"));
        teamNotesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        teamNotesDetailArea.setEditable(false);
        teamNotesDetailArea.setLineWrap(true);
        teamNotesDetailArea.setWrapStyleWord(true);
        teamNotesDetailArea.setBackground(new Color(250, 250, 250));
        teamNotesDetailArea.setText("Select a team to view its messages.");
        teamNotesList.addListSelectionListener(e -> showSelectedTeamNoteDetails());

        JPanel teamNotesPanel = new JPanel(new BorderLayout(8, 8));
        teamNotesPanel.add(new JScrollPane(teamNotesList), BorderLayout.WEST);
        teamNotesPanel.add(new JScrollPane(teamNotesDetailArea), BorderLayout.CENTER);

        JPanel teamActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshTeamNotesButton = new JButton("Refresh Team Notes");
        JButton addMemberButton = new JButton("Add Selected User to Team");
        JButton editMessageButton = new JButton("Edit Selected Message");
        JButton deleteMessageButton = new JButton("Delete Selected Message");
        refreshTeamNotesButton.addActionListener(e -> refreshTeamNotes());
        addMemberButton.addActionListener(e -> addSelectedUserToTeam());
        editMessageButton.addActionListener(e -> editSelectedMessage());
        deleteMessageButton.addActionListener(e -> deleteSelectedMessage());
        teamActions.add(addMemberButton);
        teamActions.add(refreshTeamNotesButton);
        teamActions.add(editMessageButton);
        teamActions.add(deleteMessageButton);

        teamOverview.add(teamActions, BorderLayout.NORTH);
        teamOverview.add(teamNotesPanel, BorderLayout.CENTER);

        JPanel teamPanel = new JPanel(new BorderLayout(12, 12));
        teamPanel.add(creationPanel, BorderLayout.NORTH);
        teamPanel.add(teamOverview, BorderLayout.CENTER);

        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.add(form, BorderLayout.CENTER);
        container.add(teamPanel, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh Lists");
        JButton submitButton = new JButton("Post Message");
        actions.add(refreshButton);
        actions.add(submitButton);

        refreshButton.addActionListener(e -> loadSelections());
        submitButton.addActionListener(e -> postMessage());

        add(container, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setOpaque(true);
        statusArea.setBackground(new Color(245, 245, 245));

        JButton copyStatusButton = new JButton("Copy Status");
        copyStatusButton.addActionListener(e -> copyStatusToClipboard());

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(0, 70));
        footer.add(statusScroll, BorderLayout.CENTER);
        footer.add(copyStatusButton, BorderLayout.EAST);
        add(footer, BorderLayout.NORTH);

        userCombo.addActionListener(e -> {
            if (suppressSelectionEvents) {
                return;
            }

            UserOption selectedUser = (UserOption) userCombo.getSelectedItem();
            if (selectedUser == null) {
                createTeamButton.setEnabled(false);
                refreshTeamNotes();
                return;
            }

            createTeamButton.setEnabled(isUserVerified(selectedUser));
            if (!isUserVerified(selectedUser)) {
                authenticateSelectedUser();
            }
            refreshTeamNotes();
        });
        teamCombo.addActionListener(e -> {
            if (!suppressSelectionEvents) {
                refreshTeamNotes();
            }
        });

        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        loadSelections();
    }

    private void setStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.setText(message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }

    private void copyStatusToClipboard() {
        StringSelection selection = new StringSelection(statusArea.getText());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    /**
     * Pulls the latest user and team data from the backend and repopulates the dropdowns.
     */
    private void loadSelections() {
        setStatus("Loading accounts and teams...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    List<UserOption> users = fetchUsers();
                    List<TeamOption> teams = fetchTeams();
                    SwingUtilities.invokeLater(() -> {
                        suppressSelectionEvents = true;
                        try {
                            usersModel.removeAllElements();
                            for (UserOption user : users) {
                                usersModel.addElement(user);
                            }
                            userCombo.setSelectedItem(null);

                            teamsModel.removeAllElements();
                            for (TeamOption team : teams) {
                                teamsModel.addElement(team);
                            }
                            teamCombo.setSelectedItem(null);
                        } finally {
                            suppressSelectionEvents = false;
                        }

                        setStatus("Loaded " + usersModel.getSize() + " account(s) and " + teamsModel.getSize() + " team(s)");
                        refreshTeamNotes();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> setStatus("Failed to load data: " + ex.getMessage()));
                }
                return null;
            }
        };
        worker.execute();
    }

    private boolean isUserVerified(UserOption user) {
        return user != null && Boolean.TRUE.equals(verifiedUsers.get(user.id));
    }

    /**
     * Prompts the user for the selected account password and keeps the authentication
     * state cached so the user is not re-prompted repeatedly after a successful login.
     */
    private boolean promptForUserPassword(UserOption user) {
        if (user == null) {
            return false;
        }

        if (isUserVerified(user)) {
            return true;
        }

        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(
                this,
                passwordField,
                "Enter the password for " + user.username,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            verifiedUsers.remove(user.id);
            userCombo.setSelectedItem(null);
            createTeamButton.setEnabled(false);
            setStatus("Password required to use account " + user.username + ".");
            return false;
        }

        String password = new String(passwordField.getPassword()).trim();
        try {
            boolean valid = verifyUserPassword(user.id, password);
            if (!valid) {
                verifiedUsers.remove(user.id);
                userCombo.setSelectedItem(null);
                createTeamButton.setEnabled(false);
                setStatus("Incorrect password for " + user.username + ".");
                JOptionPane.showMessageDialog(this, "Incorrect password. Please try again.", "Authentication failed", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            verifiedUsers.put(user.id, true);
            createTeamButton.setEnabled(true);
            setStatus("Authenticated as " + user.username + ".");
            return true;
        } catch (Exception ex) {
            verifiedUsers.remove(user.id);
            userCombo.setSelectedItem(null);
            createTeamButton.setEnabled(false);
            setStatus("Failed to verify password for " + user.username + ": " + getErrorMessage(ex));
            return false;
        }
    }

    private void authenticateSelectedUser() {
        UserOption selectedUser = (UserOption) userCombo.getSelectedItem();
        if (selectedUser == null) {
            return;
        }
        boolean authenticated = promptForUserPassword(selectedUser);
        createTeamButton.setEnabled(authenticated && selectedUser != null);
        if (!authenticated) {
            verifiedUsers.remove(selectedUser.id);
        }
    }

    private boolean verifyUserPassword(String userId, String password) throws IOException, InterruptedException {
        String payload = OBJECT_MAPPER.writeValueAsString(Map.of("password", password));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/users/" + userId + "/verify-password"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("Server responded with " + response.statusCode() + ": " + response.body());
        }

        Map<String, Boolean> verification = OBJECT_MAPPER.readValue(response.body(), new TypeReference<Map<String, Boolean>>() {});
        return Boolean.TRUE.equals(verification.get("valid"));
    }

    private void createUser() {
        String username = newUserNameField.getText() == null ? "" : newUserNameField.getText().trim();
        String email = newUserEmailField.getText() == null ? "" : newUserEmailField.getText().trim();
        String password = new String(newUserPasswordField.getPassword()).trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            setStatus("Please enter a username, email, and password.");
            return;
        }

        setStatus("Creating user...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    ensureServerAvailable();
                    postJson("/api/users", Map.of("username", username, "email", email, "password", password));

                    newUserNameField.setText("");
                    newUserEmailField.setText("");
                    newUserPasswordField.setText("");
                    loadSelections();
                    SwingUtilities.invokeLater(() -> setStatus("User created: " + username));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> setStatus("Failed to create user: " + getErrorMessage(ex)));
                }
                return null;
            }
        };
        worker.execute();
    }

    private void createTeam() {
        UserOption selectedUser = (UserOption) userCombo.getSelectedItem();
        String teamName = newTeamNameField.getText() == null ? "" : newTeamNameField.getText().trim();
        String description = newTeamDescriptionArea.getText() == null ? "" : newTeamDescriptionArea.getText().trim();

        if (selectedUser == null) {
            setStatus("Please select an account before creating a team.");
            return;
        }

        if (!promptForUserPassword(selectedUser)) {
            return;
        }

        if (teamName.isEmpty()) {
            setStatus("Please enter a team name.");
            return;
        }

        setStatus("Creating team...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    ensureServerAvailable();
                    postJson("/api/teams", Map.of("name", teamName, "description", description, "userId", selectedUser.id));

                    newTeamNameField.setText("");
                    newTeamDescriptionArea.setText("");
                    loadSelections();
                    SwingUtilities.invokeLater(() -> setStatus("Team created: " + teamName + " and added " + selectedUser.username + " to the team."));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> setStatus("Failed to create team: " + getErrorMessage(ex)));
                }
                return null;
            }
        };
        worker.execute();
    }

    private void addSelectedUserToTeam() {
        final UserOption selectedUser = (UserOption) userCombo.getSelectedItem();
        final TeamOption selectedTeam = (TeamOption) teamCombo.getSelectedItem();

        if (selectedUser == null || selectedTeam == null) {
            setStatus("Please select both an account and a team.");
            return;
        }

        if (!promptForUserPassword(selectedUser)) {
            return;
        }

        setStatus("Adding account to team...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    ensureServerAvailable();
                    postJson("/api/teams/" + selectedTeam.id + "/members", Map.of("userId", selectedUser.id));
                    SwingUtilities.invokeLater(() -> {
                        loadSelections();
                        refreshTeamNotes();
                        setStatus("Added " + selectedUser.username + " to " + selectedTeam.name + ".");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> setStatus("Failed to add user to team: " + getErrorMessage(ex)));
                }
                return null;
            }
        };
        worker.execute();
    }

    /**
     * Validates account selection and password before sending a new team message.
     */
    private void postMessage() {
        final UserOption selectedUser = (UserOption) userCombo.getSelectedItem();
        final TeamOption selectedTeam = (TeamOption) teamCombo.getSelectedItem();
        final String title = titleField.getText() == null ? "" : titleField.getText().trim();
        final String content = contentArea.getText() == null ? "" : contentArea.getText().trim();

        if (selectedUser == null || selectedTeam == null) {
            setStatus("Please select both an account and a team.");
            return;
        }

        if (!promptForUserPassword(selectedUser)) {
            return;
        }

        if (title.isEmpty()) {
            setStatus("Please enter a message title.");
            return;
        }

        if (content.isEmpty()) {
            setStatus("Please enter a message.");
            return;
        }

        setStatus("Posting note...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    ensureServerAvailable();
                    postJson("/api/teams/" + selectedTeam.id + "/notes", Map.of(
                            "userId", selectedUser.id,
                            "title", title,
                            "content", content
                    ));

                    SwingUtilities.invokeLater(() -> {
                        titleField.setText("");
                        contentArea.setText("");
                        refreshTeamNotes();
                        setStatus("Message posted to " + selectedTeam.name + " by " + selectedUser.username + ".");
                    });
                } catch (Exception ex) {
                    String errorMessage = getErrorMessage(ex);
                    final String displayErrorMessage = errorMessage.contains("Only team members")
                            ? errorMessage + " Add the selected account to this team before posting."
                            : errorMessage;
                    SwingUtilities.invokeLater(() -> setStatus("Failed to post message: " + displayErrorMessage));
                }
                return null;
            }
        };
        worker.execute();
    }

    /**
     * Loads the notes for the selected team and updates the detail panel shown in the GUI.
     */
    private void refreshTeamNotes() {
        final TeamOption selectedTeam = (TeamOption) teamCombo.getSelectedItem();
        if (selectedTeam == null) {
            teamNotesModel.clear();
            teamNotesDetailArea.setText("Select a team to view its messages.");
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    List<TeamNote> notes = fetchTeamNotes(selectedTeam.id);
                    SwingUtilities.invokeLater(() -> {
                        teamNotesModel.clear();
                        for (TeamNote note : notes) {
                            teamNotesModel.addElement(note);
                        }
                        if (teamNotesModel.isEmpty()) {
                            teamNotesDetailArea.setText("No messages in this team yet.");
                            return;
                        }
                        teamNotesList.setSelectedIndex(0);
                        showSelectedTeamNoteDetails();
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        teamNotesModel.clear();
                        teamNotesDetailArea.setText("Unable to load messages for " + selectedTeam.name + ": " + getErrorMessage(ex));
                    });
                }
                return null;
            }
        };
        worker.execute();
    }

    private void showSelectedTeamNoteDetails() {
        TeamNote selectedNote = teamNotesList.getSelectedValue();
        if (selectedNote == null) {
            teamNotesDetailArea.setText("Select a message to view details.");
            return;
        }
        teamNotesDetailArea.setText(formatMessage(selectedNote));
    }

    private void editSelectedMessage() {
        final UserOption selectedUser = (UserOption) userCombo.getSelectedItem();
        final TeamNote selectedNote = teamNotesList.getSelectedValue();
        if (selectedUser == null || selectedNote == null) {
            setStatus("Please select a user and a message to edit.");
            return;
        }

        if (!promptForUserPassword(selectedUser)) {
            return;
        }

        JTextField titleFieldEditor = new JTextField(selectedNote.title == null ? "" : selectedNote.title);
        JTextArea contentAreaEditor = new JTextArea(selectedNote.content == null ? "" : selectedNote.content, 8, 40);
        contentAreaEditor.setLineWrap(true);
        contentAreaEditor.setWrapStyleWord(true);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel("Title:"), BorderLayout.NORTH);
        panel.add(titleFieldEditor, BorderLayout.CENTER);
        panel.add(new JScrollPane(contentAreaEditor), BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Team Message", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        final String newTitle = titleFieldEditor.getText() == null ? "" : titleFieldEditor.getText().trim();
        final String newContent = contentAreaEditor.getText() == null ? "" : contentAreaEditor.getText().trim();
        if (newTitle.isBlank() || newContent.isBlank()) {
            setStatus("Title and message cannot be empty.");
            return;
        }

        setStatus("Updating note...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    ensureServerAvailable();
                    putJson("/api/notes/" + selectedNote.id, Map.of("userId", selectedUser.id, "title", newTitle, "content", newContent));
                    SwingUtilities.invokeLater(() -> {
                        refreshTeamNotes();
                        setStatus("Message updated by " + selectedUser.username + ".");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> setStatus("Failed to update message: " + getErrorMessage(ex)));
                }
                return null;
            }
        };
        worker.execute();
    }

    private void deleteSelectedMessage() {
        final UserOption selectedUser = (UserOption) userCombo.getSelectedItem();
        final TeamNote selectedNote = teamNotesList.getSelectedValue();
        if (selectedUser == null || selectedNote == null) {
            setStatus("Please select a message to delete.");
            return;
        }

        if (!promptForUserPassword(selectedUser)) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete the selected message?\n\n" + selectedNote.title,
                "Delete Message",
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setStatus("Deleting note...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    ensureServerAvailable();
                    deleteJson("/api/notes/" + selectedNote.id, Map.of("userId", selectedUser.id));
                    SwingUtilities.invokeLater(() -> {
                        refreshTeamNotes();
                        setStatus("Message deleted by " + selectedUser.username + ".");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> setStatus("Failed to delete message: " + getErrorMessage(ex)));
                }
                return null;
            }
        };
        worker.execute();
    }

    private List<TeamNote> fetchTeamNotes(String teamId) throws IOException, InterruptedException {
        String body = get(baseUrl + "/api/teams/" + teamId + "/notes");
        return OBJECT_MAPPER.readValue(body, new TypeReference<List<TeamNote>>() {});
    }

    private String formatMessage(TeamNote note) {
        if (note == null) {
            return "No message selected.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Title: ")
                .append(note.title == null || note.title.isBlank() ? "(untitled)" : note.title)
                .append("\n\n");
        if (note.content != null && !note.content.isBlank()) {
            builder.append(note.content).append("\n\n");
        }
        builder.append("Author: ")
                .append(note.authorId == null ? "unknown" : note.authorId)
                .append("\n");
        if (note.createdAt != null) {
            builder.append("Created: ").append(note.createdAt).append("\n");
        }
        if (note.updatedAt != null && !note.updatedAt.equals(note.createdAt)) {
            builder.append("Updated: ").append(note.updatedAt).append("\n");
        }
        return builder.toString();
    }

    private List<UserOption> fetchUsers() throws IOException, InterruptedException {
        String body = get(baseUrl + "/api/users");
        return OBJECT_MAPPER.readValue(body, new TypeReference<List<UserOption>>() {});
    }

    private List<TeamOption> fetchTeams() throws IOException, InterruptedException {
        String body = get(baseUrl + "/api/teams");
        return OBJECT_MAPPER.readValue(body, new TypeReference<List<TeamOption>>() {});
    }

    private void ensureServerAvailable() throws IOException, InterruptedException {
        String health = get(baseUrl + "/api/health");
        if (health == null || !health.contains("UP")) {
            throw new IOException("Backend is not reachable at " + baseUrl + ". Start the Spring Boot service first.");
        }
    }

    private HttpResponse<String> postJson(String path, Map<String, String> body) throws IOException, InterruptedException {
        String payload = OBJECT_MAPPER.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            String responseText = response.body() == null ? "" : response.body();
            throw new IOException("Server responded with " + response.statusCode() + ": " + responseText);
        }
        return response;
    }

    private HttpResponse<String> putJson(String path, Map<String, String> body) throws IOException, InterruptedException {
        String payload = OBJECT_MAPPER.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            String responseText = response.body() == null ? "" : response.body();
            throw new IOException("Server responded with " + response.statusCode() + ": " + responseText);
        }
        return response;
    }

    private HttpResponse<String> deleteJson(String path, Map<String, String> body) throws IOException, InterruptedException {
        String payload = OBJECT_MAPPER.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            String responseText = response.body() == null ? "" : response.body();
            throw new IOException("Server responded with " + response.statusCode() + ": " + responseText);
        }
        return response;
    }

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            String responseText = response.body() == null ? "" : response.body();
            throw new IOException("Request failed for " + url + " with status " + response.statusCode() + ": " + responseText);
        }
        return response.body();
    }

    private static String getErrorMessage(Throwable ex) {
        if (ex == null) {
            return "Unknown error";
        }
        String msg = ex.getMessage();
        return msg == null || msg.isBlank() ? "null" : msg;
    }

    private static String escapeJson(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static void main(String[] args) {
        String baseUrl = args.length > 0 ? args[0] : "http://localhost:8080";
        SwingUtilities.invokeLater(() -> {
            NoteClientGui gui = new NoteClientGui(baseUrl);
            gui.setVisible(true);
        });
    }

    public static class UserOption {
        public String id;
        public String username;
        public String email;

        @Override
        public String toString() {
            return username + " (" + email + ")";
        }
    }

    public static class TeamOption {
        public String id;
        public String name;
        public String description;

        @Override
        public String toString() {
            return name;
        }
    }

    public static class TeamNote {
        public String id;
        public String title;
        public String content;
        public String authorId;
        public String teamId;
        public String createdAt;
        public String updatedAt;

        @Override
        public String toString() {
            return (title == null || title.isBlank() ? "(untitled)" : title) + " — " + (authorId == null ? "unknown" : authorId);
        }
    }
}
