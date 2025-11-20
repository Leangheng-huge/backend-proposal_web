package com.romantic.proposal.service;

import com.romantic.proposal.dto.*;
import com.romantic.proposal.entity.Notification;
import com.romantic.proposal.entity.Proposal;
import com.romantic.proposal.entity.User;
import com.romantic.proposal.exception.ProposalAlreadyAnsweredException;
import com.romantic.proposal.exception.ProposalNotFoundException;
import com.romantic.proposal.repository.NotificationRepository;
import com.romantic.proposal.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public ProposalResponse createProposal(User user, String frontendUrl) {
        String uniqueToken = UUID.randomUUID().toString().replace("-", "");
        String shareableLink = frontendUrl + "?proposal=" + uniqueToken;

        Proposal proposal = Proposal.builder()
                .user(user)
                .uniqueToken(uniqueToken)
                .shareableLink(shareableLink)
                .build();

        proposal = proposalRepository.save(proposal);

        return ProposalResponse.builder()
                .proposalId(proposal.getId().toString())
                .uniqueToken(proposal.getUniqueToken())
                .shareableLink(proposal.getShareableLink())
                .createdAt(proposal.getCreatedAt().toString())
                .build();
    }

    @Transactional
    public Map<String, String> respondToProposal(String uniqueToken, RespondRequest request) {
        Proposal proposal = proposalRepository.findByUniqueToken(uniqueToken)
                .orElseThrow(() -> new ProposalNotFoundException("Invalid proposal token"));

        if (proposal.getResponse() != null) {
            throw new ProposalAlreadyAnsweredException("This proposal has already been answered");
        }

        Proposal.ProposalResponse response = Proposal.ProposalResponse.valueOf(request.getResponse());
        proposal.setResponse(response);
        proposal.setRespondedAt(LocalDateTime.now());
        proposalRepository.save(proposal);

        // Create notification for user
        String notificationMessage = response == Proposal.ProposalResponse.YES
                ? "Congratulations!! I am so, so happy for you! You totally deserve this."
                : "It's okay to feel disappointed/sad/angry. Take all the time you need to process it.";

        Notification notification = Notification.builder()
                .proposal(proposal)
                .user(proposal.getUser())
                .message(notificationMessage)
                .build();
        notificationRepository.save(notification);

        // Send email notification
        emailService.sendProposalResponseEmail(
                proposal.getUser().getEmail(),
                request.getResponse(),
                proposal.getShareableLink()
        );

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Response recorded successfully");
        responseMap.put("status", "success");
        return responseMap;
    }
}
