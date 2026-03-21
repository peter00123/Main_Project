// AtezhareApplication.java
// Spring Boot application entry point.
// Starts the embedded Tomcat server on port 8080 with context path /atezhare.
// Scans all components under com.atezhare (controllers, services, repositories, etc.)

package com.atezhare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Atezhare backend.
 *
 * @EnableScheduling — enables the @Scheduled session cleanup task
 *   in service/SessionCleanupService.java
 */
@SpringBootApplication
@EnableScheduling
public class AtezhareApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtezhareApplication.class, args);
    }
}
