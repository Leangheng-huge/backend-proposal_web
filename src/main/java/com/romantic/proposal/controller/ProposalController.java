package com.romantic.proposal.controller;

import com.romantic.proposal.dto.ProposalResponse;
import com.romantic.proposal.dto.RespondRequest;
import com.romantic.proposal.dto.StatusResponse;
import com.romantic.proposal.entity.User;
import com.romantic.proposal.service.AuthService;
import com.romantic.proposal.service.ProposalService;
import com.romantic.proposal.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/proposal")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // Frontend URL - CHANGE THIS to match your frontend location!
    @Value("${frontend.url:http://127.0.0.1:5500/index.html}")
    private String frontendUrl;

    @PostMapping("/create")
    public ResponseEntity<ProposalResponse> createProposal(
            @RequestHeader("Authorization") String authHeader) {

        String email = extractEmailFromToken(authHeader);
        User user = authService.getUserByEmail(email);

        // Pass frontend URL to service
        ProposalResponse response = proposalService.createProposal(user, frontendUrl);

        System.out.println("✅ Created proposal with link: " + response.getShareableLink());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{uniqueToken}/respond")
    public ResponseEntity<Map<String, String>> respondToProposal(
            @PathVariable String uniqueToken,
            @Valid @RequestBody RespondRequest request) {

        Map<String, String> response = proposalService.respondToProposal(uniqueToken, request);

        System.out.println("✅ Proposal responded: " + request.getResponse());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{proposalId}/status")
    public ResponseEntity<StatusResponse> getProposalStatus(
            @PathVariable String proposalId,
            @RequestHeader("Authorization") String authHeader) {

        String email = extractEmailFromToken(authHeader);
        User user = authService.getUserByEmail(email);

        StatusResponse response = proposalService.getProposalStatus(UUID.fromString(proposalId), user);

        return ResponseEntity.ok(response);
    }

    private String extractEmailFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.extractEmail(token);
    }
}