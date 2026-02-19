package com.project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dto.LoginRequest;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Backend Connected Successfully!";
    }
    

    private String message = "Hello from Spring Boot Backend 🚀";

    @GetMapping("/api/message")
    public String getMessage() {
        return message;
    }
}
