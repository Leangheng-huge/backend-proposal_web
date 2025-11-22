package com.romantic.proposal.service;

// ... other imports stay the same ...

import com.romantic.proposal.dto.RespondRequest;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final SendGridEmailService sendGridEmailService;  // CHANGED: Use SendGrid instead of EmailService

    @Value("${frontend.url}")
    private String frontendUrl;

    // createProposal method stays the same...

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

        // 🔥 SEND EMAIL USING SENDGRID
        try {
            User user = proposal.getUser();
            String userEmail = user.getEmail();
            String proposalLink = proposal.getShareableLink();

            log.info("📧 Attempting to send SendGrid email to: {}", userEmail);
            sendGridEmailService.sendProposalResponseEmail(
                    userEmail,
                    responseEnum.name(),
                    proposalLink
            );
            log.info("✅ SendGrid email sent successfully to: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send SendGrid email notification", e);
        }

        // Build response - FIXED: Create notification message inline
        String notificationMessage;
        if ("YES".equalsIgnoreCase(responseEnum.name())) {
            notificationMessage = "🎉 They said YES! Congratulations! 💕";
        } else if ("NO".equalsIgnoreCase(responseEnum.name())) {
            notificationMessage = "💙 They respectfully declined. Stay strong!";
        } else {
            notificationMessage = "Response received";
        }

        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("message", "Response recorded successfully");
        response.put("response", responseEnum.name());
        response.put("notification", notificationMessage);  // FIXED: Use inline variable

        return response;
    }
}