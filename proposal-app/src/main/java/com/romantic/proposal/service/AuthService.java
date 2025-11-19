package com.romantic.proposal.service;

import com.romantic.proposal.dto.*;
import com.romantic.proposal.entity.User;

import com.romantic.proposal.exception.InvalidCredentialsException;
import com.romantic.proposal.exception.UserAlreadyExistsException;
import com.romantic.proposal.exception.UserNotFoundException;
import com.romantic.proposal.repository.UserRepository;
import com.romantic.proposal.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public Map<String, String> register(RegisterRequest request) {

        // Check valid email format
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidCredentialsException("Invalid email format");
        }

        // Check if email already used
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        user = userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("userId", user.getId().toString());
        response.put("email", user.getEmail());
        response.put("message", "User registered successfully");

        return response;
    }

    public LoginResponse login(LoginRequest request) {

        // Check valid email format
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidCredentialsException("Invalid email format");
        }

        // Check if user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .build();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
