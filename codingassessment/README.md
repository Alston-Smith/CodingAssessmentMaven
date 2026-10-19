# Shared Note-Taking Backend

This project provides a lightweight Spring Boot backend for a shared note-taking service used by multiple small teams.

## Features

- Create users
- Create teams
- Add members to teams
- Create notes within a team
- View notes for a team
- View notes visible to a user
- Update a note when the user is the author

## Tech stack

- Java 17
- Spring Boot 3.3.4
- Maven

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

## API examples

### Create a user

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com"}'
```

### Create a team

```bash
curl -X POST http://localhost:8080/api/teams \
  -H "Content-Type: application/json" \
  -d '{"name":"Product","description":"Customer-facing planning"}'
```

### Add a team member

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

### Health check

```bash
curl http://localhost:8080/api/health
```

## Desktop GUI

A Swing GUI is included to let a user select an account and team and post a shared message without using raw HTTP requests.

### Run the backend

```bash
mvn spring-boot:run
```

### Run the GUI

In your IDE, run the `com.alstonsmith.gui.NoteClientGui` class. If you prefer the command line, build the project first and then run:

```bash
mvn clean package
java -cp target/classes com.alstonsmith.gui.NoteClientGui
```

The GUI will:

- load available users from the backend
- load available teams from the backend
- let you choose an account and team
- let you enter a title and message
- send the note to the selected team on behalf of the selected user

## Storage model

This implementation uses in-memory storage for simplicity. All data is reset when the application restarts.
