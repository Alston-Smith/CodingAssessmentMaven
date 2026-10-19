package com.alstonsmith.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class NoteClientGui extends JFrame {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    private final JTextField newTeamNameField = new JTextField();
    private final JTextArea newTeamDescriptionArea = new JTextArea(3, 20);
    private final JLabel statusLabel = new JLabel("Ready");

    private final String baseUrl;

    public NoteClientGui(String baseUrl) {
        super("Shared Notes");
        this.baseUrl = baseUrl;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        setPreferredSize(new Dimension(760, 520));
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
        form.add(userCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Team:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
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

        JButton createUserButton = new JButton("Create User");
        createUserButton.addActionListener(e -> createUser());
        userGbc.gridx = 0;
        userGbc.gridy = 2;
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

        JButton createTeamButton = new JButton("Create Team");
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

        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.add(form, BorderLayout.CENTER);
        container.add(creationPanel, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshButton = new JButton("Refresh Lists");
        JButton submitButton = new JButton("Post Message");
        actions.add(refreshButton);
        actions.add(submitButton);

        refreshButton.addActionListener(e -> loadSelections());
        submitButton.addActionListener(e -> postMessage());

        add(container, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        footer.add(statusLabel, BorderLayout.CENTER);
        add(footer, BorderLayout.NORTH);

        pack();
        loadSelections();
    }

    private void loadSelections() {
        statusLabel.setText("Loading accounts and teams...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    List<UserOption> users = fetchUsers();
                    List<TeamOption> teams = fetchTeams();
                    SwingUtilities.invokeLater(() -> {
                        usersModel.removeAllElements();
                        for (UserOption user : users) {
                            usersModel.addElement(user);
                        }
                        if (usersModel.getSize() > 0) {
                            userCombo.setSelectedIndex(0);
                        }

                        teamsModel.removeAllElements();
                        for (TeamOption team : teams) {
                            teamsModel.addElement(team);
                        }
                        if (teamsModel.getSize() > 0) {
                            teamCombo.setSelectedIndex(0);
                        }

                        statusLabel.setText("Loaded " + usersModel.getSize() + " account(s) and " + teamsModel.getSize() + " team(s)");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Failed to load data: " + ex.getMessage()));
                }
                return null;
            }
        };
        worker.execute();
    }

    private void createUser() {
        String username = newUserNameField.getText() == null ? "" : newUserNameField.getText().trim();
        String email = newUserEmailField.getText() == null ? "" : newUserEmailField.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            statusLabel.setText("Please enter both a username and email.");
            return;
        }

        statusLabel.setText("Creating user...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    ensureServerAvailable();
                    postJson("/api/users", Map.of("username", username, "email", email));

                    newUserNameField.setText("");
                    newUserEmailField.setText("");
                    loadSelections();
                    SwingUtilities.invokeLater(() -> statusLabel.setText("User created: " + username));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Failed to create user: " + getErrorMessage(ex)));
                }
                return null;
            }
        };
        worker.execute();
    }

    private void createTeam() {
        String teamName = newTeamNameField.getText() == null ? "" : newTeamNameField.getText().trim();
        String description = newTeamDescriptionArea.getText() == null ? "" : newTeamDescriptionArea.getText().trim();

        if (teamName.isEmpty()) {
            statusLabel.setText("Please enter a team name.");
            return;
        }

        statusLabel.setText("Creating team...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    ensureServerAvailable();
                    postJson("/api/teams", Map.of("name", teamName, "description", description));

                    newTeamNameField.setText("");
                    newTeamDescriptionArea.setText("");
                    loadSelections();
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Team created: " + teamName));
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Failed to create team: " + getErrorMessage(ex)));
                }
                return null;
            }
        };
        worker.execute();
    }

    private void postMessage() {
        UserOption selectedUser = (UserOption) userCombo.getSelectedItem();
        TeamOption selectedTeam = (TeamOption) teamCombo.getSelectedItem();
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String content = contentArea.getText() == null ? "" : contentArea.getText().trim();

        if (selectedUser == null || selectedTeam == null) {
            statusLabel.setText("Please select both an account and a team.");
            return;
        }

        if (title.isEmpty()) {
            statusLabel.setText("Please enter a message title.");
            return;
        }

        if (content.isEmpty()) {
            statusLabel.setText("Please enter a message.");
            return;
        }

        statusLabel.setText("Posting note...");
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
                        statusLabel.setText("Message posted to " + selectedTeam.name + " by " + selectedUser.username + ".");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> statusLabel.setText("Failed to post message: " + getErrorMessage(ex)));
                }
                return null;
            }
        };
        worker.execute();
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
}
