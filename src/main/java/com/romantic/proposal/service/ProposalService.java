package com.romantic.proposal.service;

import com.romantic.proposal.dto.ProposalResponse;
import com.romantic.proposal.dto.RespondRequest;
import com.romantic.proposal.dto.StatusResponse;
import com.romantic.proposal.entity.Proposal;
import com.romantic.proposal.entity.User;
import com.romantic.proposal.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    public ProposalResponse createProposal(User user, String frontendUrl) {
        log.info("🎯 Creating proposal for user: {}", user.getEmail());

        // Check if user already has a proposal
        Optional<Proposal> existingProposals = proposalRepository.findAll()
                .stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst();

        if (existingProposals.isPresent()) {
            Proposal proposal = existingProposals.get();
            log.info("♻️ Returning existing proposal: {}", proposal.getId());

            return ProposalResponse.builder()
                    .proposalId(proposal.getId())
                    .shareableLink(proposal.getShareableLink())
                    .message("Existing proposal returned")
                    .build();
        }

        // Create new proposal
        String uniqueToken = UUID.randomUUID().toString();
        String shareableLink = frontendUrl + "?proposal=" + uniqueToken;

        Proposal proposal = Proposal.builder()
                .user(user)
                .uniqueToken(uniqueToken)
                .shareableLink(shareableLink)
                .createdAt(LocalDateTime.now())
                .build();

        Proposal savedProposal = proposalRepository.save(proposal);
        log.info("✅ New proposal created with ID: {}", savedProposal.getId());

        return ProposalResponse.builder()
                .proposalId(savedProposal.getId())
                .shareableLink(shareableLink)
                .message("Proposal created successfully")
                .build();
    }

    @Transactional
    public Map<String, String> respondToProposal(String uniqueToken, RespondRequest request) {
        log.info("💬 Processing response for token: {}", uniqueToken);
        log.info("Response: {}", request.getResponse());

        // Find proposal by token
        Proposal proposal = proposalRepository.findByUniqueToken(uniqueToken)
                .orElseThrow(() -> {
                    log.error("❌ Proposal not found for token: {}", uniqueToken);
                    return new RuntimeException("Proposal not found");
                });

        // Check if already answered
        if (proposal.getResponse() != null) {
            log.warn("⚠️ Proposal already answered: {}", proposal.getId());
            throw new RuntimeException("This proposal has already been answered");
        }

        // Convert String response to Enum
        Proposal.ProposalResponse responseEnum;
        try {
            responseEnum = Proposal.ProposalResponse.valueOf(request.getResponse().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid response value: {}", request.getResponse());
            throw new RuntimeException("Invalid response. Must be YES or NO");
        }

        // Update proposal with response
        proposal.setResponse(responseEnum);
        proposal.setRespondedAt(LocalDateTime.now());

        Proposal savedProposal = proposalRepository.save(proposal);
        log.info("✅ Proposal updated: {} with response: {}", savedProposal.getId(), responseEnum);

        // 🔥🔥🔥 SEND EMAIL HERE 🔥🔥🔥
        try {
            User user = proposal.getUser();
            String userEmail = user.getEmail();
            String proposalLink = proposal.getShareableLink();

            log.info("📧 Attempting to send email to: {}", userEmail);
            emailService.sendProposalResponseEmail(
                    userEmail,
                    responseEnum.name(), // "YES" or "NO"
                    proposalLink
            );
            log.info("✅ Email sent successfully to: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send email notification", e);
            log.error("Error details: ", e);
            // Don't fail the request if email fails
        }

        // Build response
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("message", "Response recorded successfully");
        response.put("response", responseEnum.name());
        response.put("notification", buildNotificationMessage(responseEnum.name()));

        return response;
    }

    public StatusResponse getProposalStatus(UUID proposalId, User user) {
        log.info("🔍 Checking status for proposal: {}", proposalId);

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> {
                    log.error("❌ Proposal not found: {}", proposalId);
                    return new RuntimeException("Proposal not found");
                });

        // Verify ownership
        if (!proposal.getUser().getId().equals(user.getId())) {
            log.error("❌ Unauthorized access attempt by user: {}", user.getEmail());
            throw new RuntimeException("Unauthorized access to proposal");
        }

        boolean answered = proposal.getResponse() != null;
        String responseStr = answered ? proposal.getResponse().name() : null;

        log.info("📊 Proposal status - Answered: {}, Response: {}", answered, responseStr);

        return StatusResponse.builder()
                .proposalId(proposal.getId())
                .answered(answered)
                .response(responseStr)
                .notification(buildNotificationMessage(responseStr))
                .answeredAt(proposal.getRespondedAt())
                .build();
    }

    private String buildNotificationMessage(String response) {
        if (response == null) {
            return "Waiting for response...";
        }
        if ("YES".equalsIgnoreCase(response)) {
            return "🎉 They said YES! Congratulations! 💕";
        } else if ("NO".equalsIgnoreCase(response)) {
            return "💙 They respectfully declined. Stay strong!";
        }
        return "Response received";
    }
}