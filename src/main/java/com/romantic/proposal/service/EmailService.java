package com.romantic.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    /**
     * Sends an email notifying the recipient about a proposal response.
     *
     * @param toEmail       The recipient's email address.
     * @param response      The response to the proposal ("YES" or any other value).
     * @param proposalLink  The link to the proposal.
     */
    public void sendProposalResponseEmail(String toEmail, String response, String proposalLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);

            if ("YES".equalsIgnoreCase(response)) {
                message.setSubject("🎉 Great News! Someone Accepted Your Proposal!");
                message.setText(buildAcceptedProposalMessage(proposalLink));
            } else {
                message.setSubject("💙 Response to Your Proposal");
                message.setText(buildRejectedProposalMessage(proposalLink));
            }

            mailSender.send(message);
            log.info("Email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", toEmail, e);
            // Avoid throwing exceptions to prevent breaking the app
        }
    }

    private String buildAcceptedProposalMessage(String proposalLink) {
        return "Congratulations!! 🎉🎉\n\n" +
                "Someone accepted your romantic proposal!\n\n" +
                "I am so, so happy for you! You totally deserve this. 💕\n\n" +
                "Proposal Link: " + proposalLink + "\n\n" +
                "Best wishes,\n" +
                "Romantic Proposal App";
    }

    private String buildRejectedProposalMessage(String proposalLink) {
        return "Hello,\n\n" +
                "Someone has responded to your romantic proposal.\n\n" +
                "It's okay to feel disappointed, sad, or angry. Take all the time you need to process it. 💙\n\n" +
                "Remember, this doesn't define your worth. Keep your head up!\n\n" +
                "Proposal Link: " + proposalLink + "\n\n" +
                "Take care,\n" +
                "Romantic Proposal App";
    }
}
