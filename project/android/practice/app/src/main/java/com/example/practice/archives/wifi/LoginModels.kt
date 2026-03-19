package com.example.practice.archives.wifi

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val message: String
)