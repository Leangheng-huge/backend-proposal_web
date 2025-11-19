package com.romantic.proposal.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String requestURI = request.getRequestURI();

        System.out.println("=== JWT FILTER DEBUG ===");
        System.out.println("Request URI: " + requestURI);
        System.out.println("Auth Header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token extracted: " + token.substring(0, Math.min(20, token.length())) + "...");

            try {
                String email = jwtUtil.extractEmail(token);
                System.out.println("Email extracted: " + email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    boolean isValid = jwtUtil.validateToken(token, email);
                    System.out.println("Token valid: " + isValid);

                    if (isValid) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("✅ Authentication set successfully for: " + email);
                    } else {
                        System.out.println("❌ Token validation failed!");
                    }
                } else {
                    System.out.println("Email is null or already authenticated");
                }
            } catch (Exception e) {
                System.out.println("❌ ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No Bearer token found in header");
        }

        System.out.println("Current Authentication: " + SecurityContextHolder.getContext().getAuthentication());
        System.out.println("========================");

        filterChain.doFilter(request, response);
    }
}