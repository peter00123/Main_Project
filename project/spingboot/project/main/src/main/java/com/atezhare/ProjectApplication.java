package com.atezhare;

// ProjectApplication.java  (UPDATED — add @EnableScheduling)
//
// @EnableScheduling is required for SessionController's @Scheduled cleanup task
// that removes expired sessions every 5 minutes.
// Without this annotation, the cleanup method is never invoked.

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   // ADD THIS — needed by SessionController.cleanupExpiredSessions()
public class ProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
    }
}
