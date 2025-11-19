package com.romantic.proposal.controller;

import com.romantic.proposal.dto.LoginRequest;
import com.romantic.proposal.dto.LoginResponse;
import com.romantic.proposal.dto.RegisterRequest;
import com.romantic.proposal.entity.User;
import com.romantic.proposal.repository.UserRepository;
import com.romantic.proposal.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            System.out.println("📝 Registration request for: " + request.getEmail());

            // Check if user already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Email already registered"));
            }

            // Create new user
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());

            User savedUser = userRepository.save(user);
            System.out.println("✅ User saved: " + savedUser.getEmail());

            // Generate JWT token using JwtUtil
            String token = jwtUtil.generateToken(savedUser.getEmail());
            System.out.println("✅ Token generated for registration");

            return ResponseEntity.ok(new LoginResponse(
                    savedUser.getEmail(),
                    token,
                    savedUser.getId().toString(),  // Convert UUID to String
                    "Registration successful"
            ));

        } catch (Exception e) {
            System.err.println("❌ Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("🔐 Login request for: " + request.getEmail());

            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

            if (userOptional.isEmpty()) {
                System.out.println("❌ User not found: " + request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid email or password"));
            }

            User user = userOptional.get();

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                System.out.println("❌ Invalid password for: " + request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Invalid email or password"));
            }

            // Generate JWT token using JwtUtil
            String token = jwtUtil.generateToken(user.getEmail());
            System.out.println("✅ Login successful, token generated for: " + user.getEmail());

            return ResponseEntity.ok(new LoginResponse(
                    user.getEmail(),
                    token,
                    user.getId().toString(),
                    "Login successful"
            ));

        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Login failed: " + e.getMessage()));
        }
    }

    // Inner class for error response
    static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}