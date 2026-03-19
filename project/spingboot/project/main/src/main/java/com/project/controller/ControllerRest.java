package com.project.controller;

import org.springframework.web.bind.annotation.*;

import dto.LoginRequest;

@RestController
@RequestMapping("/api/auth")
public class ControllerRest {

    // Hardcoded credentials (for learning purpose)
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "1234";

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {

        if (USERNAME.equals(request.getUsername())
                && PASSWORD.equals(request.getPassword())) {

            return "LOGIN_SUCCESS";
        } else {
            return "INVALID_CREDENTIALS";
        }
    }

    // Optional test mapping (for browser check)
    @GetMapping("/login")
    public String test() {
        return "Use POST method to login";
    }
    

    
}


