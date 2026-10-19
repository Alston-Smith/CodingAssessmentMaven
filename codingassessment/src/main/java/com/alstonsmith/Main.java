package com.alstonsmith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the shared notes backend.
 * The service exposes REST endpoints for managing users, teams, and notes.
 */
@SpringBootApplication
public class Main {
    /**
     * Starts the Spring Boot application and initializes the REST API and GUI backend.
     *
     * @param args command-line arguments passed to the Spring Boot application
     */
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}