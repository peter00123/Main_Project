package com.atezhare.controller;

// AuthController.java (UPDATED)
// Login endpoint removed — Supabase Auth handles sign up and login
// directly on the Android app. Spring Boot only validates the JWT token
// that Supabase issues, via JwtFilter.java.
//
// This controller only keeps the /auth/test health check endpoint
// so you can verify the backend is running.

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * GET /atezhare/auth/test
     * Public health check — no token required.
     * Use this to verify the backend is reachable from the Android app.
     */
    @GetMapping("/test")
    public String test() {
        return "Atezhare backend is running!";
    }

    /**
     * GET /atezhare/auth/me
     * Returns the authenticated user's ID extracted from the JWT.
     * Useful for debugging — confirms the token is being validated correctly.
     * The userId here comes from JwtFilter via SecurityContextHolder.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
            "userId", authentication.getName(),
            "message", "Token is valid"
        ));
    }
}