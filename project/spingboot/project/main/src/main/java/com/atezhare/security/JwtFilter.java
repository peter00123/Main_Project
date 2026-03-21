package com.atezhare.security;

// JwtFilter.java
// Intercepts every HTTP request and validates the Supabase JWT token
// from the Authorization: Bearer <token> header.
//
// If token is valid:
//   - Extracts the userId (UUID) from the "sub" claim
//   - Sets it in SecurityContext so controllers can access it via
//     SecurityContextHolder.getContext().getAuthentication().getName()
//
// If token is missing or invalid:
//   - Returns 401 Unauthorized
//   - Request never reaches the controller
//
// Public endpoints that skip this filter: none (all require auth)
// Except: /auth/test (health check) — whitelisted in SecurityConfig

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip filter for the health check endpoint
        if (request.getRequestURI().contains("/auth/test")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // No token → reject
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Missing or invalid Authorization header\"}");
            return;
        }

        String token = authHeader.substring(7); // remove "Bearer "

        try {
            // Verify the token signature using the Supabase JWT secret
            Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

            // "sub" claim contains the user's UUID (e.g. "f47ac10b-58cc-4372-a567-0e02b2c3d479")
            String userId = claims.getSubject();

            if (userId == null || userId.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid token: missing subject\"}");
                return;
            }

            // Store userId in SecurityContext — accessible in controllers via:
            // SecurityContextHolder.getContext().getAuthentication().getName()
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Pass request to the next filter / controller
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Token expired, invalid signature, malformed, etc.
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
        }
    }
}