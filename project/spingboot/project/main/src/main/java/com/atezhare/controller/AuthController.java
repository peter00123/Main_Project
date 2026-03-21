package com.atezhare.controller;

// AuthController.java  — replaces TestController.java
//
// Handles authentication for the Android app.
// Called by: ui/auth/LoginViewModel.login()  (POST /atezhare/auth/login)
//
// Current implementation validates hardcoded credentials (admin / 1234)
// matching the Android app's LoginViewModel. Returns a simple token string.
// Swap out the hardcoded check for a database user lookup when ready.

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    // Hardcoded credentials — matches LoginViewModel.kt (admin / 1234)
    // Replace with UserRepository.findByUsername() for real auth
    private static final String VALID_USER = "admin";
    private static final String VALID_PASS = "1234";

    /**
     * POST /atezhare/auth/login
     *
     * Body: { "userId": "admin", "password": "1234" }
     * Returns:
     *   200 { success: true, token: "...", userId: "admin", message: "Login successful" }
     *   401 { success: false, token: null, userId: null, message: "Invalid credentials" }
     *
     * The returned token is stored by utils/SessionManager.kt on the Android side
     * and sent as Authorization: Bearer <token> on subsequent requests via RetrofitClient.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        // Android app sends "userId" field (see model/Models.kt LoginRequest)
        String userId   = (String) body.getOrDefault("userId", "");
        String password = (String) body.getOrDefault("password", "");

        if (VALID_USER.equals(userId) && VALID_PASS.equals(password)) {
            // Generate a simple session token — replace with JWT signing in production
            String token = "atezhare-token-" + UUID.randomUUID();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token",   token,
                "userId",  userId,
                "message", "Login successful"
            ));
        }

        return ResponseEntity.status(401).body(Map.of(
            "success", false,
            "token",   "",
            "userId",  "",
            "message", "Invalid credentials"
        ));
    }

    /**
     * GET /atezhare/auth/test
     * Quick health-check endpoint — replaces the old TestController /api/test
     */
    @GetMapping("/test")
    public String test() {
        return "Atezhare backend connected successfully!";
    }
}
