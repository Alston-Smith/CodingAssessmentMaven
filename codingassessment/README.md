# Shared Notes Service

This project provides a Spring Boot-based note-sharing service for a small group of teams. The backend stores users, teams, and notes, and the included Swing GUI gives a quick desktop workflow for creating records and managing note activity without manually calling the REST API.

## Current features

### Backend

- Create users with a required password
- Enforce unique account names and team names (case-insensitive)
- Encrypt stored account passwords using AES-256
- Require a selected/logged-in user to create a team
- Automatically add the creator to any team they create
- Add users to teams
- View users and teams
- Create notes within a team
- View all notes for a team
- View all notes visible to a user
- Update notes when the user is the author
- Delete notes when the user is the author
- Restrict team membership rules for note creation and editing
- Persistent JSON storage across service restarts
- Health endpoint for basic availability checks

### GUI

- Select a user and team from the loaded lists
- Create new users with username, email, and password fields
- Create a team only when an account is selected
- Team creation automatically adds the currently selected account to the team
- Add the selected user to the selected team
- Post a message to the team on behalf of the selected user
- View messages in the selected team
- Edit selected messages
- Delete selected messages
- Copy status messages to the clipboard
- Display success and failure status updates in the UI

## Tech stack

- Java 17
- Spring Boot 3.3.4
- Maven
- Jackson JSON support
- Swing desktop client

## Run locally

From the project directory:

```bash
mvn clean package
mvn spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

## API overview

### Health

```bash
curl http://localhost:8080/api/health
```

### Create a user

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"Password123!"}'
```

User account names must be unique, and passwords are stored encrypted with AES-256.

### Get all users

```bash
curl http://localhost:8080/api/users
```

### Create a team

```bash
curl -X POST http://localhost:8080/api/teams \
  -H "Content-Type: application/json" \
  -d '{"name":"Product","description":"Customer-facing planning","userId":"user-12345678"}'
```

Team names must be unique and the selected user is automatically added as a member when the team is created.

### Get all teams

```bash
curl http://localhost:8080/api/teams
```

### Add a user to a team

```bash
curl -X POST http://localhost:8080/api/teams/{teamId}/members \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-12345678"}'
```

### Create a note

```bash
curl -X POST http://localhost:8080/api/teams/{teamId}/notes \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-12345678","title":"Release checklist","content":"Ship on Friday"}'
```

### View notes for a team

```bash
curl http://localhost:8080/api/teams/{teamId}/notes
```

### View notes visible to a user

```bash
curl http://localhost:8080/api/users/{userId}/notes
```

### Update a note

```bash
curl -X PUT http://localhost:8080/api/notes/{noteId} \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-12345678","title":"Updated release checklist","content":"Ship on Monday"}'
```

### Delete a note

```bash
curl -X DELETE http://localhost:8080/api/notes/{noteId} \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-12345678"}'
```

## Desktop GUI

A Swing GUI is included to let users work with accounts, teams, and notes without directly composing HTTP requests.

### Start the backend

```bash
mvn spring-boot:run
```

### Start the GUI

Run the class `com.alstonsmith.gui.NoteClientGui` from your IDE or from the command line after building:

```bash
mvn clean package
java -cp target/classes com.alstonsmith.gui.NoteClientGui
```

The GUI supports:

- listing existing users and teams
- creating a new user or team
- selecting an account and team
- adding the selected user to the selected team
- posting a note to the team
- refreshing and viewing team messages
- editing and deleting the selected message
- copying status text to the clipboard

## Data persistence

Data is persisted to a local JSON file in the project working directory as `shared-notes-data.json`. This means users, teams, and notes remain available after the service restarts instead of resetting to memory-only state.

## Notes on business rules

- Only team members may create messages in that team.
- Notes can be edited or deleted only by their original author.
- The UI uses the selected user and team to submit actions to the REST API.
