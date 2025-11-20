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

    /**
     * Creates a new proposal for the given user.
     *
     * @param user        The user creating the proposal.
     * @param frontendUrl The frontend URL to generate a shareable link.
     * @return ProposalResponse containing details of the created proposal.
     */
    @Transactional
    public ProposalResponse createProposal(User user, String frontendUrl) {
        String uniqueToken = UUID.randomUUID().toString().replace("-", "");
        String shareableLink = frontendUrl + "?proposal=" + uniqueToken;

        System.out.println("✅ Creating proposal with link: " + shareableLink);

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

    /**
     * Responds to a proposal identified by its unique token.
     *
     * @param uniqueToken The unique token of the proposal.
     * @param request     The response request containing YES/NO.
     * @return A map indicating the status of the response.
     */
    @Transactional
    public Map<String, String> respondToProposal(String uniqueToken, RespondRequest request) {
        Proposal proposal = proposalRepository.findByUniqueToken(uniqueToken)
                .orElseThrow(() -> new ProposalNotFoundException("Invalid proposal token"));

        if (proposal.getResponse() != null) {
            throw new ProposalAlreadyAnsweredException("This proposal has already been answered");
        }

        Proposal.ProposalResponse response = Proposal.ProposalResponse.valueOf(request.getResponse().toUpperCase());
        proposal.setResponse(response);
        proposal.setRespondedAt(LocalDateTime.now());
        proposalRepository.save(proposal);

        // Create notification
        String notificationMessage = getNotificationMessage(response);
        Notification notification = Notification.builder()
                .proposal(proposal)
                .user(proposal.getUser())
                .message(notificationMessage)
                .build();
        notificationRepository.save(notification);

        // Send email
        emailService.sendProposalResponseEmail(proposal.getUser().getEmail(), request.getResponse(), proposal.getShareableLink());

        System.out.println("✅ Proposal responded: " + request.getResponse());

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Response recorded successfully");
        responseMap.put("status", "success");

        return responseMap;
    }

    /**
     * Retrieves the status of a proposal for a given user.
     *
     * @param proposalId The ID of the proposal.
     * @param user       The user who owns the proposal.
     * @return StatusResponse containing whether it was answered and related info.
     */
    public StatusResponse getProposalStatus(UUID proposalId, User user) {
        Proposal proposal = proposalRepository.findByIdAndUser(proposalId, user)
                .orElseThrow(() -> new ProposalNotFoundException("Proposal not found"));

        boolean answered = proposal.getResponse() != null;
        String response = answered ? proposal.getResponse().name() : null;
        String notification = answered ? getNotificationMessage(proposal.getResponse()) : null;

        return StatusResponse.builder()
                .answered(answered)
                .response(response)
                .notification(notification)
                .build();
    }

    /**
     * Helper method to generate notification messages based on response.
     *
     * @param response YES/NO response.
     * @return Notification message string.
     */
    private String getNotificationMessage(Proposal.ProposalResponse response) {
        if (response == Proposal.ProposalResponse.YES) {
            return "Congratulations!! I am so, so happy for you! You totally deserve this.";
        } else {
            return "It's okay to feel disappointed/sad/angry. Take all the time you need to process it.";
        }
    }
}
