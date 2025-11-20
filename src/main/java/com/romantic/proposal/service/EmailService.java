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
     * Sends an email notification to the user about the response to their proposal.
     *
     * @param toEmail      Recipient email address
     * @param response     Proposal response ("YES" or other)
     * @param proposalLink Link to view the proposal
     */
    public void sendProposalResponseEmail(String toEmail, String response, String proposalLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);

            if ("YES".equalsIgnoreCase(response)) {
                preparePositiveResponseEmail(message, proposalLink);
            } else {
                prepareNegativeResponseEmail(message, proposalLink);
            }

            mailSender.send(message);
            log.info("✅ Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send email to: {}", toEmail, e);
            // Do not throw exception: email failure should not break the app
        }
    }

    private void preparePositiveResponseEmail(SimpleMailMessage message, String proposalLink) {
        message.setSubject("🎉 Great News! Someone Accepted Your Proposal!");
        message.setText(
                "Congratulations!! 🎉🎉\n\n" +
                        "Someone accepted your romantic proposal!\n\n" +
                        "I am so, so happy for you! You totally deserve this. 💕\n\n" +
                        "Proposal Link: " + proposalLink + "\n\n" +
                        "Best wishes,\n" +
                        "Romantic Proposal App"
        );
    }

    private void prepareNegativeResponseEmail(SimpleMailMessage message, String proposalLink) {
        message.setSubject("💙 Response to Your Proposal");
        message.setText(
                "Hello,\n\n" +
                        "Someone has responded to your romantic proposal.\n\n" +
                        "It's okay to feel disappointed, sad, or angry. Take all the time you need to process it. 💙\n\n" +
                        "Remember, this doesn't define your worth. Keep your head up!\n\n" +
                        "Proposal Link: " + proposalLink + "\n\n" +
                        "Take care,\n" +
                        "Romantic Proposal App"
        );
    }
}
