package com.atezhare.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${supabase.url}")
    private String supabaseUrl;

    private ECPublicKey cachedPublicKey = null;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("=== JwtFilter running for: " + request.getRequestURI());

        // Skip auth for health check
        if (request.getRequestURI().contains("/auth/test")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("=== Auth header present: " + (authHeader != null));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing Authorization header\"}");
            return;
        }

        String token = authHeader.substring(7);

        try {
            ECPublicKey publicKey = getSupabasePublicKey();

            Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String userId = claims.getSubject();
            System.out.println("=== Token valid. UserId: " + userId);

            if (userId == null || userId.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid token\"}");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userId, null, Collections.emptyList()
                );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.out.println("=== Token verification failed: " + e.getMessage());
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
        }
    }

    /**
     * Fetches Supabase public key from JWKS endpoint and builds ECPublicKey.
     * Uses standard Java crypto — no Bouncy Castle needed.
     * Cached after first fetch.
     */
    private ECPublicKey getSupabasePublicKey() throws Exception {
        if (cachedPublicKey != null) return cachedPublicKey;

        // Fetch JWKS from Supabase
        String jwksUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";
        System.out.println("=== Fetching JWKS from: " + jwksUrl);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(jwksUrl))
            .GET()
            .build();

        HttpResponse<String> httpResponse =
            client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("=== JWKS response: " + httpResponse.body());

        // Parse JWKS JSON
        Map<?, ?> jwks = objectMapper.readValue(httpResponse.body(), Map.class);
        List<?> keys = (List<?>) jwks.get("keys");

        if (keys == null || keys.isEmpty()) {
            throw new Exception("No keys found in JWKS response");
        }

        Map<?, ?> key = (Map<?, ?>) keys.get(0);
        String x = (String) key.get("x");
        String y = (String) key.get("y");

        System.out.println("=== Key x: " + x);
        System.out.println("=== Key y: " + y);

        // Decode base64url coordinates
        byte[] xBytes = Base64.getUrlDecoder().decode(x);
        byte[] yBytes = Base64.getUrlDecoder().decode(y);

        // Build EC point
        ECPoint point = new ECPoint(
            new BigInteger(1, xBytes),
            new BigInteger(1, yBytes)
        );

        // Get P-256 curve parameters using standard Java
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec ecSpec = parameters.getParameterSpec(ECParameterSpec.class);

        // Build the public key
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        cachedPublicKey = (ECPublicKey) keyFactory.generatePublic(
            new ECPublicKeySpec(point, ecSpec)
        );

        System.out.println("=== Public key built successfully");
        return cachedPublicKey;
    }
}