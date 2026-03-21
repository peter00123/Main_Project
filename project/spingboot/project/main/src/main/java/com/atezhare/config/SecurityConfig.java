package com.atezhare.config;

// SecurityConfig.java
// Configures Spring Security to permit all requests to the Atezhare API.
// Without this, Spring Security blocks all incoming requests with 401 by default.
//
// This is a permissive dev config. For production:
//   - Protect /files/upload and /session/* with JWT validation
//   - Keep /auth/login and /auth/test open (permitAll)
//   - Enable CSRF for web clients if adding a web UI

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — Android app uses stateless REST, not browser form sessions
            .csrf(AbstractHttpConfigurer::disable)

            // Allow all requests — Android app handles its own auth token logic
            // See utils/SessionManager.kt and network/RetrofitClient.kt on Android side
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

            // Disable default form login page
            .formLogin(AbstractHttpConfigurer::disable)

            // Disable HTTP Basic auth popup
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
